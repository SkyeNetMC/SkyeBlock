#!/bin/bash

echo "🏝️  Verifying SkyeBlock Implementation"
echo "=============================================================="

# Check for file existence
echo "Checking files..."
for file in \
  "src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/IslandCommand.java" \
  "src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/VisitorProtectionListener.java" \
  "src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java" \
  "target/skyeblock-1.0.0.jar"
do
  if [ -f "$file" ]; then
    echo "✅ $file exists"
  else
    echo "❌ $file does not exist"
    exit 1
  fi
done

# Check for specific strings in files
echo ""
echo "Checking for handleCoopVisit in IslandCommand.java..."
grep -n "handleCoopVisit" src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/IslandCommand.java

echo ""
echo "Checking for survival mode setting in IslandManager.java..."
grep -n "GameMode.SURVIVAL" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java

echo ""
echo "Checking for container protection in VisitorProtectionListener.java..."
grep -n "onEntityInteractWithContainer" src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/VisitorProtectionListener.java

echo ""
echo "🎉 Verification complete!"
echo "=============================================================="
