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

    private final JdbcConfigConfig config;
    private AgroalDataSource dataSource;

    private String selectAllQuery;
    private String selectKeysQuery;
    private String selectValueQuery;

    public Repository(JdbcConfigConfig config) throws SQLException {
        this.config = config;
        prepareDataSource();
        prepareQueries();
    }

    public synchronized Map<String, String> getAllConfigValues() {
        try (final Connection connection = dataSource.getConnection()) {
            final PreparedStatement selectAllStmt = connection.prepareStatement(selectAllQuery);
            try (ResultSet rs = selectAllStmt.executeQuery()) {
                final Map<String, String> result = new HashMap<>();
                while (rs.next()) {
                    result.put(rs.getString(1), rs.getString(2));
                }
                return result;
            } finally {
                selectAllStmt.close();
            }
        } catch (SQLException e) {
            log.trace("config-jdbc: could not get values: " + e.getLocalizedMessage());
            return Collections.emptyMap();
        }
    }

    public synchronized Set<String> getPropertyNames() {
        try (final Connection connection = dataSource.getConnection()) {
            final PreparedStatement selectKeysStmt = connection.prepareStatement(selectKeysQuery);
            final Set<String> keys = new HashSet<>();
            try (ResultSet rs = selectKeysStmt.executeQuery()) {
                while (rs.next()) {
                    keys.add(rs.getString(1));
                }
                return keys;
            } finally {
                selectKeysStmt.close();
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
                while (rs.next()) {
                    return rs.getString(1);
                }
            } finally {
                selectValueStmt.close();
            }
        } catch (SQLException e) {
            log.trace("config-jdbc: could not get value for key " + propertyName + ": " + e.getLocalizedMessage());
        }
        return null;
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
        poolConfiguration
                .initialSize(config.initialSize)
                .minSize(config.initialSize)
                .maxSize(config.maxSize)
                .acquisitionTimeout(config.acquisitionTimeout);

        // configure supplier
        connectionFactoryConfiguration
                .jdbcUrl(config.url.get())
                .credential(new NamePrincipal(config.username.get()))
                .credential(new SimplePassword(config.password.get()));

        dataSource = AgroalDataSource.from(dataSourceConfiguration.get());
    }

    private void prepareQueries() {
        selectAllQuery = new StringBuilder("SELECT conf.").append(config.keyColumn)
                .append(", conf.").append(config.valueColumn)
                .append(" FROM ").append(config.table).append(" conf").toString();

        selectKeysQuery = new StringBuilder("SELECT conf.").append(config.keyColumn)
                .append(" FROM ").append(config.table).append(" conf").toString();

        selectValueQuery = new StringBuilder("SELECT conf.").append(config.valueColumn)
                .append(" FROM ").append(config.table).append(" conf")
                .append(" WHERE conf.").append(config.keyColumn).append(" = ?").toString();
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

}
