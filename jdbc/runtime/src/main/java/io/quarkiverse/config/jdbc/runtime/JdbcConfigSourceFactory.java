package io.quarkiverse.config.jdbc.runtime;

import java.sql.SQLException;
import java.time.Duration;
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

    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        try {
            final JdbcConfigConfig config = populateConfig(context);
            final Repository repository = new Repository(config);
            return getConfigSource(config, repository);
        } catch (SQLException e) {
            log.warn("jdbc-config disabled. reason: " + e.getLocalizedMessage());
            return Collections.emptyList();
        }
    }

    protected List<ConfigSource> getConfigSource(final JdbcConfigConfig config, final Repository repository) {

        if (!config.enabled) {
            return Collections.emptyList();
        }

        final List<ConfigSource> list = new ArrayList<>();
        final Map<String, String> result = repository.getAllConfigValues();

        if (config.cache) {
            list.add(new InMemoryConfigSource("jdbc-config", result, 400));
        } else {
            list.add(new JdbcConfigSource("jdbc-config", repository, 400));
        }

        return list;

    }

    private JdbcConfigConfig populateConfig(ConfigSourceContext context) {
        final JdbcConfigConfig config = new JdbcConfigConfig();

        // jdbc-config parameters

        config.enabled = Boolean.valueOf(Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.enabled").getValue())
                .orElse(String.valueOf(config.enabled)));

        // short-circuit if config is disabled
        if (!config.enabled) {
            return config;
        }

        config.cache = Boolean.valueOf(Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.cache").getValue())
                .orElse(String.valueOf(config.cache)));

        // table, keyColumn, valueColumn
        config.table = Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.table").getValue())
                .orElse(config.table);
        config.keyColumn = Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.key").getValue())
                .orElse(config.keyColumn);
        config.valueColumn = Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.value").getValue())
                .orElse(config.valueColumn);

        // connection parameters

        // url, username, password (use default datasource values if jdbc-config values are not defined )
        config.username = Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.username").getValue())
                .or(() -> Optional.ofNullable(context.getValue("quarkus.datasource.username").getValue()));
        config.password = Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.password").getValue())
                .or(() -> Optional.ofNullable(context.getValue("quarkus.datasource.password").getValue()));
        config.url = Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.url").getValue())
                .or(() -> Optional.ofNullable(context.getValue("quarkus.datasource.jdbc.url").getValue()));

        // initialSize, minSize, maxSize, acquisitionTimeout
        config.initialSize = Integer
                .parseInt(Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.initial-size").getValue())
                        .orElse(String.valueOf(config.initialSize)));
        config.minSize = Integer
                .parseInt(Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.min-size").getValue())
                        .orElse(String.valueOf(config.minSize)));
        config.maxSize = Integer
                .parseInt(Optional.ofNullable(context.getValue("quarkus.config.source.jdbc.max-size").getValue())
                        .orElse(String.valueOf(config.maxSize)));

        Optional<String> timeout = Optional
                .ofNullable(context.getValue("quarkus.config.source.jdbc.acquisition-timeout").getValue());

        // acquisitionTimeout (java.time.Duration) is allowed to be just numbers (seconds) or standart Duratino format (PTXX)
        if (timeout.isPresent() && timeout.get().matches("[0-9]+")) {
            config.acquisitionTimeout = Duration.ofSeconds(Long.parseLong(timeout.get()));
        } else {
            config.acquisitionTimeout = Duration.parse(timeout.orElse(config.acquisitionTimeout.toString()));
        }
        return config;
    }

    private static final class InMemoryConfigSource extends MapBackedConfigSource {

        public InMemoryConfigSource(String name, Map<String, String> propertyMap, int defaultOrdinal) {
            super(name, propertyMap, defaultOrdinal);
        }

    }

}
