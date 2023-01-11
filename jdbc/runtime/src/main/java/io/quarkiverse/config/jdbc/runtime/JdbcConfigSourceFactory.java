package io.quarkiverse.config.jdbc.runtime;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.logging.Logger;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.common.MapBackedConfigSource;

public class JdbcConfigSourceFactory implements ConfigSourceFactory.ConfigurableConfigSourceFactory<JdbcConfigConfig> {
    private static final Logger log = Logger.getLogger(JdbcConfigSourceFactory.class);

    @Override
    public Iterable<ConfigSource> getConfigSources(final ConfigSourceContext context, final JdbcConfigConfig config) {
        if (!config.enabled()) {
            return Collections.emptyList();
        }

        try {
            Repository repository = new Repository(config);
            if (config.cache()) {
                return Collections.singletonList(new InMemoryConfigSource("jdbc-config", repository.getAllConfigValues(), 400));
            } else {
                return Collections.singletonList(new JdbcConfigSource("jdbc-config", repository, 400));
            }
        } catch (SQLException e) {
            log.warn("jdbc-config disabled. reason: " + e.getLocalizedMessage());
            return Collections.emptyList();
        }
    }

    private static final class InMemoryConfigSource extends MapBackedConfigSource {
        public InMemoryConfigSource(String name, Map<String, String> propertyMap, int defaultOrdinal) {
            super(name, propertyMap, defaultOrdinal);
        }
    }
}
