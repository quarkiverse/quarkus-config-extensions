package io.quarkus.config.hocon.runtime;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import io.smallrye.config.AbstractLocationConfigSourceLoader;
import io.smallrye.config.source.hocon.HoconConfigSource;

public class ApplicationHoconConfigSourceLoader extends AbstractLocationConfigSourceLoader {
    @Override
    protected String[] getFileExtensions() {
        return new String[] {
                "conf"
        };
    }

    @Override
    protected ConfigSource loadConfigSource(final URL url, final int ordinal) throws IOException {
        return new HoconConfigSource(url, ordinal);
    }

    public static class InClassPath extends ApplicationHoconConfigSourceLoader implements ConfigSourceProvider {
        @Override
        public List<ConfigSource> getConfigSources(final ClassLoader classLoader) {
            return new ArrayList<>(loadConfigSources("application.conf", 255, classLoader));
        }

        @Override
        protected List<ConfigSource> tryFileSystem(final URI uri, final int ordinal) {
            return new ArrayList<>();
        }
    }

    public static class InFileSystem extends ApplicationHoconConfigSourceLoader implements ConfigSourceProvider {
        @Override
        public List<ConfigSource> getConfigSources(final ClassLoader classLoader) {
            return new ArrayList<>(loadConfigSources(
                    Paths.get(System.getProperty("user.dir"), "config", "application.conf").toUri().toString(), 265,
                    classLoader));
        }

        @Override
        protected List<ConfigSource> tryClassPath(final URI uri, final int ordinal, final ClassLoader classLoader) {
            return new ArrayList<>();
        }
    }
}
