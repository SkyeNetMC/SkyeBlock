#!/bin/bash

# Test Island Type Naming Fix
# This script validates that the CustomSchematicManager creates the correct keys

echo "=== SkyeBlock Island Type Naming Fix Test ==="
echo ""

echo "Expected behavior after fix:"
echo "1. Internal keys: 'classic', 'desert', 'nether' (from filenames)"
echo "2. Display names: 'Classic SkyBlock', 'Desert Island', 'Nether Island' (from YAML)"
echo "3. Island creation uses single-word types"
echo ""

echo "Checking schematic files and expected keys:"
for file in src/main/resources/schematics/*.yml; do
    filename=$(basename "$file" .yml)
    display_name=$(grep "^name:" "$file" | cut -d'"' -f2)
    echo "  File: $file"
    echo "    Key: '$filename' (from filename)"
    echo "    Display: '$display_name' (from YAML)"
    echo ""
done

echo "Verifying code changes:"
echo ""

# Check if the loadSchematics method uses filename-based keys
if grep -q 'String key = fileName.substring(0, fileName.lastIndexOf' src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/CustomSchematicManager.java; then
    echo "✅ loadSchematics() method correctly uses filename-based keys"
else
    echo "❌ loadSchematics() method does not use filename-based keys"
fi

# Check if schematics.put uses the key variable
if grep -q 'schematics.put(key, schematic)' src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/CustomSchematicManager.java; then
    echo "✅ Schematics are stored with filename-based keys"
else
    echo "❌ Schematics are not stored with filename-based keys"
fi

echo ""
echo "Expected island type usage:"
echo "  /island create classic      → Island type stored as: 'classic'"
echo "  /island create desert       → Island type stored as: 'desert'" 
echo "  /island create nether       → Island type stored as: 'nether'"
echo ""
echo "Display in /island types command:"
echo "  classic - The classic skyblock experience with a small platform, tree, and basic supplies"
echo "  desert - A desert-themed island with sand and cactus"
echo "  nether - A nether-themed island with netherrack and fire"
echo ""
echo "Test completed! ✅"
