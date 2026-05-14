package io.quarkiverse.config.jdbc.runtime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
                    String key = rs.getString(1);
                    String value = rs.getString(2);
                    if (isJson(value)) {
                        Map<String, String> flattened = flattenJson(key, value);
                        result.putAll(flattened);
                    } else {
                        result.put(key, value);
                    }

                }
                return result;
            }
        } catch (SQLException e) {
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
                .jdbcUrl(config.url());

        if (config.driver().isPresent()) {
            connectionFactoryConfiguration.connectionProviderClassName(config.driver().get());
        }

        if (config.username().isPresent()) {
            connectionFactoryConfiguration.credential(new NamePrincipal(config.username().get()));
        }

        if (config.password().isPresent()) {
            connectionFactoryConfiguration.credential(new SimplePassword(config.password().get()));
        }

        dataSource = AgroalDataSource.from(dataSourceConfiguration.get());
    }

    private void prepareQueries(final JdbcConfigConfig config) {
        selectAllQuery = "SELECT conf." + config.keyColumn() + ", conf." + config.valueColumn() + " FROM " + config.table()
                + " conf";
        selectKeysQuery = "SELECT conf." + config.keyColumn() + " FROM " + config.table() + " conf";
        selectValueQuery = "SELECT conf." + config.valueColumn() + " FROM " + config.table() + " conf WHERE conf."
                + config.keyColumn() + " = ?";
    }

    private static boolean isJson(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        String trimmed = value.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    private static Map<String, String> flattenJson(String baseKey, String jsonValue) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode root = MAPPER.readTree(jsonValue);
            flattenJsonNode(baseKey, root, result);
        } catch (Exception exception) {
            log.warn("Failed to parse JSON for key %s: %s", baseKey, exception);
            result.put(baseKey, jsonValue);
        }
        return result;
    }

    private static void flattenJsonNode(String prefix, JsonNode node, Map<String, String> result) {
        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> field : node.properties()) {
                String newPrefix = prefix.isEmpty() ? field.getKey() : prefix + "." + field.getKey();
                flattenJsonNode(newPrefix, field.getValue(), result);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                flattenJsonNode(prefix + "[" + i + "]", node.get(i), result);
            }
        } else if (node.isValueNode()) {
            if (node.isTextual()) {
                result.put(prefix, node.asText());
            } else if (node.isNumber()) {
                result.put(prefix, node.numberValue().toString());
            } else if (node.isBoolean()) {
                result.put(prefix, String.valueOf(node.booleanValue()));
            } else if (!node.isNull()) {
                result.put(prefix, node.toString());
            }
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
