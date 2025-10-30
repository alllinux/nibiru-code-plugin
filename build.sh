#!/bin/bash

# Nibiru Plugin Build Script
# Uses system Gradle to avoid network download issues

echo "==================================="
echo "Nibiru Plugin - Build Script"
echo "==================================="
echo ""

# Check if system Gradle exists
if ! command -v gradle &> /dev/null; then
    echo "❌ Error: System Gradle not found"
    echo ""
    echo "Please install Gradle 8+ first:"
    echo "  https://gradle.org/install/"
    exit 1
fi

# Check Gradle version
GRADLE_VERSION=$(gradle --version | grep "Gradle" | awk '{print $2}')
MAJOR_VERSION=$(echo $GRADLE_VERSION | cut -d. -f1)

echo "✓ Found system Gradle: $GRADLE_VERSION"
echo ""

if [ "$MAJOR_VERSION" -lt 8 ]; then
    echo "❌ Error: Gradle $GRADLE_VERSION is too old"
    echo "   Required: Gradle 8.0 or higher"
    echo ""
    echo "To upgrade Gradle, run:"
    echo "  ./upgrade-gradle.sh"
    echo ""
    echo "Or install manually from: https://gradle.org/install/"
    exit 1
fi

# Check Java version
echo "Java version:"
java -version
echo ""

# Build the plugin using system Gradle
echo "Building plugin with system Gradle..."
echo "Command: gradle buildPlugin --no-daemon"
echo ""

gradle buildPlugin --no-daemon

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "✓✓✓ BUILD SUCCESSFUL! ✓✓✓"
    echo ""
    echo "Plugin location:"
    ls -lh build/distributions/*.zip 2>/dev/null
    echo ""
    echo "To install:"
    echo "1. Open IntelliJ IDEA or PHPStorm"
    echo "2. Go to Settings → Plugins → ⚙️ → Install Plugin from Disk..."
    echo "3. Select: build/distributions/nibiru-code-plugin-1.0.0.zip"
    echo "4. Restart IDE"
else
    echo ""
    echo "❌ BUILD FAILED"
    echo ""
    echo "Try running with more details:"
    echo "  gradle buildPlugin --stacktrace"
    exit 1
fi

echo ""
echo "==================================="
