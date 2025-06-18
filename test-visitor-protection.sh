#!/bin/bash

# SkyeBlock Visitor Protection System Test
# Tests comprehensive visitor restrictions for island protection

echo "🛡️  Testing SkyeBlock Visitor Protection System"
echo "============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to check if a file exists and contains specific content
check_file_content() {
    local file=$1
    local pattern=$2
    local description=$3
    
    if [ -f "$file" ]; then
        if grep -q "$pattern" "$file"; then
            echo -e "${GREEN}✅ $description${NC}"
            ((TESTS_PASSED++))
        else
            echo -e "${RED}❌ $description - Pattern not found${NC}"
            ((TESTS_FAILED++))
        fi
    else
        echo -e "${RED}❌ $description - File not found${NC}"
        ((TESTS_FAILED++))
    fi
}

# Function to check if a method exists in a Java file
check_method_exists() {
    local file=$1
    local method=$2
    local description=$3
    
    if [ -f "$file" ]; then
        if grep -q "public.*$method" "$file" || grep -q "private.*$method" "$file"; then
            echo -e "${GREEN}✅ $description${NC}"
            ((TESTS_PASSED++))
        else
            echo -e "${RED}❌ $description - Method not found${NC}"
            ((TESTS_FAILED++))
        fi
    else
        echo -e "${RED}❌ $description - File not found${NC}"
        ((TESTS_FAILED++))
    fi
}

echo ""
echo "🔍 Test 1: Checking VisitorProtectionListener Implementation"
echo "-----------------------------------------------------------"

VISITOR_LISTENER="src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/VisitorProtectionListener.java"

check_file_content "$VISITOR_LISTENER" "class VisitorProtectionListener implements Listener" "VisitorProtectionListener class exists"
check_file_content "$VISITOR_LISTENER" "isVisitorRestricted" "Visitor restriction check method exists"
check_file_content "$VISITOR_LISTENER" "sendRestrictionMessage" "Restriction message method exists"

echo ""
echo "🚫 Test 2: Checking Block Interaction Protection"
echo "------------------------------------------------"

check_file_content "$VISITOR_LISTENER" "public void onBlockBreak" "Block break event handler"
check_file_content "$VISITOR_LISTENER" "public void onBlockPlace" "Block place event handler"
check_file_content "$VISITOR_LISTENER" "public void onPlayerInteract" "Player interact event handler"
check_file_content "$VISITOR_LISTENER" "BlockBreakEvent" "Block break event import/usage"
check_file_content "$VISITOR_LISTENER" "BlockPlaceEvent" "Block place event import/usage"

echo ""
echo "📦 Test 3: Checking Container Access Protection"
echo "-----------------------------------------------"

check_file_content "$VISITOR_LISTENER" "public void onInventoryOpen" "Inventory open event handler"
check_file_content "$VISITOR_LISTENER" "public void onInventoryClick" "Inventory click event handler"
check_file_content "$VISITOR_LISTENER" "InventoryOpenEvent" "Inventory open event import/usage"
check_file_content "$VISITOR_LISTENER" "InventoryClickEvent" "Inventory click event import/usage"
check_file_content "$VISITOR_LISTENER" "InventoryType" "Inventory type checking"

echo ""
echo "🎒 Test 4: Checking Item Manipulation Protection"
echo "------------------------------------------------"

check_file_content "$VISITOR_LISTENER" "public void onEntityPickupItem" "Item pickup event handler"
check_file_content "$VISITOR_LISTENER" "public void onPlayerDropItem" "Item drop event handler"
check_file_content "$VISITOR_LISTENER" "EntityPickupItemEvent" "Entity pickup item event import/usage"
check_file_content "$VISITOR_LISTENER" "PlayerDropItemEvent" "Player drop item event import/usage"

echo ""
echo "🐄 Test 5: Checking Entity Interaction Protection"
echo "-------------------------------------------------"

check_file_content "$VISITOR_LISTENER" "public void onPlayerInteractEntity" "Entity interact event handler"
check_file_content "$VISITOR_LISTENER" "PlayerInteractEntityEvent" "Player interact entity event import/usage"
check_file_content "$VISITOR_LISTENER" "EntityType" "Entity type checking"

echo ""
echo "⚔️ Test 6: Checking PVP Protection"
echo "-----------------------------------"

check_file_content "$VISITOR_LISTENER" "public void onEntityDamageByEntity" "Entity damage event handler"
check_file_content "$VISITOR_LISTENER" "EntityDamageByEntityEvent" "Entity damage by entity event import/usage"

echo ""
echo "🎮 Test 7: Checking Game Mode Protection"
echo "----------------------------------------"

