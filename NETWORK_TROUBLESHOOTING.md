# Network Troubleshooting Guide

## Build Error: Timeout or Connection Issues

If you're seeing errors like:
```
Could not resolve org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.9.21
Connect to XXX.XXX.XXX.XXX:XXXXX failed: Connect timed out
```

This is a network connectivity issue between your build system and the required repositories.

## Required Internet Access

The build needs to download dependencies from:
1. **plugins.gradle.org** - Gradle plugins (Kotlin, IntelliJ Platform)
2. **repo1.maven.org** - Maven Central (dependencies)
3. **cache-redirector.jetbrains.com** - IntelliJ Platform SDK

## Solutions

### Solution 1: Build on a Machine with Internet Access (Recommended)

If you're in a corporate network with restrictions:

```bash
# On a machine with unrestricted internet
git clone https://github.com/alllinux/nibiru-code-plugin.git
cd nibiru-code-plugin
gradle buildPlugin

# Transfer the built plugin
scp build/distributions/nibiru-code-plugin-1.0.0.zip your-machine:~/
```

### Solution 2: Configure Corporate Proxy

If your company uses a proxy server:

Add to `~/.gradle/gradle.properties`:
```properties
systemProp.http.proxyHost=your-proxy-host
systemProp.http.proxyPort=your-proxy-port
systemProp.https.proxyHost=your-proxy-host
systemProp.https.proxyPort=your-proxy-port

# If proxy requires authentication
systemProp.http.proxyUser=username
systemProp.http.proxyPassword=password
systemProp.https.proxyUser=username
systemProp.https.proxyPassword=password

# If some hosts don't need proxy
systemProp.http.nonProxyHosts=*.internal.com|localhost
```

Then build:
```bash
gradle buildPlugin
```

### Solution 3: Request Network Access

Ask your IT/network administrator to whitelist these domains:
- `*.gradle.org`
- `*.maven.org`
- `*.jetbrains.com`
- `*.jcenter.bintray.com` (legacy, may still be needed)

### Solution 4: Use Dependency Cache

If you have a machine that already successfully built the project:

```bash
# On machine that built successfully, package the Gradle cache
cd ~
tar -czf gradle-cache.tar.gz .gradle/caches

# Transfer to restricted machine
scp gradle-cache.tar.gz restricted-machine:~/

# On restricted machine, extract
tar -xzf gradle-cache.tar.gz -C ~/

# Now try building
cd nibiru-code-plugin
gradle buildPlugin --offline
```

### Solution 5: Use a Gradle Repository Mirror

If you have access to an internal repository mirror (Nexus, Artifactory):

Create/edit `~/.gradle/init.gradle`:
```groovy
allprojects {
    buildscript {
        repositories {
            maven { url "https://your-internal-mirror/maven-public/" }
        }
    }

    repositories {
        maven { url "https://your-internal-mirror/maven-public/" }
    }
}
```

## Verification

Test connectivity before building:

```bash
# Test Gradle Plugin Portal
curl -I https://plugins.gradle.org

# Test Maven Central
curl -I https://repo1.maven.org

# Test JetBrains
curl -I https://cache-redirector.jetbrains.com
```

All should return `200 OK` or `301/302 Redirect`, not `403 Forbidden` or timeout.

## Common Error Messages

### "Connect timed out"
- **Cause**: Cannot reach repository servers
- **Solution**: Check firewall/proxy settings, try Solutions 1-3

### "403 Forbidden"
- **Cause**: Access blocked by network policy
- **Solution**: Request access from IT (Solution 3) or use mirror (Solution 5)

### "UnknownHostException"
- **Cause**: DNS resolution failure
- **Solution**: Check DNS settings, verify internet connection

### "Could not determine java version from 'XX.X.X'"
- **Cause**: Old Gradle wrapper cached
- **Solution**: See QUICK_FIX.md

## Still Having Issues?

### Check Your Network
```bash
# Test basic connectivity
ping 8.8.8.8

# Test DNS
nslookup plugins.gradle.org

# Test HTTPS
curl -v https://plugins.gradle.org
```

### Check Gradle Configuration
```bash
# See what Gradle is doing
gradle buildPlugin --debug > build-debug.log 2>&1

# Search for connection issues
grep -i "connect\|timeout\|403\|failed" build-debug.log
```

### Alternative: Pre-built Plugin

If you absolutely cannot build due to network restrictions, you can:
1. Request someone else to build it
2. Use GitHub Actions to build (push to GitHub, let Actions build it)
3. Download a pre-built release (if available)

## GitHub Actions Build (No Local Network Issues)

Create `.github/workflows/build.yml`:
```yaml
name: Build Plugin
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Build
        run: gradle buildPlugin
      - uses: actions/upload-artifact@v3
        with:
          name: plugin
          path: build/distributions/*.zip
```

Push to GitHub, and GitHub Actions will build it with full internet access. Download the artifact from the Actions tab.
