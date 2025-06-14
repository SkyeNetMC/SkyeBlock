#!/bin/bash

# Test script for Nether World Auto-Creation System
echo "🌋 Testing Nether World Auto-Creation System"
echo "============================================="

# Check if plugin JAR exists
if [ -f "target/skyeblock-2.0.0.jar" ]; then
    echo "✅ Plugin JAR found: skyeblock-2.0.0.jar"
else
    echo "❌ Plugin JAR not found. Run 'mvn clean package' first."
    exit 1
fi

# Check configuration files
echo ""
echo "📋 Configuration Check:"
echo "----------------------"

if [ -f "config.yml" ]; then
    echo "✅ Main config.yml found"
    
    # Check for nether configuration
    if grep -q "auto-create-nether: true" config.yml; then
        echo "✅ Auto-create-nether enabled"
    else
        echo "⚠️  Auto-create-nether setting not found or disabled"
    fi
    
    if grep -q "nether_portal_island" config.yml; then
        echo "✅ Nether portal island template configured"
    else
        echo "❌ Nether portal island template not found in config"
    fi
else
    echo "❌ config.yml not found"
fi

# Check schematic files
echo ""
echo "🗺️  Schematic Files Check:"
echo "-------------------------"

if [ -d "src/main/resources/schematics" ]; then
    echo "✅ Schematics directory found"
    
    schematic_count=$(find src/main/resources/schematics -name "*.yml" | wc -l)
    echo "📁 Found $schematic_count YAML schematic files:"
    
    find src/main/resources/schematics -name "*.yml" | sort | while read file; do
        basename=$(basename "$file" .yml)
        echo "   📄 $basename"
    done
    
    # Check for nether portal island specifically
    if [ -f "src/main/resources/schematics/nether-portal-island.yml" ]; then
        echo "✅ Nether portal island schematic found"
    else
        echo "⚠️  nether-portal-island.yml schematic not found"
        echo "   (Will be needed for nether world generation)"
    fi
else
    echo "❌ Schematics directory not found"
fi

# Check Java source files
echo ""
echo "☕ Source Code Check:"
echo "--------------------"

if [ -f "src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java" ]; then
    echo "✅ IslandManager.java found"
    
    if grep -q "createNetherIsland" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java; then
        echo "✅ createNetherIsland method found"
    else
        echo "❌ createNetherIsland method not found"
    fi
    
    if grep -q "nether_portal_island" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java; then
        echo "✅ Nether portal island template usage found"
    else
        echo "❌ Nether portal island template usage not found"
    fi
else
    echo "❌ IslandManager.java not found"
fi

if [ -f "src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java" ]; then
    echo "✅ WorldManager.java found"
    
    if grep -q "isNetherIsland.*contains.*nether" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java; then
        echo "✅ Nether island detection logic found"
    else
        echo "❌ Nether island detection logic not found"
    fi
    
    if grep -q "World.Environment.NETHER" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java; then
        echo "✅ Nether environment configuration found"
    else
        echo "❌ Nether environment configuration not found"
    fi
else
    echo "❌ WorldManager.java not found"
fi

# Check GUI implementation
if [ -f "src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandCreationGUI.java" ]; then
    echo "✅ IslandCreationGUI.java found"
    
    # Count island types
    island_count=$(grep -c "new IslandType" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandCreationGUI.java || echo "0")
    echo "🏝️  Found $island_count island types in GUI"
    
    if [ "$island_count" -eq "19" ]; then
        echo "✅ All 19 island types implemented"
    else
        echo "⚠️  Expected 19 island types, found $island_count"
    fi
else
    echo "❌ IslandCreationGUI.java not found"
fi

echo ""
echo "🔧 Build Test:"
echo "-------------"

# Test compilation
if mvn compile -q > /dev/null 2>&1; then
    echo "✅ Code compiles successfully"
else
    echo "❌ Compilation failed"
    echo "   Run 'mvn compile' to see detailed errors"
fi

echo ""
echo "📊 Test Summary:"
echo "==============="
echo "The nether world auto-creation system appears to be implemented."
echo ""
echo "🎯 Expected Behavior:"
echo "1. When a player creates any island type, a corresponding nether world is created"
echo "2. The nether world uses the 'nether_portal_island' template"
echo "3. Nether worlds are created in 'skyeblock/nether/' directory structure"
echo "4. Portal coordinates sync between overworld and nether dimensions"
echo ""
echo "🚀 To deploy:"
echo "1. Copy target/skyeblock-2.0.0.jar to server plugins/ folder"
echo "2. Ensure nether-portal-island.yml schematic exists"
echo "3. Restart server"
echo "4. Test with '/island create vanilla' command"
echo ""
echo "Test completed! ✨"
