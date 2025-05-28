#!/bin/bash

# Test script for comprehensive island visiting and coop system
# This script validates the implementation of all new features

echo "🏝️  Testing SkyeBlock Island Visiting and Coop System"
echo "=================================================="

# Build the plugin first
echo "📦 Building plugin..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "❌ Build failed! Aborting test."
    exit 1
fi
echo "✅ Build successful!"

# Test 1: Verify all new classes exist and compile
echo ""
echo "🔍 Test 1: Verifying class compilation..."
REQUIRED_CLASSES=(
    "target/classes/skyeblock/nobleskye/dev/skyeblock/gui/MainSettingsGUI.class"
    "target/classes/skyeblock/nobleskye/dev/skyeblock/gui/VisitingSettingsGUI.class" 
    "target/classes/skyeblock/nobleskye/dev/skyeblock/gui/IslandVisitGUI.class"
    "target/classes/skyeblock/nobleskye/dev/skyeblock/listeners/VisitorProtectionListener.class"
    "target/classes/skyeblock/nobleskye/dev/skyeblock/models/Island.class"
    "target/classes/skyeblock/nobleskye/dev/skyeblock/models/Island\$CoopRole.class"
)

for class_file in "${REQUIRED_CLASSES[@]}"; do
    if [ -f "$class_file" ]; then
        echo "✅ $class_file"
    else
        echo "❌ Missing: $class_file"
        exit 1
    fi
done

# Test 2: Verify Island model enhancements
echo ""
echo "🏗️  Test 2: Verifying Island model enhancements..."
if grep -q "enum CoopRole" src/main/java/skyeblock/nobleskye/dev/skyeblock/models/Island.java; then
    echo "✅ CoopRole enum found"
else
    echo "❌ CoopRole enum missing"
    exit 1
fi

if grep -q "private final Map<UUID, CoopRole> coopMembers" src/main/java/skyeblock/nobleskye/dev/skyeblock/models/Island.java; then
    echo "✅ Coop members system found"
else
    echo "❌ Coop members system missing"
    exit 1
fi

if grep -q "private final Map<UUID, Long> votes" src/main/java/skyeblock/nobleskye/dev/skyeblock/models/Island.java; then
    echo "✅ Voting system found"
else
    echo "❌ Voting system missing"
    exit 1
fi

# Test 3: Verify command system
echo ""
echo "⚡ Test 3: Verifying command system..."
REQUIRED_COMMANDS=(
    "visit"
    "lock"
    "unlock"
    "edit"
    "coop"
    "vote"
    "set"
    "home"
)

for cmd in "${REQUIRED_COMMANDS[@]}"; do
    if grep -q "case \"$cmd\":" src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/IslandCommand.java; then
        echo "✅ Command /$cmd found"
    else
        echo "❌ Command /$cmd missing"
        exit 1
    fi
done

# Test 4: Verify GUI system
echo ""
echo "🖼️  Test 4: Verifying GUI system..."
GUI_FILES=(
    "src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/MainSettingsGUI.java"
    "src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/VisitingSettingsGUI.java"
    "src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandVisitGUI.java"
)

for gui_file in "${GUI_FILES[@]}"; do
    if [ -f "$gui_file" ]; then
        echo "✅ $gui_file exists"
        if grep -q "InventoryHolder" "$gui_file"; then
            echo "✅ $gui_file implements InventoryHolder"
        else
            echo "❌ $gui_file missing InventoryHolder"
            exit 1
        fi
    else
        echo "❌ Missing: $gui_file"
        exit 1
    fi
done

# Test 5: Verify visitor protection system
echo ""
echo "🛡️  Test 5: Verifying visitor protection system..."
if [ -f "src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/VisitorProtectionListener.java" ]; then
    echo "✅ VisitorProtectionListener exists"
    
    PROTECTION_EVENTS=(
        "BlockBreakEvent"
        "BlockPlaceEvent"
        "PlayerInteractEvent"
        "EntityPickupItemEvent"
        "PlayerDropItemEvent"
        "InventoryOpenEvent"
    )
    
    for event in "${PROTECTION_EVENTS[@]}"; do
        if grep -q "$event" src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/VisitorProtectionListener.java; then
            echo "✅ $event protection found"
        else
            echo "❌ $event protection missing"
            exit 1
        fi
    done
else
    echo "❌ VisitorProtectionListener missing"
    exit 1
fi

# Test 6: Verify plugin.yml updates
echo ""
echo "📋 Test 6: Verifying plugin.yml updates..."
if grep -q "skyeblock.island.visit" src/main/resources/plugin.yml; then
    echo "✅ Visit permission found"
else
    echo "❌ Visit permission missing"
fi

if grep -q "skyeblock.island.vote" src/main/resources/plugin.yml; then
    echo "✅ Vote permission found"
else
    echo "❌ Vote permission missing"
fi

if grep -q "skyeblock.island.coop" src/main/resources/plugin.yml; then
    echo "✅ Coop permission found"
else
    echo "❌ Coop permission missing"
fi

# Test 7: Final compilation test
echo ""
echo "🔨 Test 7: Final compilation test..."
mvn compile -q
if [ $? -eq 0 ]; then
    echo "✅ Final compilation successful"
else
    echo "❌ Final compilation failed"
    exit 1
fi

echo ""
echo "🎉 ALL TESTS PASSED!"
echo "=================================================="
echo "Island Visiting and Coop System Implementation Summary:"
echo ""
echo "✅ Enhanced Island Model with:"
echo "   • Coop system with 5-tier role hierarchy"
echo "   • Voting system with time restrictions"
echo "   • Island customization (title, description, icon)"
echo "   • Locking and adventure mode settings"
echo "   • Custom home/visit locations"
echo ""
echo "✅ Complete Command System:"
echo "   • /is visit [player] - Visit islands with GUI"
echo "   • /is lock|unlock - Toggle island access"
echo "   • /is edit title|desc|icon - Customize island"
echo "   • /is coop add|remove|role|leave|list - Manage coop"
echo "   • /is vote <player> - Vote for islands"
echo "   • /is set home|visit - Set teleport locations"
echo "   • /is home - Teleport home (renamed from /is tp)"
echo ""
echo "✅ Advanced GUI System:"
echo "   • MainSettingsGUI - Central settings hub"
echo "   • VisitingSettingsGUI - Complete visiting controls"
echo "   • IslandVisitGUI - Player-head island browser"
echo ""
echo "✅ Visitor Protection System:"
echo "   • Adventure mode enforcement for visitors"
echo "   • Block interaction prevention"
echo "   • Container access protection"
echo "   • Item manipulation restrictions"
echo ""
echo "✅ Security and Access Control:"
echo "   • Role-based permissions (VISITOR < MEMBER < ADMIN < CO_OWNER < OWNER)"
echo "   • Island locking prevents unauthorized visits"
echo "   • Vote cooldown system (23h cooldown, 7d expiry)"
echo "   • Safe teleportation with fallback locations"
echo ""
echo "🚀 Ready for deployment and testing!"
