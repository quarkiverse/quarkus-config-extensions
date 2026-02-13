package io.quarkiverse.config.jdbc.extensions.test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import jakarta.inject.Inject;

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
public class JdbcConfigJsonValuesTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(() -> ShrinkWrap
            .create(JavaArchive.class).addAsResource("json_application.properties", "application.properties"));

    @Inject
    DataSource dataSource;

    @Test
    @DisplayName("Reads a json property from json DB")
    public void readGreetingFromDB() throws SQLException {
        Config c = ConfigProvider.getConfig();
        // DB has key "greeting" with JSON value {"message":"hello from json table"}
        // JSON flattening exposes it as "greeting.message"
        String message = c.getValue("greeting", String.class);
        Assertions.assertEquals("{\"message\":\"hello from json table\"}", message);

        // Update the JSON value for key "greeting" in the database
        int result = updateConfigValue("greeting", "{\"message\":\"updated hello from json table\"}");
        Assertions.assertTrue(result > 0);
        // assert we are getting the updated message because cache is disabled
        message = c.getValue("greeting", String.class);
        Assertions.assertEquals("{\"message\":\"updated hello from json table\"}", message);
    }

    private int updateConfigValue(String key, String value) throws SQLException {
        PreparedStatement updateStatement = dataSource.getConnection()
                .prepareStatement("UPDATE json_config c SET c.b = ? WHERE c.a = ?");
        updateStatement.setString(1, value);
        updateStatement.setString(2, key);
        return updateStatement.executeUpdate();
    }
}
