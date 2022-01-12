package io.quarkiverse.config.jdbc.runtime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;

public class Repository implements AutoCloseable {

    private final String url;
    private final String username;
    private final String password;
    private final String table;
    private final String keyColumn;
    private final String valueColumn;

    private AgroalDataSource dataSource;
    private PreparedStatement selectAll;
    private PreparedStatement selectKeys;
    private PreparedStatement selectValue;

    public Repository(String url, String username, String password, String table, String keyColumn, String valueColumn)
            throws SQLException {
        this.url = url;
        this.username = username;
        this.password = password;
        this.table = table;
        this.keyColumn = keyColumn;
        this.valueColumn = valueColumn;
    }

    public synchronized Map<String, String> getAllConfigValues()
            throws SQLException {
        Map<String, String> result = new HashMap<>();
        initializeRepository();
        if (selectAll != null) {
            try (ResultSet rs = selectAll.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString(1), rs.getString(2));
                }
            }
        }

        return result;
    }

    public Set<String> getPropertyNames() throws SQLException {
        Set<String> keys = new HashSet<>();
        initializeRepository();
        if (selectKeys != null) {
            try (ResultSet rs = selectKeys.executeQuery()) {
                while (rs.next()) {
                    keys.add(rs.getString(1));
                }
            }
        }
        return keys;
    }

    public String getValue(String propertyName) throws SQLException {
        initializeRepository();
        if (selectValue != null) {
            selectValue.clearParameters();
            selectValue.setString(1, propertyName);
            try (ResultSet rs = selectValue.executeQuery()) {
                while (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private void initializeRepository() throws SQLException {
        if (dataSource == null || !dataSource.isHealthy(false)) {
            prepareDataSource();
            prepareStatments();
        }
    }

    private void prepareDataSource() throws SQLException {
        // create supplier
        AgroalDataSourceConfigurationSupplier dataSourceConfiguration = new AgroalDataSourceConfigurationSupplier();
        // get reference to connection pool
        AgroalConnectionPoolConfigurationSupplier poolConfiguration = dataSourceConfiguration
                .connectionPoolConfiguration();
        // get reference to connection factory
        AgroalConnectionFactoryConfigurationSupplier connectionFactoryConfiguration = poolConfiguration
                .connectionFactoryConfiguration();

        // configure pool
        poolConfiguration.initialSize(5).maxSize(5).minSize(1)
                .acquisitionTimeout(Duration.of(20, ChronoUnit.SECONDS));

        // configure supplier
        connectionFactoryConfiguration.jdbcUrl(url).credential(new NamePrincipal(username))
                .credential(new SimplePassword(password));

        dataSource = AgroalDataSource.from(dataSourceConfiguration.get());
    }

    private void prepareStatments() throws SQLException {
        String selectAllQuery = new StringBuilder("SELECT conf.").append(keyColumn).append(", conf.").append(valueColumn)
                .append(" FROM ").append(table).append(" conf").toString();
        String selectKeysQuery = new StringBuilder("SELECT conf.").append(keyColumn).append(" FROM ").append(table)
                .append(" conf").toString();
        String selectValueQuery = new StringBuilder("SELECT conf.").append(valueColumn).append(" FROM ").append(table)
                .append(" conf").append(" WHERE conf.").append(keyColumn).append(" = ?").toString();

        selectAll = dataSource.getConnection().prepareStatement(selectAllQuery);
        selectKeys = dataSource.getConnection().prepareStatement(selectKeysQuery);
        selectValue = dataSource.getConnection().prepareStatement(selectValueQuery);

    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

}
