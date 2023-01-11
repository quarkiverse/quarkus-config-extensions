package io.quarkiverse.config.jdbc.runtime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;

public class Repository implements AutoCloseable {
    private static final Logger log = Logger.getLogger(Repository.class);

    private AgroalDataSource dataSource;

    private String selectAllQuery;
    private String selectKeysQuery;
    private String selectValueQuery;

    public Repository(JdbcConfigConfig config) throws SQLException {
        prepareDataSource(config);
        prepareQueries(config);
    }

    public synchronized Map<String, String> getAllConfigValues() {
        try (final Connection connection = dataSource.getConnection()) {
            try (PreparedStatement selectAllStmt = connection.prepareStatement(selectAllQuery);
                    ResultSet rs = selectAllStmt.executeQuery()) {
                final Map<String, String> result = new HashMap<>();
                while (rs.next()) {
                    result.put(rs.getString(1), rs.getString(2));
                }
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.trace("config-jdbc: could not get values: " + e.getLocalizedMessage());
            return Collections.emptyMap();
        }
    }

    public synchronized Set<String> getPropertyNames() {
        try (final Connection connection = dataSource.getConnection()) {
            try (PreparedStatement selectKeysStmt = connection.prepareStatement(selectKeysQuery);
                    ResultSet rs = selectKeysStmt.executeQuery()) {
                final Set<String> keys = new HashSet<>();
                while (rs.next()) {
                    keys.add(rs.getString(1));
                }
                return keys;
            }
        } catch (SQLException e) {
            log.trace("config-jdbc: could not get keys: " + e.getLocalizedMessage());
            return Collections.emptySet();
        }
    }

    public String getValue(String propertyName) {
        try (final Connection connection = dataSource.getConnection()) {
            final PreparedStatement selectValueStmt = connection.prepareStatement(selectValueQuery);
            selectValueStmt.setString(1, propertyName);
            try (ResultSet rs = selectValueStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            log.trace("config-jdbc: could not get value for key " + propertyName + ": " + e.getLocalizedMessage());
        }
        return null;
    }

    private void prepareDataSource(final JdbcConfigConfig config) throws SQLException {
        // create supplier
        AgroalDataSourceConfigurationSupplier dataSourceConfiguration = new AgroalDataSourceConfigurationSupplier();
        // get reference to connection pool
        AgroalConnectionPoolConfigurationSupplier poolConfiguration = dataSourceConfiguration
                .connectionPoolConfiguration();
        // get reference to connection factory
        AgroalConnectionFactoryConfigurationSupplier connectionFactoryConfiguration = poolConfiguration
                .connectionFactoryConfiguration();

        // configure pool
        poolConfiguration
                .initialSize(config.initialSize())
                .minSize(config.minSize())
                .maxSize(config.maxSize())
                .acquisitionTimeout(config.acquisitionTimeout());

        // configure supplier
        connectionFactoryConfiguration
                .jdbcUrl(config.url())
                .credential(new NamePrincipal(config.username()))
                .credential(new SimplePassword(config.password()));

        dataSource = AgroalDataSource.from(dataSourceConfiguration.get());
    }

    private void prepareQueries(final JdbcConfigConfig config) {
        selectAllQuery = "SELECT conf." + config.keyColumn() + ", conf." + config.valueColumn() + " FROM " + config.table()
                + " conf";
        selectKeysQuery = "SELECT conf." + config.keyColumn() + " FROM " + config.table() + " conf";
        selectValueQuery = "SELECT conf." + config.valueColumn() + " FROM " + config.table() + " conf WHERE conf."
                + config.keyColumn() + " = ?1";
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
