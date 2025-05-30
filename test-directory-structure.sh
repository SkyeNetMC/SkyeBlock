#!/bin/bash

# Test script to verify the new directory structure implementation
# This script tests the WorldManager directory structure changes

echo "=== SkyeBlock Directory Structure Test ==="
echo "Testing WorldManager directory structure changes..."
echo

# Check if the plugin was built successfully
if [ ! -f "target/skyeblock-1.0.0.jar" ]; then
    echo "❌ Plugin JAR not found! Please build the plugin first."
    exit 1
fi

echo "✅ Plugin JAR found: target/skyeblock-1.0.0.jar"
echo

# Check WorldManager.java for correct directory structure
echo "🔍 Checking WorldManager.java for directory structure updates..."

# Check for new islands directory structure
if grep -q "skyeblock/islands/" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java; then
    echo "✅ Found 'skyeblock/islands/' directory structure for normal islands"
else
    echo "❌ Missing 'skyeblock/islands/' directory structure"
fi

# Check for nether directory structure
if grep -q "skyeblock/nether/" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java; then
    echo "✅ Found 'skyeblock/nether/' directory structure for nether islands"
else
    echo "❌ Missing 'skyeblock/nether/' directory structure"
fi

# Check for SlimeWorld naming convention updates
if grep -q "skyeblock_islands_" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java; then
    echo "✅ Found 'skyeblock_islands_' SlimeWorld naming for normal islands"
else
    echo "❌ Missing 'skyeblock_islands_' SlimeWorld naming"
fi

if grep -q "skyeblock_nether_" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java; then
    echo "✅ Found 'skyeblock_nether_' SlimeWorld naming for nether islands"
else
    echo "❌ Missing 'skyeblock_nether_' SlimeWorld naming"
fi

# Check that old 'overworld' references are removed
if grep -q "skyeblock_overworld_" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java; then
    echo "❌ Found old 'skyeblock_overworld_' references - should be updated to 'skyeblock_islands_'"
else
    echo "✅ No old 'skyeblock_overworld_' references found"
fi

if grep -q "skyeblock/overworld/" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java; then
    echo "❌ Found old 'skyeblock/overworld/' references - should be updated to 'skyeblock/islands/'"
else
    echo "✅ No old 'skyeblock/overworld/' directory references found"
fi

echo
echo "🔍 Checking for nether environment settings..."

# Check that nether islands get NETHER environment
if grep -A5 -B5 "Environment.NETHER" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java | grep -q "isNetherIsland"; then
    echo "✅ Found nether environment configuration for nether islands"
else
    echo "❌ Missing nether environment configuration"
fi

echo
echo "📋 Summary of Directory Structure Changes:"
echo "  Normal Islands: root/skyeblock/islands/"
echo "  Nether Islands: root/skyeblock/nether/"
echo "  SlimeWorld Normal: skyeblock_islands_<id>"
echo "  SlimeWorld Nether: skyeblock_nether_<id>"
echo

echo "=== Test Complete ==="
echo "Review the output above to ensure all directory structure changes are correct."
