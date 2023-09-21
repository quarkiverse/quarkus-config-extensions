package io.quarkus.consul.config.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.logging.Logger;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAwait;

class ConsulConfigSourceFactory implements ConfigSourceFactory.ConfigurableConfigSourceFactory<ConsulConfig> {
    private static final Logger log = Logger.getLogger(ConsulConfigSourceFactory.class);

    @Override
    public Iterable<ConfigSource> getConfigSources(final ConfigSourceContext context, final ConsulConfig config) {
        if (!config.enabled()) {
            return Collections.emptyList();
        }

        return getConfigSources(config, new VertxConsulConfigGateway(config));
    }

    Iterable<ConfigSource> getConfigSources(ConsulConfig config, ConsulConfigGateway consulConfigGateway) {
        Map<String, ValueType> keys = new LinkedHashMap<>();
        if (config.rawValueKeys().isPresent()) {
            for (String key : config.rawValueKeys().get()) {
                keys.put(key, ValueType.RAW);
            }
        }
        if (config.propertiesValueKeys().isPresent()) {
            for (String key : config.propertiesValueKeys().get()) {
                keys.put(key, ValueType.PROPERTIES);
            }
        }
        if (keys.isEmpty()) {
            log.debug("No keys were configured for config source lookup");
            return Collections.emptyList();
        }

        Map<String, ConfigSource> results = new LinkedHashMap<>(keys.size());

        List<Uni<?>> allUnis = new ArrayList<>();

        for (Map.Entry<String, ValueType> entry : keys.entrySet()) {
            String fullKey = config.prefix().isPresent() ? config.prefix().get() + "/" + entry.getKey() : entry.getKey();
            results.put(fullKey, null);
            allUnis.add(consulConfigGateway.getValue(fullKey).invoke(new Consumer<Response>() {
                @Override
                public void accept(Response response) {
                    if (response != null) {
                        results.put(response.getKey(),
                                ResponseConfigSourceUtil.toConfigSource(response, entry.getValue(), config.prefix()));
                    } else {
                        String message = "Key '" + fullKey + "' not found in Consul.";
                        if (config.failOnMissingKey()) {
                            throw new RuntimeException(message);
                        } else {
                            results.remove(fullKey);
                            log.info(message);
                        }
                    }
                }
            }));
        }

        try {
            UniAwait<Void> await = Uni.combine().all().unis(allUnis).discardItems().await();
            if (config.agent().connectionTimeout().isZero() && config.agent().readTimeout().isZero()) {
                await.indefinitely();
            } else {
                await.atMost(config.agent().connectionTimeout().plus(config.agent().readTimeout().multipliedBy(2)));
            }
        } catch (CompletionException e) {
            throw new RuntimeException("An error occurred while attempting to fetch configuration from Consul.", e);
        } finally {
            consulConfigGateway.close();
        }

        return results.values();
    }
}
