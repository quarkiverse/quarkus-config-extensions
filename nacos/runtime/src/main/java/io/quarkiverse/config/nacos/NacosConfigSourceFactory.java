package io.quarkiverse.config.nacos;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class NacosConfigSourceFactory implements ConfigSourceFactory {

    // Can't use ConfigSourceFactory.ConfigurableConfigSourceFactory because of unmapped build time config
    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        List<String> profiles = new ArrayList<>(context.getProfiles());
        Collections.reverse(profiles);

        SmallRyeConfig config = new SmallRyeConfigBuilder()
                .withProfiles(profiles)
                .withSources(new ConfigSourceContext.ConfigSourceContextConfigSource(context))
                .withSources(context.getConfigSources())
                .withMapping(NacosConfig.class)
                .withMappingIgnore("quarkus.config.source.nacos.**")
                .build();

        return getConfigSources(config.getConfigMapping(NacosConfig.class));
    }

    private Iterable<ConfigSource> getConfigSources(NacosConfig config) {
        Vertx vertx = null;
        WebClient webClient = null;
        try {
            vertx = Vertx.vertx(new VertxOptions());
            webClient = WebClient.create(vertx, new WebClientOptions()
                    .setDefaultHost(config.serverAddr().getHostName())
                    .setDefaultPort(config.serverAddr().getPort())
                    .setFollowRedirects(true)
                    .setConnectTimeout((int) Duration.of(60, SECONDS).toMillis()));

            Optional<String> accessToken = accessToken(config, webClient);
            HttpRequest<Buffer> request = webClient.get("/nacos/v3/client/cs/config")
                    .addQueryParam("namespaceId", config.namespace())
                    .addQueryParam("groupName", config.group())
                    .addQueryParam("dataId", config.dataId());
            accessToken.ifPresent(s -> request.addQueryParam("accessToken", s));

            HttpResponse<Buffer> response = request
                    .send()
                    .toCompletionStage()
                    .toCompletableFuture()
                    .get(30, TimeUnit.SECONDS);

            JsonObject json = response.bodyAsJsonObject();
            if (response.statusCode() == 200) {
                int code = json.getInteger("code");
                if (code > 0) {
                    throw new RuntimeException(response.bodyAsString());
                }

                JsonObject data = json.getJsonObject("data");
                String contentType = data.getString("contentType");
                if ("properties".equals(contentType)) {
                    Properties properties = properties(data);
                    return !properties.isEmpty() ? List.of(new PropertiesConfigSource(properties, "NacosConfigSource", 270))
                            : List.of();
                } else {
                    throw new IllegalArgumentException("Configuration format not supported: " + contentType);
                }
            } else {
                throw new RuntimeException(response.bodyAsString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (webClient != null) {
                webClient.close();
            }
            if (vertx != null) {
                vertx.close();
            }
        }
    }

    private static Optional<String> accessToken(NacosConfig config, WebClient webClient) throws Exception {
        if (config.username().isEmpty() && config.password().isEmpty()) {
            return Optional.empty();
        }

        HttpResponse<Buffer> response = webClient.post("/nacos/v3/auth/user/login")
                .sendForm(MultiMap.caseInsensitiveMultiMap()
                        .add("username", config.username().get())
                        .add("password", config.password().get()))
                .toCompletionStage()
                .toCompletableFuture()
                .get(30, TimeUnit.SECONDS);

        if (response.statusCode() == 200) {
            JsonObject json = response.bodyAsJsonObject();
            String accessToken = json.getString("accessToken");
            return Optional.of(accessToken);
        } else if (response.statusCode() >= 400) {
            throw new RuntimeException(response.bodyAsString());
        }
        return Optional.empty();
    }

    private static Properties properties(JsonObject data) throws IOException {
        Properties properties = new Properties();
        String content = data.getString("content");
        if (content != null && !content.isEmpty()) {
            properties.load(new StringReader(content));
        }
        return properties;
    }
}
