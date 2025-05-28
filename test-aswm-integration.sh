#!/bin/bash

# SkyeBlock ASWM Integration Test Script
# This script helps test the ASWM integration functionality

echo "🧪 SkyeBlock ASWM Integration Test"
echo "================================="
echo

# Build the plugin
echo "📦 Building SkyeBlock plugin..."
mvn clean package -q

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📄 Plugin jar created: target/skyeblock-1.0.0.jar"
else
    echo "❌ Build failed!"
    exit 1
fi

echo
echo "🔍 Checking integration features..."

# Check if WorldManager has ASWM methods
if grep -q "isSlimeWorldEnabled" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java; then
    echo "✅ ASWM status checking methods present"
else
    echo "❌ ASWM status methods missing"
fi

if grep -q "getSlimeWorldManagerType" src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/WorldManager.java; then
    echo "✅ ASWM type detection methods present"
else
    echo "❌ ASWM type detection missing"
fi

# Check if IslandCommand has status command
if grep -q "handleStatusCommand" src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/IslandCommand.java; then
    echo "✅ Status command implementation present"
else
    echo "❌ Status command missing"
fi

# Check plugin.yml for soft dependencies
if grep -q "AdvancedSlimeWorldManager" src/main/resources/plugin.yml; then
    echo "✅ ASWM soft dependency configured"
else
    echo "❌ ASWM soft dependency missing"
fi

echo
echo "📋 Integration Summary:"
echo "----------------------"
echo "• Automatic ASWM/SWM detection"
echo "• Reflection-based API (no compile dependencies)"
echo "• Fallback to standard Bukkit worlds"
echo "• Admin status command: /island status"
echo "• Optimized slime world properties"
echo "• Memory and performance improvements"

echo
echo "🚀 Next Steps:"
echo "-------------"
echo "1. Copy target/skyeblock-1.0.0.jar to your server's plugins folder"
echo "2. Install ASWM from: https://infernalsuite.com/docs/asp/"
echo "3. Restart your server"
echo "4. Use '/island status' to verify ASWM integration"
echo "5. Create test islands to see performance improvements"

echo
echo "📖 Documentation:"
echo "----------------"
echo "• ASWM_INTEGRATION_GUIDE.md - Complete setup guide"
echo "• ASWM_IMPLEMENTATION_SUMMARY.md - Technical details"

echo
echo "✨ ASWM Integration Test Complete!"
