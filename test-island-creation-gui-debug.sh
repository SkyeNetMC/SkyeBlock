#!/bin/bash

# Test script for Island Creation GUI debugging
echo "=== Island Creation GUI Debug Test ==="

# Check if the plugin was built successfully
if [ ! -f "target/skyeblock-2.0.0.jar" ]; then
    echo "❌ Plugin JAR not found. Building..."
    mvn package -q
    if [ $? -ne 0 ]; then
        echo "❌ Failed to build plugin"
        exit 1
    fi
fi

echo "✅ Plugin JAR exists: $(ls -lh target/skyeblock-2.0.0.jar | awk '{print $5}')"

# Check if IslandCreationGUI class exists
if [ -f "target/classes/skyeblock/nobleskye/dev/skyeblock/gui/IslandCreationGUI.class" ]; then
    echo "✅ IslandCreationGUI class compiled successfully"
else
    echo "❌ IslandCreationGUI class failed to compile"
fi

# Check if CreateIslandCommand class exists
if [ -f "target/classes/skyeblock/nobleskye/dev/skyeblock/commands/CreateIslandCommand.class" ]; then
    echo "✅ CreateIslandCommand class compiled successfully"
else
    echo "❌ CreateIslandCommand class failed to compile"
fi

# Check the SkyeBlockPlugin onEnable method for GUI initialization
echo ""
echo "=== Checking Plugin Integration ==="

if grep -q "islandCreationGUI = new IslandCreationGUI" src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java; then
    echo "✅ IslandCreationGUI is initialized in SkyeBlockPlugin.onEnable()"
else
    echo "❌ IslandCreationGUI not initialized in SkyeBlockPlugin.onEnable()"
fi

if grep -q "createisland" src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java; then
    echo "✅ CreateIslandCommand is registered in SkyeBlockPlugin"
else
    echo "❌ CreateIslandCommand not registered in SkyeBlockPlugin"
fi

# Check if createisland command is defined in plugin.yml
echo ""
echo "=== Checking plugin.yml Configuration ==="

if grep -q "createisland:" plugin.yml; then
    echo "✅ createisland command defined in plugin.yml"
    echo "Command details:"
    sed -n '/createisland:/,/^[^ ]/p' plugin.yml | head -n -1
else
    echo "❌ createisland command not defined in plugin.yml"
fi

# Check event handler implementation
echo ""
echo "=== Checking Event Handler Implementation ==="

if grep -q "@EventHandler" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandCreationGUI.java; then
    echo "✅ Event handlers found in IslandCreationGUI"
    echo "Event handlers:"
    grep -A 1 "@EventHandler" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandCreationGUI.java
else
    echo "❌ No event handlers found in IslandCreationGUI"
fi

# Check if Listener interface is implemented
if grep -q "implements.*Listener" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandCreationGUI.java; then
    echo "✅ IslandCreationGUI implements Listener interface"
else
    echo "❌ IslandCreationGUI does not implement Listener interface"
fi

# Check inventory creation method
echo ""
echo "=== Checking Inventory Implementation ==="

if grep -q "Bukkit.createInventory(this" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandCreationGUI.java; then
    echo "✅ Inventory created with proper holder (this)"
else
    echo "❌ Inventory not created with proper holder"
fi

# Check click slot handling
if grep -q "slot >= 10 && slot <= 16" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandCreationGUI.java; then
    echo "✅ Island type selection slots (10-16) handled"
else
    echo "❌ Island type selection slots not handled"
fi

if grep -q "slot == 22" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandCreationGUI.java; then
    echo "✅ Create button slot (22) handled"
else
    echo "❌ Create button slot not handled"
fi

if grep -q "slot == 18" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandCreationGUI.java; then
    echo "✅ Cancel button slot (18) handled"
else
    echo "❌ Cancel button slot not handled"
fi

# Check for debug logging
echo ""
echo "=== Checking Debug Logging ==="

if grep -q "plugin.getLogger().info.*InventoryClick event received" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandCreationGUI.java; then
    echo "✅ Debug logging added to inventory click handler"
else
    echo "❌ No debug logging in inventory click handler"
fi

if grep -q "plugin.getLogger().info.*openCreationGUI called" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandCreationGUI.java; then
    echo "✅ Debug logging added to openCreationGUI method"
else
    echo "❌ No debug logging in openCreationGUI method"
fi

echo ""
echo "=== Summary ==="
echo "The Island Creation GUI implementation appears to be complete with debug logging."
echo "Key components to verify during testing:"
echo "1. Check server logs for 'IslandCreationGUI constructor called' message on plugin load"
echo "2. Check server logs for 'openCreationGUI called for player' when /createisland is used"
echo "3. Check server logs for 'InventoryClick event received' when GUI items are clicked"
echo "4. Verify inventory holder detection logs show 'Is IslandCreationGUI: true'"
echo ""
echo "If GUI items are still not clickable, the logs will help identify where the issue occurs."
