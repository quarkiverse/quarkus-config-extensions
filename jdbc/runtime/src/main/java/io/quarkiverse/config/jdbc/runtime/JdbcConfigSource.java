package io.quarkiverse.config.jdbc.runtime;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import io.smallrye.config.common.AbstractConfigSource;

public class JdbcConfigSource extends AbstractConfigSource {

    private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(JdbcConfigSource.class);

    private final Repository repository;

    public JdbcConfigSource(String name, Repository repository, int defaultOrdinal) {
        super(name, defaultOrdinal);
        this.repository = repository;
    }

    @Override
    public Map<String, String> getProperties() {
        try {
            return repository.getAllConfigValues();
        } catch (SQLException e) {
            log.warn("jdbc-config unable to get properties: " + e.getLocalizedMessage());
            return Map.of();
        }
    }

    @Override
    public Set<String> getPropertyNames() {
        try {
            return repository.getPropertyNames();
        } catch (SQLException e) {
            log.warn("jdbc-config unable to get property names: " + e.getLocalizedMessage());
            return Set.of();
        }
    }

    @Override
    public String getValue(String propertyName) {
        try {
            return repository.getValue(propertyName);
        } catch (SQLException e) {
            return null;
        }
    }

}
