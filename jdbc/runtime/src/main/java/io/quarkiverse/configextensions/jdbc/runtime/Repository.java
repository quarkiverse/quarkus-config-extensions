package io.quarkiverse.configextensions.jdbc.runtime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;

public class Repository {
    private String url;
    private String username;
    private String password;

    public Repository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;

    }

    public synchronized Map<String, String> getAllConfigValues(String table, String keyColumn, String valueColumn)
            throws SQLException {
        Map<String, String> result = new HashMap<>();

        try (AgroalDataSource datasource = getDataSource(url, username, password)) {

            String selectAllQuery = new StringBuilder("SELECT conf.").append(keyColumn).append(", conf.")
                    .append(valueColumn).append(" FROM ").append(table).append(" conf").toString();
            PreparedStatement selectAll = datasource.getConnection().prepareStatement(selectAllQuery);

            if (selectAll != null) {
                try (ResultSet rs = selectAll.executeQuery()) {
                    while (rs.next()) {
                        result.put(rs.getString(1), rs.getString(2));
                    }
                }
            }

        }

        return result;
    }

    private AgroalDataSource getDataSource(String url, String username, String password) throws SQLException {
        // create supplier
        AgroalDataSourceConfigurationSupplier dataSourceConfiguration = new AgroalDataSourceConfigurationSupplier();
        // get reference to connection pool
        AgroalConnectionPoolConfigurationSupplier poolConfiguration = dataSourceConfiguration
                .connectionPoolConfiguration();
        // get reference to connection factory
        AgroalConnectionFactoryConfigurationSupplier connectionFactoryConfiguration = poolConfiguration
                .connectionFactoryConfiguration();

        // configure pool
        poolConfiguration.initialSize(2).maxSize(2).minSize(2).maxLifetime(Duration.of(30, ChronoUnit.SECONDS))
                .acquisitionTimeout(Duration.of(20, ChronoUnit.SECONDS));

        // configure supplier
        connectionFactoryConfiguration.jdbcUrl(url).credential(new NamePrincipal(username))
                .credential(new SimplePassword(password));

        return AgroalDataSource.from(dataSourceConfiguration.get());
    }

}
