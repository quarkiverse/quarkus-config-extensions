# JSON Property Support for Quarkus Config JDBC Extension

This document describes the JSON property flattening feature added to the `quarkus-config-jdbc` extension.

## Overview

The extension now supports storing JSON values in your configuration database table. JSON objects are automatically flattened into dot-notation properties, making them accessible via the standard MicroProfile Config API.

## How It Works

When a configuration value stored in the database is a valid JSON object or array, the extension automatically flattens it into multiple configuration properties using dot notation.

### Example

**Database Entry:**

| Key | Value |
|-----|-------|
| `app.settings` | `{"database": {"host": "localhost", "port": 5432}, "cache": {"enabled": true}}` |

**Flattened Properties:**

- `app.settings.database.host` → `localhost`
- `app.settings.database.port` → `5432`
- `app.settings.cache.enabled` → `true`

## Supported JSON Structures

### Objects

```json
{"message": "hello", "count": 42}
```

Flattens to:
- `<key>.message` → `hello`
- `<key>.count` → `42`

### Nested Objects

```json
{"server": {"host": "localhost", "port": 8080}}
```

Flattens to:
- `<key>.server.host` → `localhost`
- `<key>.server.port` → `8080`

### Arrays

```json
{"items": ["a", "b", "c"]}
```

Flattens to:
- `<key>.items[0]` → `a`
- `<key>.items[1]` → `b`
- `<key>.items[2]` → `c`

### Supported Value Types

- Strings
- Numbers (integers and decimals)
- Booleans
- Null values (skipped)

## Usage

### Database Setup

```sql
CREATE TABLE config (key VARCHAR(255), value VARCHAR(4096));

INSERT INTO config (key, value)
VALUES ('greeting', '{"message": "hello from json table"}');
```

### Application Properties

```properties
quarkus.config.source.jdbc.table=config
quarkus.config.source.jdbc.key=key
quarkus.config.source.jdbc.value=value
quarkus.config.source.jdbc.cache=false
quarkus.config.source.jdbc.url=jdbc:postgresql://localhost:5432/mydb
quarkus.config.source.jdbc.username=user
quarkus.config.source.jdbc.password=password
```

### Accessing Properties in Code

```java
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MyService {

    @ConfigProperty(name = "greeting.message")
    String greetingMessage;

    // Or programmatically
    public String getMessage() {
        Config config = ConfigProvider.getConfig();
        return config.getValue("greeting.message", String.class);
    }
}
```

## Dependencies

This feature requires Jackson for JSON parsing. The dependency is included in the extension:

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jackson</artifactId>
</dependency>
```

## Nested POJO Mapping

This extension has been validated for use with nested Plain Old Java Objects (POJOs). The JSON flattening mechanism converts hierarchical JSON structures into dot-notation property keys, which are then correctly mapped to nested POJO fields by the MicroProfile Config implementation.

### Example: Mapping to Nested POJOs

**Database Entry:**

```json
{"server": {"host": "localhost", "port": 8080}, "credentials": {"username": "admin", "password": "secret"}}
```

**POJO Structure:**

```java
@ConfigMapping(prefix = "app")
public interface AppConfig {
    ServerConfig server();
    CredentialsConfig credentials();
}

public interface ServerConfig {
    String host();
    int port();
}

public interface CredentialsConfig {
    String username();
    String password();
}
```

The flattened properties (`app.server.host`, `app.server.port`, `app.credentials.username`, `app.credentials.password`) are automatically bound to the corresponding nested interface methods, enabling type-safe configuration access throughout the application.

## Notes

- Non-JSON values are returned as-is without any processing
- Invalid JSON is treated as a plain string value
- JSON detection is based on the value starting/ending with `{}` or `[]`
- Cache setting (`quarkus.config.source.jdbc.cache`) affects whether updated JSON values are immediately reflected
- Nested POJO mapping has been tested and verified to work correctly with multi-level JSON hierarchies