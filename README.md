# Suppository

Use your Minecraft server as your maven repository...

## Features

- maven repository manager
- ugly directory browser
- and that's about it...

## Configuration
Currently, only the port and users are configurable.

You can limit upload access to the repository by specifying
the username:password combinations in the config.

### Example configuration
```yaml
port: 6969
users:
  - 'admin:sECr3t'
  - 'gopnik:hardbass123' 
```

Users don't have different permissions right now,
but support might be added in the future.

The users can then add the specified username and secret
in their ~/.m2/settings.xml:
```xml
<settings>
    <servers>
        <server>
            <id>test-server</id>
            <username>gopnik</username>
            <password>hardbass123</password>
        </server>
    </servers>
</settings>
```

## Building

```bash
./gradlew build
```
