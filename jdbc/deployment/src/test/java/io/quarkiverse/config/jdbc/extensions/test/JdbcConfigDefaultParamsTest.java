package io.quarkiverse.config.jdbc.extensions.test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

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
public class JdbcConfigDefaultParamsTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(() -> ShrinkWrap
            .create(JavaArchive.class).addAsResource("default_application.properties", "application.properties"));

    @Inject
    DataSource dataSource;

    @Test
    @DisplayName("Reads a property from custom config DB")
    public void readGreetingFromDB() throws SQLException {
        Config c = ConfigProvider.getConfig();
        String message = c.getValue("greeting.message", String.class);
        Assertions.assertEquals("hello from default table", message);

        int result = updateConfigValue("greeting.message", "updated hello from default table");
        Assertions.assertTrue(result > 0);
        // assert we are still getting the original message because cache is enabled
        message = c.getValue("greeting.message", String.class);
        Assertions.assertEquals("hello from default table", message);
    }

    private int updateConfigValue(String key, String value) throws SQLException {
        PreparedStatement updateStatement = dataSource.getConnection()
                .prepareStatement("UPDATE configuration c SET c.value = ? WHERE c.key = ?");
        updateStatement.setString(1, value);
        updateStatement.setString(2, key);
        return updateStatement.executeUpdate();
    }
}
