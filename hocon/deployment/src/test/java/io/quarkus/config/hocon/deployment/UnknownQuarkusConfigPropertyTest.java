package io.quarkus.config.hocon.deployment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class UnknownQuarkusConfigPropertyTest {
    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar.addAsResource(
                    new StringAsset("quarkus.a.property=INFO\nquarkus.log { level: INFO }\n"),
                    "application.conf"))
            .setLogRecordPredicate(record -> record.getLevel().intValue() >= Level.WARNING.intValue())
            .assertLogRecords(logRecords -> assertTrue(logRecords.stream()
                    .map(LogRecord::getMessage)
                    .anyMatch(message -> message.startsWith("Unrecognized configuration key"))));

    @Test
    void unknownConfig() {

    }
}