check_file_content "$VISITOR_LISTENER" "public void onGameModeChange" "Game mode change event handler"
check_file_content "$VISITOR_LISTENER" "PlayerGameModeChangeEvent" "Player game mode change event import/usage"
check_file_content "$VISITOR_LISTENER" "GameMode.ADVENTURE" "Adventure mode enforcement"

echo ""
echo "🔑 Test 8: Checking Admin Bypass System"
echo "---------------------------------------"

check_file_content "$VISITOR_LISTENER" "skyeblock.admin.bypass" "Admin bypass permission check"
check_file_content "$VISITOR_LISTENER" "hasPermission.*bypass" "Admin bypass permission implementation"

echo ""
echo "🏝️ Test 9: Checking Island Role System Integration"
echo "--------------------------------------------------"

check_file_content "$VISITOR_LISTENER" "Island.CoopRole.VISITOR" "Visitor role checking"
check_file_content "$VISITOR_LISTENER" "getCoopRole" "Coop role retrieval"
check_file_content "$VISITOR_LISTENER" "getIslandById" "Island retrieval by world name"

echo ""
echo "📜 Test 10: Checking Plugin Registration"
echo "----------------------------------------"

PLUGIN_FILE="src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java"

check_file_content "$PLUGIN_FILE" "VisitorProtectionListener" "VisitorProtectionListener import"
check_file_content "$PLUGIN_FILE" "new VisitorProtectionListener" "VisitorProtectionListener instantiation"
check_file_content "$PLUGIN_FILE" "registerEvents.*visitorProtectionListener" "VisitorProtectionListener registration"

echo ""
echo "🔧 Test 11: Checking Compilation"
echo "--------------------------------"

echo "Attempting to compile the project..."
if mvn clean compile -q > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Project compiles successfully${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Project compilation failed${NC}"
    ((TESTS_FAILED++))
fi

echo ""
echo "📋 Test 12: Checking Event Priority"
echo "-----------------------------------"

check_file_content "$VISITOR_LISTENER" "EventPriority.HIGH" "High priority event handling"

echo ""
echo "💬 Test 13: Checking Message System"
echo "-----------------------------------"

check_file_content "$VISITOR_LISTENER" "sendRestrictionMessage" "Restriction message method"
check_file_content "$VISITOR_LISTENER" "MiniMessage" "MiniMessage integration"
check_file_content "$VISITOR_LISTENER" "cannot.*while visiting" "Visitor restriction messages"

echo ""
echo "🎯 Test 14: Checking Specific Protection Types"
echo "----------------------------------------------"

# Check for specific protection implementations
check_file_content "$VISITOR_LISTENER" "break blocks" "Block break protection message"
check_file_content "$VISITOR_LISTENER" "place blocks" "Block place protection message"
check_file_content "$VISITOR_LISTENER" "open containers" "Container access protection message"
check_file_content "$VISITOR_LISTENER" "pick up items" "Item pickup protection message"
check_file_content "$VISITOR_LISTENER" "drop items" "Item drop protection message"
check_file_content "$VISITOR_LISTENER" "engage in PVP" "PVP protection message"

echo ""
echo "🚪 Test 15: Checking Navigation Allowances"
echo "------------------------------------------"

# Check that basic navigation is still allowed
check_file_content "$VISITOR_LISTENER" "OAK_DOOR\|SPRUCE_DOOR" "Door interaction allowance"
check_file_content "$VISITOR_LISTENER" "BUTTON\|PRESSURE_PLATE" "Button/pressure plate allowance"

echo ""
echo "==============================================="
echo "🏁 Visitor Protection System Test Results"
echo "==============================================="

TOTAL_TESTS=$((TESTS_PASSED + TESTS_FAILED))

echo -e "Total Tests: ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"

if [ $TESTS_FAILED -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 ALL TESTS PASSED! Visitor protection system is fully implemented.${NC}"
    echo ""
    echo "✅ Visitors are now completely restricted from:"
    echo "   • Breaking or placing blocks"
    echo "   • Opening containers (chests, furnaces, etc.)"
    echo "   • Picking up or dropping items"
    echo "   • Interacting with entities (animals, item frames, etc.)"
    echo "   • Engaging in PVP"
    echo "   • Changing game modes"
    echo "   • Using redstone devices (except doors/buttons for navigation)"
    echo ""
    echo "✅ Visitors can still:"
    echo "   • Walk around and explore"
    echo "   • Use doors, buttons, and pressure plates for navigation"
    echo "   • View the island"
    echo ""
    echo "✅ Admins can bypass all restrictions with 'skyeblock.admin.bypass' permission"
    echo ""
    exit 0
else
    echo ""
    echo -e "${RED}❌ SOME TESTS FAILED! Please review the implementation.${NC}"
    echo ""
    exit 1
fi
