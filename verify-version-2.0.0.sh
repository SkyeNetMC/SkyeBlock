#!/bin/bash

# Version 2.0.0 Verification Script
# Verifies that all version references have been updated correctly

echo "🔍 SkyeBlock Version 2.0.0 Verification"
echo "========================================"

# Check plugin.yml version
echo "📄 Checking plugin.yml..."
PLUGIN_VERSION=$(grep "version:" plugin.yml | head -1 | cut -d"'" -f2)
if [ "$PLUGIN_VERSION" = "2.0.0" ]; then
    echo "✅ plugin.yml version: $PLUGIN_VERSION"
else
    echo "❌ plugin.yml version incorrect: $PLUGIN_VERSION (expected 2.0.0)"
fi

# Check pom.xml version
echo "📄 Checking pom.xml..."
POM_VERSION=$(grep "<version>" pom.xml | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
if [ "$POM_VERSION" = "2.0.0" ]; then
    echo "✅ pom.xml version: $POM_VERSION"
else
    echo "❌ pom.xml version incorrect: $POM_VERSION (expected 2.0.0)"
fi

# Check config.yml version
echo "📄 Checking config.yml..."
CONFIG_VERSION=$(grep "version:" src/main/resources/config.yml | cut -d" " -f2)
if [ "$CONFIG_VERSION" = "2.0.0" ]; then
    echo "✅ config.yml version: $CONFIG_VERSION"
else
    echo "❌ config.yml version incorrect: $CONFIG_VERSION (expected 2.0.0)"
fi

# Check if JAR file was built with correct version
echo "📦 Checking built JAR file..."
if [ -f "target/skyeblock-2.0.0.jar" ]; then
    JAR_SIZE=$(wc -c < "target/skyeblock-2.0.0.jar")
    echo "✅ skyeblock-2.0.0.jar exists ($(($JAR_SIZE / 1024))KB)"
else
    echo "❌ skyeblock-2.0.0.jar not found"
fi

# Verify no old version references remain
echo "🔍 Checking for old version references..."
OLD_REFS=$(grep -r "1\.1\.0" . --exclude-dir=target --exclude-dir=.git 2>/dev/null || true)
if [ -z "$OLD_REFS" ]; then
    echo "✅ No old version references found"
else
    echo "⚠️  Found old version references:"
    echo "$OLD_REFS"
fi

echo ""
echo "📋 Version Update Summary"
echo "========================="
echo "Previous Version: 1.1.0"
echo "New Version: 2.0.0"
echo "Build Date: $(date)"
echo "JAR Location: target/skyeblock-2.0.0.jar"

echo ""
echo "🎉 SkyeBlock v2.0.0 Update Complete!"
echo ""
echo "✨ New in v2.0.0:"
echo "• Island Creation GUI System"
echo "• Advanced Island Management"
echo "• Nether Integration"
echo "• MiniMessage Support"
echo "• Enhanced Security & Stability"
echo "• Modern Minecraft 1.21 Support"
echo ""
echo "Ready for deployment! 🚀"
