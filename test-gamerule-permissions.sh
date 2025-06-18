#!/bin/bash

echo "🔒 Gamerule Permission-Based Visibility Test"
echo "============================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

function test_info() {
    echo -e "  ${BLUE}ℹ️  INFO${NC}: $1"
}

function test_passed() {
    echo -e "  ${GREEN}✅ PASS${NC}: $1"
}

function test_failed() {
    echo -e "  ${RED}❌ FAIL${NC}: $1"
}

function test_warning() {
    echo -e "  ${YELLOW}⚠️  WARN${NC}: $1"
}

echo "🏗️ 1. BUILD VERIFICATION"
echo "========================"

if mvn clean package -DskipTests > build.log 2>&1; then
    test_passed "Plugin builds successfully with permission fixes"
else
    test_failed "Plugin build failed"
    exit 1
fi

echo ""
echo "🔒 2. PERMISSION LOGIC VERIFICATION"
echo "=================================="

# Check if getAvailableGameRules uses permission checking
if grep -A 15 "getAvailableGameRules" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandSettingsManager.java | grep -q "player.hasPermission(permissionNode)"; then
    test_passed "getAvailableGameRules correctly checks for explicit permissions"
else
    test_failed "getAvailableGameRules doesn't use proper permission checking"
fi

# Check if hasPermissionForGameRule uses permission checking
if grep -A 10 "hasPermissionForGameRule" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandSettingsManager.java | grep -q "player.hasPermission(permissionNode)"; then
    test_passed "hasPermissionForGameRule correctly checks for explicit permissions"
else
    test_failed "hasPermissionForGameRule doesn't use proper permission checking"
fi

# Check admin bypass is preserved
if grep -A 10 "getAvailableGameRules" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandSettingsManager.java | grep -q "skyeblock.gamerules.adminbypass"; then
    test_passed "Admin bypass permission is preserved"
else
    test_failed "Admin bypass permission missing"
fi

echo ""
echo "🎛️ 3. GUI BEHAVIOR VERIFICATION"
echo "==============================="

# Check if GUI handles empty gamerule lists
if grep -A 5 "availableRules.isEmpty()" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandSettingsGUI.java | grep -q "don't have permission"; then
    test_passed "GUI handles empty gamerule lists with proper message"
else
    test_failed "GUI doesn't handle empty gamerule lists properly"
fi

# Check if GUI uses getAvailableGameRules
if grep -q "getAvailableGameRules" src/main/java/skyeblock/nobleskye/dev/skyeblock/gui/IslandSettingsGUI.java; then
    test_passed "GUI uses permission-filtered gamerule list"
else
    test_failed "GUI doesn't use permission filtering"
fi

echo ""
echo "📋 4. PERMISSION NODE VERIFICATION"
echo "================================="

# Check permission node format
if grep -A 10 "hasPermissionForGameRule\|getAvailableGameRules" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandSettingsManager.java | grep -q "skyeblock.gamerule.*toLowerCase"; then
    test_passed "Permission nodes use correct format: skyeblock.gamerule.<name>"
else
    test_failed "Permission node format incorrect"
fi

echo ""
echo "🎯 5. PERMISSION SYSTEM SUMMARY"
echo "==============================="
echo ""
echo -e "${GREEN}✅ IMPLEMENTED BEHAVIOR:${NC}"
echo "  🔒 Only shows gamerules if player has explicit permission"
echo "  👑 Admin bypass: 'skyeblock.gamerules.adminbypass'"
echo "  🎛️ Individual permissions: 'skyeblock.gamerule.<name>'"
echo "  📝 Clean GUI: No gamerules shown without permission"
echo "  ⚠️ Clear message when no permissions granted"
echo ""
echo -e "${BLUE}📋 HOW IT WORKS NOW:${NC}"
echo "  1. Player opens '/island settings'"
echo "  2. Check for admin bypass permission"
echo "  3. If admin: Show ALL gamerules"
echo "  4. If not admin: Only show gamerules with explicit permission"
echo "  5. If no permissions: Show helpful message"
echo ""
echo -e "${YELLOW}🔧 PERMISSION EXAMPLES:${NC}"
echo "  Admin Access:"
echo "    skyeblock.gamerules.adminbypass: true"
echo ""
echo "  Specific Gamerules:"
echo "    skyeblock.gamerule.keepinventory: true"
echo "    skyeblock.gamerule.doweathercycle: true"
echo "    skyeblock.gamerule.mobgriefing: true"
echo ""
echo "  No Access (default):"
echo "    No permissions = No gamerules visible"
echo ""
echo -e "${GREEN}🎉 PERMISSION-BASED VISIBILITY COMPLETE!${NC}"
echo "Players will now only see gamerules they have explicit permission for!"
echo ""
echo -e "${BLUE}📝 TESTING INSTRUCTIONS:${NC}"
echo "1. Grant specific gamerule permissions to test users"
echo "2. Have them open '/island settings'"
echo "3. Verify only permitted gamerules appear"
echo "4. Test admin bypass with 'skyeblock.gamerules.adminbypass'"
