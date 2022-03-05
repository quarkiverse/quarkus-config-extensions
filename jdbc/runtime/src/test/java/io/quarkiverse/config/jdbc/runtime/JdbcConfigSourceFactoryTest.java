package io.quarkiverse.config.jdbc.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JdbcConfigSourceFactoryTest {
    private final JdbcConfigConfig config = mock(JdbcConfigConfig.class);
    private final Repository repository = mock(Repository.class);

    @Test
    @DisplayName("Repository returns data")
    void testOnStoredData() throws SQLException {
        String key = "foo";
        String value = "sample value";
        JdbcConfigSourceFactory factory = new JdbcConfigSourceFactory();
        Map<String, String> map = new HashMap<>();
        map.put(key, value);

        config.enabled = true;

        when(repository.getAllConfigValues()).thenReturn(map);
        when(repository.getValue(key)).thenReturn(value);

        List<ConfigSource> configSources = factory.getConfigSource(config, repository);
        assertEquals(1, configSources.size());

        ConfigSource configSource = configSources.iterator().next();
        assertEquals(value, configSource.getValue(key));

    }

    @Test
    @DisplayName("On disabled datasource is empty")
    void testDisabledJdbcConfig() {
        JdbcConfigSourceFactory factory = new JdbcConfigSourceFactory();

        config.enabled = false;

        List<ConfigSource> configSources = factory.getConfigSource(config, repository);
        assertTrue(configSources.isEmpty());

    }

}
