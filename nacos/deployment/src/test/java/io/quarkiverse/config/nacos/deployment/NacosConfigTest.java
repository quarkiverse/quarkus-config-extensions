package io.quarkiverse.config.nacos.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.SmallRyeConfig;

class NacosConfigTest {
    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(new StringAsset(
                            """
                                    quarkus.config.source.nacos.devservices.load-config.data-id=quarkus
                                    quarkus.config.source.nacos.devservices.load-config.group=quarkus
                                    quarkus.config.source.nacos.devservices.load-config.content=foo.bar=1234
                                    quarkus.config.source.nacos.data-id=quarkus
                                    quarkus.config.source.nacos.group=quarkus
                                    """),
                            "application.properties"));

    @Inject
    SmallRyeConfig config;

    @Test
    void nacosConfig() {
        ConfigValue configValue = config.getConfigValue("foo.bar");
        assertEquals("1234", configValue.getValue());
        assertEquals("PropertiesConfigSource[source=NacosConfigSource]", configValue.getConfigSourceName());
    }
}
