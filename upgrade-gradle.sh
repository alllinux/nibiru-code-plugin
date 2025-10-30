#!/bin/bash

# Gradle Upgrade Script for Nibiru Plugin
# Upgrades system Gradle to version 8.5

set -e

GRADLE_VERSION="8.5"
INSTALL_DIR="$HOME/.local/gradle"
GRADLE_HOME="$INSTALL_DIR/gradle-$GRADLE_VERSION"

echo "==========================================="
echo "Gradle Upgrade Script"
echo "==========================================="
echo ""

# Check current Gradle
if command -v gradle &> /dev/null; then
    CURRENT_VERSION=$(gradle --version | grep "Gradle" | awk '{print $2}')
    echo "Current Gradle version: $CURRENT_VERSION"

    MAJOR_VERSION=$(echo $CURRENT_VERSION | cut -d. -f1)
    if [ "$MAJOR_VERSION" -ge 8 ]; then
        echo "✓ Gradle $CURRENT_VERSION is sufficient (8.0+ required)"
        echo ""
        echo "You can build the plugin with:"
        echo "  gradle buildPlugin"
        exit 0
    fi

    echo "⚠️  Gradle $CURRENT_VERSION is too old (need 8.0+)"
    echo ""
else
    echo "No system Gradle found"
    echo ""
fi

echo "This script will install Gradle $GRADLE_VERSION to:"
echo "  $GRADLE_HOME"
echo ""

read -p "Continue with installation? (y/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Installation cancelled"
    exit 0
fi

echo ""
echo "Step 1: Creating installation directory..."
mkdir -p "$INSTALL_DIR"

echo "Step 2: Downloading Gradle $GRADLE_VERSION..."
cd "$INSTALL_DIR"

# Download Gradle (try multiple methods)
GRADLE_ZIP="gradle-$GRADLE_VERSION-bin.zip"
GRADLE_URL="https://services.gradle.org/distributions/$GRADLE_ZIP"

if command -v wget &> /dev/null; then
    wget -O "$GRADLE_ZIP" "$GRADLE_URL"
elif command -v curl &> /dev/null; then
    curl -L -o "$GRADLE_ZIP" "$GRADLE_URL"
else
    echo "❌ Error: Neither wget nor curl found"
    echo ""
    echo "Please install wget or curl:"
    echo "  sudo apt-get install wget"
    echo "  # or"
    echo "  sudo apt-get install curl"
    exit 1
fi

echo "Step 3: Extracting Gradle..."
if command -v unzip &> /dev/null; then
    unzip -q "$GRADLE_ZIP"
    rm "$GRADLE_ZIP"
else
    echo "❌ Error: unzip command not found"
    echo ""
    echo "Please install unzip:"
    echo "  sudo apt-get install unzip"
    exit 1
fi

echo "Step 4: Setting up environment..."
GRADLE_BIN="$GRADLE_HOME/bin/gradle"

if [ ! -f "$GRADLE_BIN" ]; then
    echo "❌ Error: Gradle binary not found at $GRADLE_BIN"
    exit 1
fi

# Add to PATH
SHELL_RC=""
if [ -f "$HOME/.bashrc" ]; then
    SHELL_RC="$HOME/.bashrc"
elif [ -f "$HOME/.zshrc" ]; then
    SHELL_RC="$HOME/.zshrc"
fi

if [ -n "$SHELL_RC" ]; then
    # Check if already added
    if ! grep -q "GRADLE_HOME.*$GRADLE_VERSION" "$SHELL_RC"; then
        echo "" >> "$SHELL_RC"
        echo "# Gradle $GRADLE_VERSION (added by Nibiru plugin build script)" >> "$SHELL_RC"
        echo "export GRADLE_HOME=\"$GRADLE_HOME\"" >> "$SHELL_RC"
        echo "export PATH=\"\$GRADLE_HOME/bin:\$PATH\"" >> "$SHELL_RC"
        echo "✓ Added Gradle to $SHELL_RC"
    else
        echo "✓ Gradle already configured in $SHELL_RC"
    fi
fi

# Make current session aware
export GRADLE_HOME="$GRADLE_HOME"
export PATH="$GRADLE_HOME/bin:$PATH"

echo ""
echo "=========================================="
echo "✓✓✓ Gradle $GRADLE_VERSION installed! ✓✓✓"
echo "=========================================="
echo ""
echo "Installation location:"
echo "  $GRADLE_HOME"
echo ""

# Verify installation
if command -v gradle &> /dev/null; then
    INSTALLED_VERSION=$(gradle --version | grep "Gradle" | awk '{print $2}')
    echo "Gradle version: $INSTALLED_VERSION"
    echo ""
fi

echo "To use Gradle in your current terminal:"
echo "  export PATH=\"$GRADLE_HOME/bin:\$PATH\""
echo ""
echo "For new terminals, it's already configured in $SHELL_RC"
echo ""
echo "Now you can build the Nibiru plugin:"
echo "  cd $(pwd)"
echo "  gradle buildPlugin"
echo ""
echo "=========================================="
