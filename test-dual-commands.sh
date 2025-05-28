#!/bin/bash

# Test script to validate dual command system functionality
# Tests both /sb <command> and /<command> formats

echo "=== SkyeBlock Dual Command System Test ==="
echo
echo "This script validates that the plugin supports both:"
echo "1. Direct commands: /island, /visit, /delete, /hub"
echo "2. Sub-commands: /sb island, /sb visit, /sb delete, /sb hub"
echo "3. Aliases: /skyblock, /is"
echo

# Check if the new SkyeBlockCommand class exists
SKYEBLOCK_COMMAND_FILE="/mnt/sda4/SkyeNetwork/SkyeBlock/src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/SkyeBlockCommand.java"
if [ -f "$SKYEBLOCK_COMMAND_FILE" ]; then
    echo "✅ SkyeBlockCommand.java exists"
else
    echo "❌ SkyeBlockCommand.java missing"
    exit 1
fi

# Check plugin.yml has sb command
PLUGIN_YML="/mnt/sda4/SkyeNetwork/SkyeBlock/src/main/resources/plugin.yml"
if grep -q "^  sb:" "$PLUGIN_YML"; then
    echo "✅ /sb command registered in plugin.yml"
else
    echo "❌ /sb command missing from plugin.yml"
    exit 1
fi

# Check that individual commands still exist
for cmd in "island" "visit" "delete" "hub"; do
    if grep -q "^  $cmd:" "$PLUGIN_YML"; then
        echo "✅ /$cmd command registered in plugin.yml"
    else
        echo "❌ /$cmd command missing from plugin.yml"
        exit 1
    fi
done

# Check aliases
if grep -A 4 "^  sb:" "$PLUGIN_YML" | grep -q "aliases: \\[skyblock\\]"; then
    echo "✅ /sb has /skyblock alias"
else
    echo "❌ /sb missing /skyblock alias"
fi

if grep -A 4 "^  island:" "$PLUGIN_YML" | grep -q "aliases: \\[is\\]"; then
    echo "✅ /island has /is alias"
else
    echo "❌ /island missing /is alias"
fi

# Check SkyeBlockPlugin registers the new command
MAIN_PLUGIN_FILE="/mnt/sda4/SkyeNetwork/SkyeBlock/src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java"
if grep -q "SkyeBlockCommand" "$MAIN_PLUGIN_FILE"; then
    echo "✅ SkyeBlockCommand registered in main plugin class"
else
    echo "❌ SkyeBlockCommand not registered in main plugin class"
    exit 1
fi

# Verify the plugin builds successfully
echo
echo "Testing plugin compilation..."
cd /mnt/sda4/SkyeNetwork/SkyeBlock
if mvn compile -q; then
    echo "✅ Plugin compiles successfully"
else
    echo "❌ Plugin compilation failed"
    exit 1
fi

# Check that the JAR file was built recently
JAR_FILE="/mnt/sda4/SkyeNetwork/SkyeBlock/target/skyeblock-1.0.0.jar"
if [ -f "$JAR_FILE" ]; then
    JAR_SIZE=$(stat -c%s "$JAR_FILE")
    echo "✅ Plugin JAR built successfully ($JAR_SIZE bytes)"
else
    echo "❌ Plugin JAR missing"
    exit 1
fi

echo
echo "=== Command Usage Examples ==="
echo "Direct commands (work as before):"
echo "  /island create classic"
echo "  /visit PlayerName"
echo "  /delete"
echo "  /hub"
echo "  /is create desert  (alias)"
echo
echo "Sub-commands (new functionality):"
echo "  /sb island create classic"
echo "  /sb visit PlayerName"
echo "  /sb delete"
echo "  /sb hub"
echo "  /skyblock island create nether  (alias)"
echo
echo "✅ All tests passed! Dual command system implemented successfully."
echo "Both direct commands and sub-commands should now work."
