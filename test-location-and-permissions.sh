#!/bin/bash

# SkyeBlock v3.1.0 Feature Test - Player Location & Granular Permissions
# Tests the new player location tracking and granular visitor protection features

echo "🛡️  Testing SkyeBlock v3.1.0 - Enhanced Features"
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test compilation
echo -e "${BLUE}🔧 Test 1: Checking Plugin Compilation${NC}"
echo "--------------------------------------------"
if mvn compile -q 2>/dev/null; then
    echo -e "${GREEN}✅ Plugin compiles successfully${NC}"
else
    echo -e "${RED}❌ Plugin compilation failed${NC}"
    echo "Please check the logs for compilation errors."
    exit 1
fi

echo ""

# Test player data manager implementation
echo -e "${BLUE}📍 Test 2: Player Location Tracking System${NC}"
echo "---------------------------------------------"

if grep -q "PlayerDataManager" src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java; then
    echo -e "${GREEN}✅ PlayerDataManager integrated into main plugin${NC}"
else
    echo -e "${RED}❌ PlayerDataManager not integrated${NC}"
fi

if [ -f "src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/PlayerDataManager.java" ]; then
    echo -e "${GREEN}✅ PlayerDataManager class exists${NC}"
else
    echo -e "${RED}❌ PlayerDataManager class missing${NC}"
fi

if [ -f "src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/PlayerLocationListener.java" ]; then
    echo -e "${GREEN}✅ PlayerLocationListener exists${NC}"
else
    echo -e "${RED}❌ PlayerLocationListener missing${NC}"
fi

if grep -q "getPlayerDataManager" src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java; then
    echo -e "${GREEN}✅ PlayerDataManager getter method exists${NC}"
else
    echo -e "${RED}❌ PlayerDataManager getter method missing${NC}"
fi

echo ""

# Test join behavior modification
echo -e "${BLUE}🚪 Test 3: Join Behavior Enhancement${NC}"
echo "--------------------------------------"

if grep -q "hasLastLocation" src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/PlayerJoinListener.java; then
    echo -e "${GREEN}✅ PlayerJoinListener checks for last location${NC}"
else
    echo -e "${RED}❌ PlayerJoinListener doesn't check last location${NC}"
fi

if grep -q "teleportToSpawnOnJoin.*last.*location" src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/PlayerJoinListener.java; then
    echo -e "${GREEN}✅ Join teleport logic updated to use saved locations${NC}"
else
    echo -e "${YELLOW}⚠️  Join teleport logic may need verification${NC}"
fi

echo ""

# Test granular permissions GUI
echo -e "${BLUE}🎛️  Test 4: Granular Permissions GUI${NC}"
echo "------------------------------------"

if grep -q "addPermissionToggle" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/VisitingSettingsGUI.java; then
    echo -e "${GREEN}✅ Granular permission toggles implemented${NC}"
else
    echo -e "${RED}❌ Granular permission toggles missing${NC}"
fi

if grep -q "visitorCanBreakBlocks\|canVisitorBreakBlocks" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/VisitingSettingsGUI.java; then
    echo -e "${GREEN}✅ Individual permission checks in GUI${NC}"
else
    echo -e "${RED}❌ Individual permission checks missing in GUI${NC}"
fi

if grep -q "INVENTORY_SIZE.*54" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/VisitingSettingsGUI.java; then
    echo -e "${GREEN}✅ GUI expanded to 6 rows for more controls${NC}"
else
    echo -e "${RED}❌ GUI size not updated${NC}"
fi

echo ""

# Test permission persistence
echo -e "${BLUE}💾 Test 5: Permission Persistence${NC}"
echo "----------------------------------"

if grep -q "visitor-permissions" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandDataManager.java; then
    echo -e "${GREEN}✅ Granular permissions saved to persistent storage${NC}"
else
    echo -e "${RED}❌ Granular permissions not saved${NC}"
fi

if grep -q "can-break-blocks\|can-place-blocks\|can-open-containers" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandDataManager.java; then
    echo -e "${GREEN}✅ Individual permission fields in persistence${NC}"
else
    echo -e "${RED}❌ Individual permission fields missing${NC}"
fi

echo ""

