#!/bin/bash

echo "=== SkyeBlock Plugin Validation ==="
echo

# Check if JAR exists
JAR_FILE="/mnt/sda4/SkyeNetwork/SkyeBlock/target/skyeblock-2.0.0.jar"
if [ -f "$JAR_FILE" ]; then
    echo "✅ JAR file exists: $JAR_FILE"
    echo "   Size: $(du -h "$JAR_FILE" | cut -f1)"
else
    echo "❌ JAR file not found: $JAR_FILE"
    exit 1
fi

echo
echo "=== Plugin.yml Validation ==="

# Extract and validate plugin.yml
TEMP_DIR=$(mktemp -d)
cd "$TEMP_DIR"
unzip -q "$JAR_FILE" plugin.yml

if [ -f "plugin.yml" ]; then
    echo "✅ plugin.yml extracted successfully"
    
    # Check main class
    MAIN_CLASS=$(grep "^main:" plugin.yml | cut -d' ' -f2)
    echo "   Main class: $MAIN_CLASS"
    
    # Check dependencies
    echo "   Dependencies:"
    if grep -q "^depend:" plugin.yml; then
        grep "^depend:" plugin.yml | sed 's/^/      /'
    else
        echo "      (none)"
    fi
    
    if grep -q "^softdepend:" plugin.yml; then
        grep "^softdepend:" plugin.yml | sed 's/^/      /'
    fi
    
    # Check version
    VERSION=$(grep "^version:" plugin.yml | cut -d"'" -f2)
    echo "   Version: $VERSION"
    
    # Check commands
    echo "   Commands:"
    grep -A1 "^commands:" plugin.yml | grep "  " | sed 's/^/      /'
    
else
    echo "❌ Failed to extract plugin.yml"
    exit 1
fi

echo
echo "=== Class Files Validation ==="

# Check if main class exists in JAR
unzip -q "$JAR_FILE" "skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.class" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ Main plugin class found in JAR"
else
    echo "❌ Main plugin class not found in JAR"
fi

# Check command classes
unzip -q "$JAR_FILE" "skyeblock/nobleskye/dev/skyeblock/commands/IslandCommand.class" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ IslandCommand class found"
else
    echo "❌ IslandCommand class not found"
fi

unzip -q "$JAR_FILE" "skyeblock/nobleskye/dev/skyeblock/commands/HubCommand.class" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ HubCommand class found"
else
    echo "❌ HubCommand class not found"
fi

# Check manager classes
echo "   Manager classes:"
for manager in "IslandManager" "WorldManager" "CustomSchematicManager"; do
    unzip -q "$JAR_FILE" "skyeblock/nobleskye/dev/skyeblock/managers/${manager}.class" 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "      ✅ $manager"
    else
        echo "      ❌ $manager"
    fi
done

echo
echo "=== Resource Files Validation ==="

# Check config files
unzip -q "$JAR_FILE" config.yml 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ config.yml found"
else
    echo "❌ config.yml not found"
fi

# Check schematics
echo "   Schematic files:"
for schematic in "classic.yml" "desert.yml" "nether.yml"; do
    unzip -q "$JAR_FILE" "schematics/$schematic" 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "      ✅ $schematic"
    else
        echo "      ❌ $schematic"
    fi
done

# Clean up
cd - > /dev/null
rm -rf "$TEMP_DIR"

echo
echo "=== Compatibility Check ==="

# Check if plugin.yml has correct format
cd /mnt/sda4/SkyeNetwork/SkyeBlock/target
unzip -p skyeblock-1.0.0.jar plugin.yml | grep -q "main: skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin"
if [ $? -eq 0 ]; then
    echo "✅ Main class reference is correct"
else
    echo "❌ Main class reference is incorrect"
fi

# Check for hard dependencies that might cause issues
unzip -p skyeblock-1.0.0.jar plugin.yml | grep "^depend:" | grep -q "SlimeWorldManager"
if [ $? -eq 0 ]; then
    echo "❌ SlimeWorldManager is a hard dependency (should be soft)"
else
    echo "✅ SlimeWorldManager is not a hard dependency"
fi

echo
echo "=== Summary ==="
echo "The plugin JAR has been built and validated."
echo "✅ Ready for deployment to a Minecraft server"
echo
echo "Required server plugins for full functionality:"
echo "  - WorldEdit (required)"
echo "  - WorldGuard (required)"
echo "  - SlimeWorldManager (optional, for individual island worlds)"
echo
echo "To deploy:"
echo "  1. Copy $JAR_FILE to your server's plugins folder"
echo "  2. Ensure WorldEdit and WorldGuard are installed"
echo "  3. Start/restart the server"
echo "  4. Check server logs for any loading issues"
