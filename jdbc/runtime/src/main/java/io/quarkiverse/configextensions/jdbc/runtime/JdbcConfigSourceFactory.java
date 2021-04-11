package io.quarkiverse.configextensions.jdbc.runtime;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.logging.Logger;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;

public class JdbcConfigSourceFactory implements ConfigSourceFactory {
    private static final Logger log = Logger.getLogger(JdbcConfigSourceFactory.class);
    Repository repository = null;

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

        Map<String, String> result;
        List<ConfigSource> list = new ArrayList<>();
        if (repository == null) {
            repository = new Repository(url, username, password);
        }
        try {

            result = repository.getAllConfigValues(table, keyColumn, valueColumn);
        } catch (SQLException e) {
            log.warn("jdbc-config disabled. reason: " + e.getLocalizedMessage());
            return Collections.emptyList();
        }

        list.add(new InMemoryConfigSource(400, "jdbc-config", result));
        return list;

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
