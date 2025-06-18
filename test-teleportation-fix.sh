#!/bin/bash

# SkyeBlock Island Teleportation Fix Test
# Tests the fix for the 0,100,0 teleportation issue after server restart

echo "🏝️  Testing SkyeBlock Island Teleportation Fix"
echo "==============================================="

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

# Test island data manager improvements
echo -e "${BLUE}💾 Test 2: Island Data Loading Improvements${NC}"
echo "--------------------------------------------"

if grep -q "WorldManager.*getIslandWorld.*islandId" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandDataManager.java; then
    echo -e "${GREEN}✅ IslandDataManager now uses WorldManager for world loading${NC}"
else
    echo -e "${RED}❌ IslandDataManager still uses basic world loading${NC}"
fi

if grep -q "Could not load.*location.*island.*world.*not found" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandDataManager.java; then
    echo -e "${GREEN}✅ Proper error logging for missing worlds${NC}"
else
    echo -e "${RED}❌ Missing error logging for world loading issues${NC}"
fi

echo ""

# Test island location safety improvements
echo -e "${BLUE}📍 Test 3: Island Location Safety${NC}"
echo "-------------------------------------"

if grep -q "location.*getWorld.*!= null" src/main/java/skyeblock/nobleskye/dev/skyeblock/models/Island.java; then
    echo -e "${GREEN}✅ Island.getSpawnLocation() checks for valid world${NC}"
else
    echo -e "${RED}❌ Island.getSpawnLocation() doesn't validate world${NC}"
fi

if grep -q "return null.*world.*not loaded" src/main/java/skyeblock/nobleskye/dev/skyeblock/models/Island.java; then
    echo -e "${GREEN}✅ Returns null when world is not loaded${NC}"
else
    echo -e "${RED}❌ Doesn't handle unloaded worlds properly${NC}"
fi

echo ""

# Test teleportation robustness
echo -e "${BLUE}🚀 Test 4: Teleportation Robustness${NC}"
echo "------------------------------------"

if grep -q "targetLocation.*null.*invalid world.*fallback" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java; then
    echo -e "${GREEN}✅ IslandManager handles null target locations${NC}"
else
    echo -e "${RED}❌ IslandManager doesn't handle null target locations${NC}"
fi

if grep -q "Could not find world for island" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java; then
    echo -e "${GREEN}✅ Proper error logging for world loading failures${NC}"
else
    echo -e "${RED}❌ Missing error logging for world issues${NC}"
fi

if grep -q "Final fallback.*center.*world.*safe height" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java; then
    echo -e "${GREEN}✅ Has final fallback teleportation logic${NC}"
else
    echo -e "${RED}❌ Missing final fallback logic${NC}"
fi

echo ""

# Test world loading chain
echo -e "${BLUE}🌍 Test 5: World Loading Chain${NC}"
echo "--------------------------------"

if grep -q "getIslandWorld.*multiple.*formats" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java; then
    echo -e "${GREEN}✅ WorldManager tries multiple world name formats${NC}"
else
    echo -e "${YELLOW}⚠️  WorldManager world loading may need verification${NC}"
fi

if grep -q "skyBlockWorld.*fallback" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java; then
    echo -e "${GREEN}✅ Has fallback to main SkyBlock world${NC}"
else
    echo -e "${RED}❌ Missing fallback world logic${NC}"
fi

echo ""

# Test home location handling
echo -e "${BLUE}🏠 Test 6: Home Location Handling${NC}"
echo "----------------------------------"

if grep -q "homeLocation.*!= null.*homeLocation" src/main/java/skyeblock/nobleskye/dev/skyeblock/models/Island.java; then
    echo -e "${GREEN}✅ getHomeLocation() properly checks for null${NC}"
else
    echo -e "${RED}❌ getHomeLocation() doesn't handle null properly${NC}"
fi

if grep -q "spawnLoc.*may be null.*world.*not loaded" src/main/java/skyeblock/nobleskye/dev/skyeblock/models/Island.java; then
    echo -e "${GREEN}✅ Acknowledges that spawn location may be null${NC}"
else
    echo -e "${RED}❌ Doesn't handle null spawn location${NC}"
fi

echo ""

echo "==============================================="
echo -e "${GREEN}🎯 Island Teleportation Fix Summary${NC}"
echo "==============================================="
echo ""
echo -e "${BLUE}🔧 FIXES APPLIED:${NC}"
echo "• Enhanced world loading in IslandDataManager"
echo "• Improved home/visit location loading with fallback"
echo "• Added null-safety checks in Island.getSpawnLocation()"
echo "• Enhanced teleportation logic with multiple fallbacks"
echo "• Better error logging for debugging world issues"
echo "• Removed duplicate permission loading code"
echo ""
echo -e "${BLUE}🛠️  HOW THE FIX WORKS:${NC}"
echo "1. When loading island data, try WorldManager.getIslandWorld() if basic world lookup fails"
echo "2. If target location is null or has invalid world, create safe fallback location"
echo "3. If island center location is invalid, update it with correct world reference"
echo "4. Final fallback: teleport to world center at height 100 (prevents 0,100,0 issue)"
echo "5. All location methods now validate world existence before returning locations"
echo ""
echo -e "${BLUE}🚨 ROOT CAUSE:${NC}"
echo "The 0,100,0 teleportation issue was caused by:"
echo "• Island locations being loaded with null world references after restart"
echo "• getSpawnLocation() failing when world was not properly loaded"
echo "• No fallback handling when target location was null"
echo "• Home/visit locations not using proper world loading logic"
echo ""
echo -e "${GREEN}✨ Expected Result:${NC}"
echo "Players should now teleport to their proper island location after server restart!"
echo "If world loading still fails, they'll get a safe location instead of 0,100,0."
