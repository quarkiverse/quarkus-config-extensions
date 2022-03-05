package io.quarkiverse.config.jdbc.runtime;

import java.util.Map;
import java.util.Set;

import io.smallrye.config.common.AbstractConfigSource;

public class JdbcConfigSource extends AbstractConfigSource {

    private final Repository repository;

    public JdbcConfigSource(String name, Repository repository, int defaultOrdinal) {
        super(name, defaultOrdinal);
        this.repository = repository;
    }

    @Override
    public Map<String, String> getProperties() {
        return repository.getAllConfigValues();
    }

    @Override
    public Set<String> getPropertyNames() {
        return repository.getPropertyNames();
    }

    @Override
    public String getValue(String propertyName) {
        return repository.getValue(propertyName);
    }

}
