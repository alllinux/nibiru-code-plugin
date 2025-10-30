# Quick Fix for Build Issues

## Common Problems

### Problem 1: "Could not determine java version from '23.0.2'"
Old Gradle 4.4.1 wrapper cached on your system doesn't support Java 23.

### Problem 2: "Downloading gradle-8.5-bin.zip" times out
Network restrictions blocking access to services.gradle.org.

### Problem 3: "Task 'buildPlugin' not found"
Your system Gradle is too old (4.4.1). The IntelliJ Platform Gradle Plugin requires Gradle 8.0+.

## ⭐ RECOMMENDED SOLUTION: Upgrade System Gradle

If you have Gradle 4.x or older installed:

```bash
# Automated upgrade to Gradle 8.5
./upgrade-gradle.sh

# Then build the plugin
gradle buildPlugin
```

**Or** install manually from [gradle.org/install](https://gradle.org/install/)

---

## Alternative Solution: Use System Gradle (Network Issues)

If Gradle wrapper download times out but you already have Gradle 8+:

```bash
# Check your Gradle version first
gradle --version

# If it's 8.0+, use the build script
./build.sh
```

**This works immediately with no downloads needed!**

---

## Other Solutions: Choose ONE of the following methods

### Method 1: Delete Old Gradle Cache (Recommended)

```bash
# Delete the old Gradle 4.4.1 cache
rm -rf ~/.gradle/wrapper/dists/gradle-4.4.1-bin

# Now try building again
./gradlew buildPlugin
```

The wrapper will download Gradle 8.5 which supports Java 23.

### Method 2: Use the Fix Script (Automated)

```bash
# Run the automated fix script
./fix-gradle.sh

# Then build
./gradlew buildPlugin
```

### Method 3: Use System Gradle Directly

If you have Gradle 8+ installed system-wide:

```bash
# Check your Gradle version
gradle --version

# If it's 8.0 or higher, use it directly
gradle buildPlugin

# Or regenerate the wrapper with system Gradle
gradle wrapper --gradle-version=8.5
./gradlew buildPlugin
```

### Method 4: Regenerate Wrapper Manually

```bash
# Remove current wrapper
rm -rf gradle/wrapper
rm gradlew gradlew.bat

# Install Gradle 8+ if you don't have it
# Then run:
gradle wrapper --gradle-version=8.5 --distribution-type=bin

# Test it
./gradlew --version

# Build the plugin
./gradlew buildPlugin
```

## Verification

After applying the fix, verify it works:

```bash
./gradlew --version
```

You should see:
```
Gradle 8.5 (or higher)
```

Not:
```
Gradle 4.4.1
```

## Build the Plugin

Once the wrapper is fixed:

```bash
./gradlew buildPlugin
```

The plugin will be created at:
```
build/distributions/nibiru-code-plugin-1.0.0.zip
```

## Still Having Issues?

### Network Restrictions

If you can't download Gradle 8.5 due to network restrictions:

1. Download Gradle 8.5 on a different machine:
   - https://services.gradle.org/distributions/gradle-8.5-bin.zip

2. Copy it to: `~/.gradle/wrapper/dists/gradle-8.5-bin/[hash]/`

3. Or use system Gradle directly: `gradle buildPlugin`

### Java Version Issues

If you have multiple Java versions:

```bash
# Check current Java
java -version

# Set Java 17, 21, or 23
export JAVA_HOME=/path/to/jdk-17-or-higher
export PATH=$JAVA_HOME/bin:$PATH

# Verify
java -version

# Build
./gradlew buildPlugin
```

## Why This Happens

The error occurs because:
1. Your system has a cached Gradle 4.4.1 distribution in `~/.gradle/wrapper/dists/`
2. Gradle 4.4.1 was released before Java 23 existed
3. The old wrapper can't parse Java 23's version string
4. Solution: Use Gradle 8+ which supports Java 8-23

## Prevention

To prevent this in the future:
- Periodically clean old Gradle caches: `rm -rf ~/.gradle/wrapper/dists/gradle-4.*`
- Keep system Gradle updated: `gradle wrapper --gradle-version=latest`
- Use Java 17 or 21 (LTS versions) instead of Java 23 if possible
