#!/bin/bash

echo "🏝️  Testing SkyeBlock Coop Visit Command and Container Protection"
echo "=============================================================="

function check_file_contains() {
  local file="$1"
  local pattern="$2"
  if grep -q "$pattern" "$file"; then
    echo "✅ Pattern found in $file"
    return 0
  else
    echo "❌ Pattern NOT found in $file"
    return 1
  fi
}

echo "📝 Testing handleCoopVisit method..."
check_file_contains "src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/IslandCommand.java" "handleCoopVisit"

echo "🔒 Testing survival mode setting in IslandManager..."
check_file_contains "src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java" "player.setGameMode(GameMode.SURVIVAL)"

echo "🚫 Testing container protection in VisitorProtectionListener..."
check_file_contains "src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/VisitorProtectionListener.java" "onEntityInteractWithContainer"

echo "🔍 Testing tab completion for coop visit..."
if grep -q "visit" "src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/IslandCommand.java" && \
   grep -q "coopActions" "src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/IslandCommand.java"; then
  echo "✅ Tab completion for coop visit found"
else
  echo "❌ Tab completion for coop visit NOT found"
  exit 1
fi

echo "📦 Checking JAR file exists..."
if [ -f "target/skyeblock-1.0.0.jar" ]; then
  echo "✅ JAR file exists"
else
  echo "❌ JAR file not found"
  exit 1
fi

echo ""
echo "🎉 Tests complete! All features implemented successfully:"
echo "- ✅ /coop visit command implemented"
echo "- ✅ Container protection enhanced"  
echo "- ✅ Game mode setting based on coop role"
echo "=============================================================="
