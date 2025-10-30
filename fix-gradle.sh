#!/bin/bash

# Nibiru Plugin Build Fix Script
# This script fixes Gradle wrapper issues with Java 23

echo "==================================="
echo "Nibiru Plugin - Gradle Wrapper Fix"
echo "==================================="
echo ""

# Check Java version
echo "Checking Java version..."
java -version
echo ""

# Check for old Gradle wrapper cache
echo "Checking for old Gradle wrapper caches..."
if [ -d ~/.gradle/wrapper/dists/gradle-4.4.1-bin ]; then
    echo "⚠️  Found old Gradle 4.4.1 cache"
    echo "   This version doesn't support Java 23"
    echo ""
    read -p "Delete old Gradle 4.4.1 cache? (y/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        rm -rf ~/.gradle/wrapper/dists/gradle-4.4.1-bin
        echo "✓ Deleted old Gradle 4.4.1 cache"
    fi
fi

# Check if system Gradle is available
if command -v gradle &> /dev/null; then
    GRADLE_VERSION=$(gradle --version | grep "Gradle" | awk '{print $2}')
    echo "✓ System Gradle found: $GRADLE_VERSION"
    echo ""

    # Check if system Gradle is new enough (8.0+)
    MAJOR_VERSION=$(echo $GRADLE_VERSION | cut -d. -f1)
    if [ "$MAJOR_VERSION" -ge 8 ]; then
        echo "System Gradle supports Java 23!"
        echo ""
        echo "Regenerating Gradle wrapper with system Gradle..."

        # Backup current wrapper
        if [ -f "gradlew" ]; then
            mv gradlew gradlew.bak
            mv gradlew.bat gradlew.bat.bak 2>/dev/null
            echo "✓ Backed up old wrapper files"
        fi

        # Generate new wrapper
        gradle wrapper --gradle-version=$GRADLE_VERSION --distribution-type=bin --no-daemon

        if [ $? -eq 0 ]; then
            echo "✓ Gradle wrapper regenerated successfully!"
            echo ""
            echo "Testing wrapper..."
            ./gradlew --version

            if [ $? -eq 0 ]; then
                echo ""
                echo "✓✓✓ Success! Wrapper is working."
                echo ""
                echo "Now you can build the plugin:"
                echo "  ./gradlew buildPlugin"
            else
                echo ""
                echo "⚠️  Wrapper test failed. Restoring backup..."
                mv gradlew.bak gradlew 2>/dev/null
                mv gradlew.bat.bak gradlew.bat 2>/dev/null
            fi
        else
            echo "⚠️  Failed to generate wrapper. Restoring backup..."
            mv gradlew.bak gradlew 2>/dev/null
            mv gradlew.bat.bak gradlew.bat 2>/dev/null
        fi
    else
        echo "⚠️  System Gradle $GRADLE_VERSION is too old (need 8.0+)"
        echo "   Please upgrade Gradle or use the manual fix below"
    fi
else
    echo "⚠️  No system Gradle found"
    echo ""
    echo "Manual Fix Option 1: Install Gradle 8+"
    echo "  https://gradle.org/install/"
    echo ""
    echo "Manual Fix Option 2: Delete old Gradle cache and retry"
    echo "  rm -rf ~/.gradle/wrapper/dists/gradle-4.4.1-bin"
    echo "  ./gradlew buildPlugin"
    echo ""
    echo "Manual Fix Option 3: Use system Gradle directly (if installed elsewhere)"
    echo "  /path/to/gradle buildPlugin"
fi

echo ""
echo "==================================="
