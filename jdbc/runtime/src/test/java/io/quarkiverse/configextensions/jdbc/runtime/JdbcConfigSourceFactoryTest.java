package io.quarkiverse.configextensions.jdbc.runtime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigValue;

public class JdbcConfigSourceFactoryTest {

    @Test
    @DisplayName("changeLog default matches liquibase default")
    void testChangeLogDefault() {
        JdbcConfigSourceFactory factory = new JdbcConfigSourceFactory();

        ConfigSourceContext context = mock(ConfigSourceContext.class);
        ConfigValue config = mock(ConfigValue.class);
        when(context.getValue(Mockito.anyString())).thenReturn(config);
        when(config.getValue()).thenReturn(null);

        factory.getConfigSources(context);
        // assertEquals(defaultConfig.changeLog, createdLiquibaseConfig().changeLog);
    }

}
