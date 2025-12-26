package io.quarkiverse.config.nacos.deployment;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.config.nacos.NacosConfigBuilder;
import io.quarkiverse.config.nacos.deployment.NacosBuildTimeConfig.DevServices;
import io.quarkiverse.config.nacos.deployment.NacosBuildTimeConfig.DevServices.LoadConfig;
import io.quarkus.deployment.IsDevServicesSupportedByLaunchMode;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigBuilderBuildItem;
import io.quarkus.deployment.builditem.Startable;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.StartableContainer;
import io.smallrye.config.SmallRyeConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

class NacosProcessor {
    private static final Logger log = Logger.getLogger(NacosProcessor.class);

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem("config-nacos");
    }

    @BuildStep
    void configBuilder(BuildProducer<RunTimeConfigBuilderBuildItem> runTimeConfigBuilder) {
        runTimeConfigBuilder.produce(new RunTimeConfigBuilderBuildItem(NacosConfigBuilder.class));
    }

    @BuildStep(onlyIf = { IsDevServicesSupportedByLaunchMode.class, DevServicesConfig.Enabled.class })
    void devService(
            NacosBuildTimeConfig nacosBuildTimeConfig,
            BuildProducer<DevServicesResultBuildItem> devServicesResult) {

        DevServices devservices = nacosBuildTimeConfig.devservices();
        if (!devservices.enabled()) {
            return;
        }

        // TODO - We shouldn't be querying the runtime config, but there is no other way to do this at the moment
        SmallRyeConfig config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
        if (config.getConfigValue("quarkus.config.source.nacos.server-addr").getValue() != null) {
            return;
        }

        DockerImageName dockerImageName = DockerImageName.parse(
                devservices.imageName().orElseGet(() -> ConfigureUtil.getDefaultImageNameFor("nacos")));
        GenericContainer<?> container = new GenericContainer<>(dockerImageName)
                .withExposedPorts(8080, 8848, 9848)
                .withEnv("MODE", "standalone")
                .withEnv("NACOS_AUTH_ENABLE", "false")
                .withEnv("NACOS_CORE_AUTH_ADMIN_ENABLED", "false")
                .withEnv("NACOS_CORE_AUTH_CONSOLE_ENABLED", "false")
                .withEnv("NACOS_AUTH_TOKEN", "SecretKey012345678901234567890123456789012345678901234567890123456789")
                .withEnv("NACOS_AUTH_IDENTITY_KEY", "serverIdentity")
                .withEnv("NACOS_AUTH_IDENTITY_VALUE", "security")
                .waitingFor(Wait.forLogMessage(".*Nacos Console started successfully.*", 1))
                .withReuse(true);

        container.start();
        log.infof("Dev Services for Nacos Server started. Console available at: http://%s:%d", container.getHost(),
                container.getMappedPort(8080));

        String serverAddr = String.format("%s:%d", container.getHost(), container.getMappedPort(8848));
        devServicesResult.produce(DevServicesResultBuildItem.owned()
                .name("Nacos Dev Service")
                .config(Map.of(
                        "quarkus.config.source.nacos.server-addr", serverAddr,
                        "quarkus.config.source.nacos.namespace", devservices.loadConfig().namespace(),
                        "quarkus.config.source.nacos.data-id", devservices.loadConfig().dataId(),
                        "quarkus.config.source.nacos.group", devservices.loadConfig().group()))
                .startable(new Supplier<Startable>() {
                    @Override
                    public Startable get() {
                        return new StartableContainer<>(container, new Function<GenericContainer<?>, String>() {
                            @Override
                            public String apply(GenericContainer<?> genericContainer) {
                                return serverAddr;
                            }
                        });
                    }
                })
                .postStartHook(new Consumer<Startable>() {
                    @Override
                    public void accept(Startable startable) {
                        LoadConfig loadConfig = devservices.loadConfig();
                        try {
                            Vertx vertx = Vertx.vertx(new VertxOptions());
                            WebClient webClient = WebClient.create(vertx, new WebClientOptions()
                                    .setDefaultHost(container.getHost())
                                    .setDefaultPort(container.getMappedPort(8080))
                                    .setFollowRedirects(true)
                                    .setConnectTimeout((int) Duration.of(60, SECONDS).toMillis()));

                            webClient.post("/v3/console/cs/config")
                                    .addQueryParam("namespaceId", loadConfig.namespace())
                                    .addQueryParam("groupName", loadConfig.group())
                                    .addQueryParam("dataId", loadConfig.dataId())
                                    .addQueryParam("content", loadConfig.content())
                                    .addQueryParam("appName", "quarkus")
                                    .addQueryParam("type", "properties")
                                    .send()
                                    .toCompletionStage()
                                    .toCompletableFuture()
                                    .get(30, TimeUnit.SECONDS);

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .build());
    }
}
