#!/bin/bash

echo "🔍 Island Reload Logic Verification"
echo "=================================="

# Check the exact teleportToIsland method
echo "1. Checking teleportToIsland method content:"
echo "============================================"
grep -A 30 "public boolean teleportToIsland(Player player) {" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java

echo ""
echo "2. Checking for loadIsland call:"
echo "==============================="
if grep -A 25 "teleportToIsland.*Player player.*{" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java | grep -q "loadIsland"; then
    echo "✅ PASS: Island reload logic found in teleportToIsland"
    echo "Found line:"
    grep -A 25 "teleportToIsland.*Player player.*{" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java | grep "loadIsland"
else
    echo "❌ FAIL: Island reload logic missing"
fi

echo ""
echo "3. Checking loadIsland method in IslandDataManager:"
echo "================================================="
if grep -q "loadIsland.*UUID" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandDataManager.java; then
    echo "✅ PASS: loadIsland method exists in IslandDataManager"
    echo "Method signature:"
    grep -A 2 "loadIsland.*UUID" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandDataManager.java
else
    echo "❌ FAIL: loadIsland method missing in IslandDataManager"
fi

echo ""
echo "4. Complete reload logic flow:"
echo "============================"
echo "teleportToIsland -> getIsland -> if null -> dataManager.loadIsland -> put back in cache"
echo ""
echo "Searching for complete flow:"
grep -A 35 "public boolean teleportToIsland(Player player)" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java | grep -E "(island == null|loadIsland|playerIslands.put)" | head -5
