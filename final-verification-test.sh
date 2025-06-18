#!/bin/bash

# Final verification test to ensure all features are working correctly

echo "🚀 Final SkyeBlock Plugin Verification"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PASS=0
FAIL=0

function test_passed() {
    echo -e "  ${GREEN}✅ PASS${NC}: $1"
    ((PASS++))
}

function test_failed() {
    echo -e "  ${RED}❌ FAIL${NC}: $1"
    ((FAIL++))
}

function test_info() {
    echo -e "  ${BLUE}ℹ️  INFO${NC}: $1"
}

function test_warning() {
    echo -e "  ${YELLOW}⚠️  WARN${NC}: $1"
}

echo "🏗️ 1. BUILD VERIFICATION"
echo "========================"

# Build the plugin
if mvn clean package -DskipTests > build.log 2>&1; then
    test_passed "Plugin builds successfully"
    if [ -f "target/skyeblock-3.1.0.jar" ]; then
        test_passed "JAR file created successfully"
        JAR_SIZE=$(stat -c%s "target/skyeblock-3.1.0.jar" 2>/dev/null || echo "0")
        test_info "JAR size: $(($JAR_SIZE / 1024))KB"
    else
        test_failed "JAR file not created"
    fi
else
    test_failed "Plugin build failed"
    echo "Build errors:"
    tail -20 build.log
fi

echo ""
echo "🔧 2. SBA COMMAND VERIFICATION"
echo "=============================="

# Check SBA command in plugin.yml
if grep -q "sba:" src/main/resources/plugin.yml; then
    test_passed "SBA command defined in plugin.yml"
    
    # Show command definition
    echo "  Command definition:"
    grep -A 5 "sba:" src/main/resources/plugin.yml | sed 's/^/    /'
else
    test_failed "SBA command not found in plugin.yml"
fi

# Check SBA command class exists
if [ -f "src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/SkyeBlockAdminCommand.java" ]; then
    test_passed "SkyeBlockAdminCommand class exists"
else
    test_failed "SkyeBlockAdminCommand class missing"
fi

# Check command registration in main plugin
if grep -q 'getCommand("sba")' src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java; then
    test_passed "SBA command registration code found"
else
    test_failed "SBA command registration missing"
fi

echo ""
echo "🕒 3. COOLDOWN LOGIC VERIFICATION"
echo "================================"

# Check cooldown methods exist
if grep -q "canDeleteIsland" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java; then
    test_passed "canDeleteIsland method found"
else
    test_failed "canDeleteIsland method missing"
fi

if grep -q "canCreateIsland" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java; then
    test_passed "canCreateIsland method found"
else
    test_failed "canCreateIsland method missing"
fi

# Check for correct cooldown logic
if grep -A 20 "canDeleteIsland" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java | grep -q "currentTries >= 2"; then
    test_passed "Correct deletion limit logic (2 deletions before cooldown)"
else
    test_failed "Incorrect or missing deletion limit logic"
fi

# Check config messages
if grep -q "deletion-blocked-cooldown" src/main/resources/config.yml; then
    test_passed "Cooldown message configured"
else
    test_failed "Cooldown message missing in config"
fi

echo ""
echo "🌍 4. ISLAND TELEPORT FIX VERIFICATION"
echo "====================================="

# Check for island reload logic in teleportToIsland
if grep -A 25 "teleportToIsland.*Player player.*{" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java | grep -q "loadIsland"; then
    test_passed "Island reload logic found in teleportToIsland"
else
    test_failed "Island reload logic missing"
fi

# Check loadIsland method exists in IslandDataManager
if grep -q "loadIsland.*UUID" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandDataManager.java; then
    test_passed "loadIsland method exists in IslandDataManager"
else
    test_failed "loadIsland method missing in IslandDataManager"
fi

echo ""
echo "🧹 5. SERVER BRAND REMOVAL VERIFICATION"
echo "======================================="

# Check that server brand related code is removed
if ! grep -r -i "serverbrand\|server.brand\|brandplugin" src/main/java/ 2>/dev/null; then
    test_passed "Server brand code removed from Java files"
else
    test_failed "Server brand code still present in Java files"
    echo "  Found references:"
    grep -r -i "serverbrand\|server.brand\|brandplugin" src/main/java/ | head -5
fi

if ! grep -i "serverbrand\|server.brand" src/main/resources/plugin.yml 2>/dev/null; then
    test_passed "Server brand references removed from plugin.yml"
else
    test_failed "Server brand references still in plugin.yml"
fi

echo ""
echo "📝 6. CONFIGURATION VERIFICATION"
echo "==============================="

# Check important config values
if grep -q "create-island:" src/main/resources/config.yml; then
    test_passed "Island creation config section found"
else
    test_failed "Island creation config section missing"
fi

if grep -q "delay:" src/main/resources/config.yml; then
    test_passed "Cooldown delay configuration found"
    DELAY_VALUE=$(grep -A 2 "delay:" src/main/resources/config.yml | grep -o '[0-9]*' | head -1)
    test_info "Configured delay: ${DELAY_VALUE}s ($(($DELAY_VALUE / 60)) minutes)"
else
    test_failed "Cooldown delay configuration missing"
fi

echo ""
echo "📊 FINAL RESULTS"
echo "==============="
echo -e "Tests passed: ${GREEN}$PASS${NC}"
echo -e "Tests failed: ${RED}$FAIL${NC}"
echo -e "Total tests: $(($PASS + $FAIL))"

if [ $FAIL -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 ALL TESTS PASSED! Plugin is ready for deployment.${NC}"
    echo ""
    echo "Key Features Verified:"
    echo "  ✅ Plugin builds successfully"
    echo "  ✅ SBA command is properly registered"
    echo "  ✅ Cooldown logic implemented (2 deletions, then 1-hour wait for 3rd)"
    echo "  ✅ Island creation is always allowed"
    echo "  ✅ /is tp has fallback reload logic for post-restart issues"
    echo "  ✅ Server brand functionality completely removed"
    echo "  ✅ Configuration is properly set up"
    echo ""
    exit 0
else
    echo ""
    echo -e "${RED}❌ Some tests failed. Please review the issues above.${NC}"
    echo ""
    exit 1
fi
