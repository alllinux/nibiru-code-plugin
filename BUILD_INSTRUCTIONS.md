# Build Instructions

## Building the Plugin

### Prerequisites
- JDK 17 or higher (tested with JDK 17, 21, and 23)
- Gradle 8.5+ (included via Gradle wrapper)
- Internet connection (for downloading dependencies)

### Standard Build Process

```bash
# Build the plugin
./gradlew buildPlugin

# The plugin will be generated in:
# build/distributions/nibiru-code-plugin-1.0.0.zip
```

### Running in Development Mode

```bash
# Run the plugin in a test IDE instance
./gradlew runIde
```

### Building Without Daemon

If you're having memory issues, you can build without the Gradle daemon:

```bash
./gradlew buildPlugin --no-daemon
```

## Troubleshooting

### Network/Proxy Issues

If you're in a restricted network environment where the Gradle Plugin Portal is blocked:

1. **Use a different network**: Try building on a machine with unrestricted internet access
2. **Use a VPN**: Connect through a VPN that allows access to gradle.org domains
3. **Contact your network administrator**: Request access to the following domains:
   - `plugins.gradle.org`
   - `services.gradle.org`
   - `repo.maven.apache.org`
   - `plugins.jetbrains.com`

### Java Version Issues

The Gradle wrapper is configured for Gradle 8.5, which supports:
- Java 8 through Java 23

If you're using Java 23 and see the error `Could not determine java version from '23.0.2'`:
- This has been fixed in the current Gradle wrapper configuration
- Make sure you're using the gradlew script, not a system-wide Gradle installation

### Clean Build

If you encounter build issues, try a clean build:

```bash
./gradlew clean buildPlugin
```

## IDE-Specific Notes

### IntelliJ IDEA Ultimate / PHPStorm

The plugin requires IntelliJ Platform 2024.1 or higher. Lower versions are not supported.

### Plugin Compatibility

This plugin is configured for:
- **Since Build**: 241 (IntelliJ 2024.1)
- **Until Build**: 243.* (IntelliJ 2024.3.*)

## Build Output

After a successful build, you'll find:

```
build/
├── distributions/
│   └── nibiru-code-plugin-1.0.0.zip  ← Install this
├── libs/
└── ...
```

## Installing the Built Plugin

1. Open IntelliJ IDEA Ultimate or PHPStorm
2. Go to `Settings/Preferences` → `Plugins`
3. Click the gear icon ⚙️ → `Install Plugin from Disk...`
4. Navigate to `build/distributions/nibiru-code-plugin-1.0.0.zip`
5. Click OK and restart the IDE

## Development Tips

### Faster Builds

For faster incremental builds during development:

```bash
# Enable Gradle daemon (default)
./gradlew buildPlugin

# Use parallel builds
./gradlew buildPlugin --parallel
```

### Logging

To see detailed build logs:

```bash
./gradlew buildPlugin --info
./gradlew buildPlugin --debug  # Very verbose
```

### Testing Changes

Instead of building and installing each time, use:

```bash
./gradlew runIde
```

This will start a sandbox IDE with your plugin pre-installed.

## GitHub Actions / CI

For automated builds in GitHub Actions or other CI systems, ensure:

1. JDK 17+ is installed
2. The gradlew script has execute permissions: `chmod +x gradlew`
3. Network access to Gradle and Maven repositories

Example GitHub Actions workflow:

```yaml
name: Build Plugin

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Build Plugin
        run: ./gradlew buildPlugin
      - uses: actions/upload-artifact@v3
        with:
          name: plugin
          path: build/distributions/*.zip
```

## Support

If you continue to experience build issues:
1. Check the Gradle build logs for specific error messages
2. Verify your network can access gradle.org and maven.org
3. Ensure you're using JDK 17 or higher
4. Try building on a different machine/network

For plugin-specific issues, please open an issue on GitHub.
