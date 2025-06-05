#!/bin/bash

# Final validation script for SkyeBlock Persistence & Conversion System
# This script performs comprehensive checks to ensure deployment readiness

echo "=================================================="
echo "SkyeBlock Final Deployment Validation"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Counters
CHECKS_PASSED=0
CHECKS_FAILED=0
TOTAL_CHECKS=0

# Function to perform a check
check() {
    local description="$1"
    local command="$2"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    echo -n "Checking: $description... "
    
    if eval "$command" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ PASS${NC}"
        CHECKS_PASSED=$((CHECKS_PASSED + 1))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC}"
        CHECKS_FAILED=$((CHECKS_FAILED + 1))
        return 1
    fi
}

# Function to check file content
check_content() {
    local description="$1"
    local file="$2"
    local pattern="$3"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    echo -n "Checking: $description... "
    
    if [ -f "$file" ] && grep -q "$pattern" "$file"; then
        echo -e "${GREEN}✓ PASS${NC}"
        CHECKS_PASSED=$((CHECKS_PASSED + 1))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC}"
        CHECKS_FAILED=$((CHECKS_FAILED + 1))
        return 1
    fi
}

echo -e "${BLUE}Phase 1: File Structure Validation${NC}"
echo "------------------------------------"

# Check core files
check "IslandDataManager exists" "[ -f 'src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandDataManager.java' ]"
check "ConvertIslandsCommand exists" "[ -f 'src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/ConvertIslandsCommand.java' ]"
check "Plugin JAR exists" "[ -f 'target/skyeblock-2.0.0.jar' ]"
check "Plugin is recent" "[ $(find target/skyeblock-2.0.0.jar -mmin -60 2>/dev/null | wc -l) -eq 1 ]"

echo ""
echo -e "${BLUE}Phase 2: Integration Validation${NC}"
echo "------------------------------------"

# Check integration points
check_content "IslandManager has initialize method" "src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java" "public void initialize()"
check_content "IslandManager has saveAllIslands method" "src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java" "public void saveAllIslands()"
check_content "SkyeBlockPlugin calls initialize" "src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java" "islandManager.initialize()"
check_content "SkyeBlockPlugin calls saveAllIslands" "src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java" "islandManager.saveAllIslands()"

echo ""
echo -e "${BLUE}Phase 3: Command Registration Validation${NC}"
echo "------------------------------------"

# Check command registration
check_content "ConvertIslands command in plugin.yml" "plugin.yml" "convertislands:"
check_content "ConvertIslands permission in plugin.yml" "plugin.yml" "skyeblock.admin.convert"
check_content "ConvertIslandsCommand registered in main" "src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java" "ConvertIslandsCommand"

echo ""
echo -e "${BLUE}Phase 4: Persistence System Validation${NC}"
echo "------------------------------------"

# Check persistence system components
check_content "IslandDataManager has saveIsland method" "src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandDataManager.java" "public void saveIsland"
check_content "IslandDataManager has loadAllIslands method" "src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandDataManager.java" "public Map<String, Island> loadAllIslands"
check_content "IslandDataManager has deleteIsland method" "src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandDataManager.java" "public void deleteIsland"
check_content "Island class has getVotes method" "src/main/java/skyeblock/nobleskye/dev/skyeblock/models/Island.java" "public Map<UUID, Long> getVotes()"

echo ""
echo -e "${BLUE}Phase 5: Conversion System Validation${NC}"
echo "------------------------------------"

# Check conversion system components
check_content "ConvertIslandsCommand has scan functionality" "src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/ConvertIslandsCommand.java" "scan"
check_content "ConvertIslandsCommand has convert functionality" "src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/ConvertIslandsCommand.java" "convert"
check_content "ConvertIslandsCommand has tab completion" "src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/ConvertIslandsCommand.java" "onTabComplete"

echo ""
echo -e "${BLUE}Phase 6: Compilation Validation${NC}"
echo "------------------------------------"

# Check compilation
check "Maven clean compile" "mvn clean compile -q"
check "Maven package" "mvn package -q"

# Check JAR size (should be reasonable)
if [ -f "target/skyeblock-1.1.0.jar" ]; then
    JAR_SIZE=$(stat -c%s "target/skyeblock-1.1.0.jar")
    if [ "$JAR_SIZE" -gt 100000 ] && [ "$JAR_SIZE" -lt 1000000 ]; then
        echo -e "Checking: JAR file size reasonable... ${GREEN}✓ PASS${NC} (${JAR_SIZE} bytes)"
        CHECKS_PASSED=$((CHECKS_PASSED + 1))
    else
        echo -e "Checking: JAR file size reasonable... ${YELLOW}⚠ WARNING${NC} (${JAR_SIZE} bytes)"
    fi
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

echo ""
echo "=================================================="
echo -e "${BLUE}VALIDATION SUMMARY${NC}"
echo "=================================================="

# Calculate percentage
PASS_PERCENTAGE=$((CHECKS_PASSED * 100 / TOTAL_CHECKS))

echo "Total Checks: $TOTAL_CHECKS"
echo -e "Passed: ${GREEN}$CHECKS_PASSED${NC}"
echo -e "Failed: ${RED}$CHECKS_FAILED${NC}"
echo "Success Rate: $PASS_PERCENTAGE%"

echo ""

if [ $CHECKS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL CHECKS PASSED - READY FOR DEPLOYMENT! 🎉${NC}"
    echo ""
    echo "Deployment Steps:"
    echo "1. Stop your Minecraft server"
    echo "2. Backup current plugin data"
    echo "3. Replace the plugin JAR: target/skyeblock-1.1.0.jar"
    echo "4. Start the server"
    echo "5. Run '/convertislands scan' and '/convertislands convert all' if needed"
    echo ""
    echo "The persistence system will automatically:"
    echo "- Load existing islands on startup"
    echo "- Save island data continuously"
    echo "- Preserve data across server restarts"
    
elif [ $CHECKS_FAILED -le 2 ]; then
    echo -e "${YELLOW}⚠ MOSTLY READY - Minor issues detected${NC}"
    echo "Most checks passed, but please review failed checks above."
    
else
    echo -e "${RED}❌ NOT READY - Multiple issues detected${NC}"
    echo "Please fix the failed checks before deployment."
    exit 1
fi

echo ""
echo "For detailed implementation information, see:"
echo "- PERSISTENCE_CONVERSION_COMPLETE.md"