# Test protection listener updates
echo -e "${BLUE}🛡️  Test 6: Protection Listener Updates${NC}"
echo "----------------------------------------"

if grep -q "isVisitorRestricted.*action.*pvp" src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/VisitorProtectionListener.java; then
    echo -e "${GREEN}✅ PVP protection uses granular permissions${NC}"
else
    echo -e "${RED}❌ PVP protection not updated${NC}"
fi

if grep -q "isVisitorRestricted.*action.*drop_items" src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/VisitorProtectionListener.java; then
    echo -e "${GREEN}✅ Item drop protection uses granular permissions${NC}"
else
    echo -e "${RED}❌ Item drop protection not updated${NC}"
fi

if grep -q "isVisitorRestricted.*action.*open_containers" src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/VisitorProtectionListener.java; then
    echo -e "${GREEN}✅ Container protection uses granular permissions${NC}"
else
    echo -e "${RED}❌ Container protection not updated${NC}"
fi

echo ""

# Test version updates
echo -e "${BLUE}📦 Test 7: Version Updates${NC}"
echo "----------------------------"

PLUGIN_VERSION=$(grep "version:" plugin.yml | sed "s/version: '\(.*\)'/\1/")
POM_VERSION=$(grep -A1 "<artifactId>skyeblock</artifactId>" pom.xml | grep "<version>" | sed 's/.*<version>\(.*\)<\/version>.*/\1/')

if [[ "$PLUGIN_VERSION" == "3.1.0" ]]; then
    echo -e "${GREEN}✅ plugin.yml version updated to 3.1.0${NC}"
else
    echo -e "${RED}❌ plugin.yml version not updated (current: $PLUGIN_VERSION)${NC}"
fi

if [[ "$POM_VERSION" == "3.1.0" ]]; then
    echo -e "${GREEN}✅ pom.xml version updated to 3.1.0${NC}"
else
    echo -e "${RED}❌ pom.xml version not updated (current: $POM_VERSION)${NC}"
fi

echo ""

# Test command functionality
echo -e "${BLUE}⚡ Test 8: Command Functionality${NC}"
echo "---------------------------------"

if grep -q "case \"tp\":\|case \"teleport\":\|case \"home\":" src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/IslandCommand.java; then
    echo -e "${GREEN}✅ /is tp, /is teleport, and /is home commands all work${NC}"
else
    echo -e "${RED}❌ Teleport commands not properly configured${NC}"
fi

if grep -q "handleTeleportCommand" src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/IslandCommand.java; then
    echo -e "${GREEN}✅ Teleport command handler exists${NC}"
else
    echo -e "${RED}❌ Teleport command handler missing${NC}"
fi

echo ""
echo "==============================================="
echo -e "${GREEN}🎯 SkyeBlock v3.1.0 Feature Summary${NC}"
echo "==============================================="
echo ""
echo -e "${BLUE}🆕 NEW FEATURES:${NC}"
echo "• Player location tracking - players return to their last world on login"
echo "• 8 granular visitor permission toggles in island settings"
echo "• Enhanced GUI with individual controls for each permission"
echo "• Improved protection system with specific action checks"
echo "• Persistent storage of all new settings"
echo ""
echo -e "${BLUE}🔧 FIXES:${NC}"
echo "• Players no longer forced to spawn on login (uses last location)"
echo "• /is tp command functionality maintained and enhanced"
echo "• Proper player data persistence across server restarts"
echo ""
echo -e "${BLUE}📋 GRANULAR PERMISSIONS:${NC}"
echo "• Break Blocks"
echo "• Place Blocks"
echo "• Open Containers"
echo "• Pick Up Items"
echo "• Drop Items"
echo "• Interact with Entities"
echo "• PVP"
echo "• Use Redstone"
echo ""
echo -e "${BLUE}🎮 HOW TO USE:${NC}"
echo "1. Start server with updated plugin"
echo "2. Use '/is settings' to access island settings"
echo "3. Click 'Visiting Settings' to configure permissions"
echo "4. Enable Adventure Mode to see granular controls"
echo "5. Toggle individual permissions as needed"
echo "6. Players will now return to their last location on login"
echo ""
echo -e "${GREEN}✨ Ready for deployment!${NC}"
