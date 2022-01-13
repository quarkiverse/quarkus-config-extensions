package io.quarkiverse.config.jdbc.runtime;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.logging.Logger;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.common.MapBackedConfigSource;

public class JdbcConfigSourceFactory implements ConfigSourceFactory {
    private static final Logger log = Logger.getLogger(JdbcConfigSourceFactory.class);
    Repository repository = null;

    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        return getConfigSource(context);
    }

    private List<ConfigSource> getConfigSource(ConfigSourceContext context) {

        boolean enabled = Boolean.valueOf(
                Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.enabled").getValue()).orElse("true"));
        if (!enabled) {
            return Collections.emptyList();
        }

        String table = Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.table").getValue())
                .orElse("configuration");
        String keyColumn = Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.key").getValue())
                .orElse("key");
        String valueColumn = Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.value").getValue())
                .orElse("value");
        boolean cacheEnabled = Boolean
                .valueOf(Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.cache").getValue())
                        .orElse("true"));

        // Datasource config
        String username = context.getValue("quarkus.datasource.username").getValue();
        String password = context.getValue("quarkus.datasource.password").getValue();
        String url = context.getValue("quarkus.datasource.jdbc.url").getValue();

        Map<String, String> result;
        List<ConfigSource> list = new ArrayList<>();
        if (repository == null) {
            repository = new Repository(url, username, password, table, keyColumn, valueColumn);
        }
        try {
            result = repository.getAllConfigValues();

            if (cacheEnabled) {
                list.add(new InMemoryConfigSource("jdbc-config", result, 400));
            } else {
                list.add(new JdbcConfigSource("jdbc-config", repository, 400));
            }

        } catch (SQLException e) {
            log.warn("jdbc-config disabled. reason: " + e.getLocalizedMessage());
            return Collections.emptyList();
        }
        return list;

    }

    private static final class InMemoryConfigSource extends MapBackedConfigSource {

        public InMemoryConfigSource(String name, Map<String, String> propertyMap, int defaultOrdinal) {
            super(name, propertyMap, defaultOrdinal);
        }

    }

}
