package io.quarkiverse.configextensions.jdbc.extensions.test;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;

@QuarkusTestResource(H2DatabaseTestResource.class)
public class JdbcConfigCustomParamsTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(() -> ShrinkWrap
            .create(JavaArchive.class).addAsResource("default_application.properties", "application.properties"));

    @Test
    @DisplayName("Reads a property from custom config DB")
    public void readGreetingFromDB() {
        Config c = ConfigProvider.getConfig();
        String message = c.getValue("greeting.message", String.class);
        Assertions.assertEquals("hello from default table", message);
    }
}
