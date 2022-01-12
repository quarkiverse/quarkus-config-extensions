package io.quarkiverse.config.jdbc.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigValue;

public class JdbcConfigSourceFactoryTest {
    private ConfigSourceContext context = mock(ConfigSourceContext.class);
    private Repository repository = mock(Repository.class);
    private ConfigValue config = mock(ConfigValue.class);

    @Test
    @DisplayName("Repository returns data")
    void testOnStoredData() throws SQLException {
        String key = "foo";
        String value = "sample value";
        JdbcConfigSourceFactory factory = new JdbcConfigSourceFactory();
        Map<String, String> map = new HashMap<>();
        map.put(key, value);

        when(context.getValue(Mockito.anyString())).thenReturn(config);
        when(config.getValue()).thenReturn(null);

        when(repository.getAllConfigValues()).thenReturn(map);
        when(repository.getValue(key)).thenReturn(value);

        factory.repository = repository;
        Iterable<ConfigSource> it = factory.getConfigSources(context);

        assertTrue(((Collection<?>) it).size() == 1);

        ConfigSource configSource = it.iterator().next();

        assertEquals(value, configSource.getValue(key));

    }

    @Test
    @DisplayName("On SQLException datasource is empty")
    void testOnSqlException() throws SQLException {
        JdbcConfigSourceFactory factory = new JdbcConfigSourceFactory();

        when(context.getValue(Mockito.anyString())).thenReturn(config);
        when(config.getValue()).thenReturn(null);
        when(repository.getAllConfigValues())
                .thenThrow(new SQLException());

        factory.repository = repository;
        Iterable<ConfigSource> it = factory.getConfigSources(context);

        assertTrue(((Collection<?>) it).isEmpty());
    }

    @Test
    @DisplayName("On disabled datasource is empty")
    void testDisabledJdbcConfig() {
        JdbcConfigSourceFactory factory = new JdbcConfigSourceFactory();

        when(context.getValue("quarkus.config.source.jdbc.enabled")).thenReturn(config);
        when(config.getValue()).thenReturn("false");

        Iterable<ConfigSource> it = factory.getConfigSources(context);

        assertTrue(((Collection<?>) it).isEmpty());
    }

}
