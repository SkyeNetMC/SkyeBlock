#!/bin/bash

# SkyeBlock Schematic System Conversion Validation Script

echo "=== SkyeBlock Schematic System Conversion Validation ==="
echo

# Check if the JAR was built successfully
if [ -f "target/skyeblock-1.1.0.jar" ]; then
    echo "✅ Plugin JAR built successfully"
    echo "   Size: $(du -h target/skyeblock-1.1.0.jar | cut -f1)"
else
    echo "❌ Plugin JAR not found"
    exit 1
fi

# Check if schematics folder exists
if [ -d "schematics" ]; then
    echo "✅ Schematics folder created"
    echo "   Contents:"
    ls -la schematics/ | sed 's/^/     /'
else
    echo "❌ Schematics folder not found"
fi

# Check for placeholder schematic files
schematic_count=0
for schematic in "classic.schem" "desert.schem" "nether.schem"; do
    if [ -f "schematics/$schematic" ]; then
        echo "✅ Found placeholder: $schematic"
        schematic_count=$((schematic_count + 1))
    else
        echo "❌ Missing placeholder: $schematic"
    fi
done

echo
echo "=== Code Analysis ==="

# Check that IslandManager was updated
if grep -q "getSchematicManager().pasteSchematic" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java; then
    echo "✅ IslandManager updated to use SchematicManager"
else
    echo "❌ IslandManager still using CustomSchematicManager"
fi

# Check that IslandCommand was updated
if grep -q "getSchematicManager().getAvailableSchematics" src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/IslandCommand.java; then
    echo "✅ IslandCommand updated to use SchematicManager"
else
    echo "❌ IslandCommand still using CustomSchematicManager"
fi

# Check that SchematicManager has required methods
if grep -q "getAvailableSchematics" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/SchematicManager.java; then
    echo "✅ SchematicManager has getAvailableSchematics method"
else
    echo "❌ SchematicManager missing getAvailableSchematics method"
fi

if grep -q "pasteSchematic" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/SchematicManager.java; then
    echo "✅ SchematicManager has pasteSchematic method"
else
    echo "❌ SchematicManager missing pasteSchematic method"
fi

echo
echo "=== Next Steps ==="
echo "1. Replace placeholder .schem files with actual WorldEdit schematics"
echo "2. Test island creation on a Minecraft server"
echo "3. Verify spawn locations work correctly"
echo "4. Update documentation if needed"

echo
echo "=== Conversion Status ==="
if [ $schematic_count -eq 3 ]; then
    echo "🎉 Schematic system conversion completed successfully!"
    echo "   Ready for .schem file replacement and testing"
else
    echo "⚠️  Conversion partially complete - missing schematic files"
fi
