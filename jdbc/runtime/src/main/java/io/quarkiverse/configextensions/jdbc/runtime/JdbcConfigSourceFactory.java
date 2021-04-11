package io.quarkiverse.configextensions.jdbc.runtime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.logging.Logger;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;

public class JdbcConfigSourceFactory implements ConfigSourceFactory {
    private static final Logger log = Logger.getLogger(JdbcConfigSourceFactory.class);

    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        return getConfigSource(context);
    }

    private List<ConfigSource> getConfigSource(ConfigSourceContext context) {

        boolean enabled = Boolean.valueOf(
                Optional.ofNullable(context.getValue("quarkus.jdbc-config.enabled").getValue()).orElse("true"));
        if (!enabled) {
            return Collections.emptyList();
        }

        String table = Optional.ofNullable(context.getValue("quarkus.jdbc-config.table").getValue())
                .orElse("configuration");
        String keyColumn = Optional.ofNullable(context.getValue("quarkus.jdbc-config.key-column").getValue())
                .orElse("key");
        String valueColumn = Optional.ofNullable(context.getValue("quarkus.jdbc-config.value-column").getValue())
                .orElse("value");

        // Datasource config
        String username = context.getValue("quarkus.datasource.username").getValue();
        String password = context.getValue("quarkus.datasource.password").getValue();
        String url = context.getValue("quarkus.datasource.jdbc.url").getValue();

        Map<String, String> result = new HashMap<>();
        List<ConfigSource> list = new ArrayList<>();

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

        } catch (SQLException e) {
            log.warn("jdbc-config disabled. reason: " + e.getLocalizedMessage());
            return Collections.emptyList();
        }

        list.add(new InMemoryConfigSource(400, "jdbc-config", result));
        return list;

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

    private static final class InMemoryConfigSource implements ConfigSource {

        private final Map<String, String> values = new HashMap<>();
        private final int ordinal;
        private final String name;

        private InMemoryConfigSource(int ordinal, String name, Map<String, String> source) {
            this.ordinal = ordinal;
            this.name = name;
            this.values.putAll(source);
        }

        @Override
        public Map<String, String> getProperties() {
            return values;
        }

        @Override
        public Set<String> getPropertyNames() {
            return values.keySet();
        }

        @Override
        public int getOrdinal() {
            return ordinal;
        }

        @Override
        public String getValue(String propertyName) {
            return values.get(propertyName);
        }

        @Override
        public String getName() {
            return name;
        }
    }

}
