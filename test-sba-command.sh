#!/bin/bash

# Test script to verify SBA command registration and functionality

echo "🔧 Testing SBA Command Registration"
echo "=================================="
echo ""

# Build the plugin first
echo "🏗️ Building plugin..."
./build.sh > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    
    # Check if SBA command is in the plugin.yml
    echo "📋 Checking command registration in plugin.yml:"
    
    # Check src/main/resources/plugin.yml (the one that gets packaged)
    if grep -q "sba:" src/main/resources/plugin.yml; then
        echo "  ✅ SBA command found in src/main/resources/plugin.yml"
        
        # Show the command definition
        echo "  📄 Command definition:"
        grep -A 4 "sba:" src/main/resources/plugin.yml | sed 's/^/    /'
        echo ""
    else
        echo "  ❌ SBA command NOT found in src/main/resources/plugin.yml"
        echo ""
    fi
    
    # Check if SkyeBlockAdminCommand class exists
    echo "📋 Checking command class:"
    if [ -f "src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/SkyeBlockAdminCommand.java" ]; then
        echo "  ✅ SkyeBlockAdminCommand.java exists"
    else
        echo "  ❌ SkyeBlockAdminCommand.java NOT found"
    fi
    
    # Check command registration in main plugin file
    echo ""
    echo "📋 Checking command registration in SkyeBlockPlugin.java:"
    if grep -q "getCommand(\"sba\")" src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java; then
        echo "  ✅ SBA command registration code found"
        echo "  📄 Registration code:"
        grep -A 3 -B 1 "getCommand(\"sba\")" src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java | sed 's/^/    /'
    else
        echo "  ❌ SBA command registration code NOT found"
    fi
    
    echo ""
    echo "🎯 SBA Command should now be available in-game!"
    echo ""
    echo "💡 Usage: /sba <subcommand> [args...]"
    echo "   Subcommands: island, create, delete, visit, hub, reload, debug, bypass"
    echo "   Permission: skyeblock.admin"
    echo "   Aliases: /skyeblockadmin"
    echo ""
    echo "🔑 Key Features:"
    echo "   - Bypasses all island restrictions"
    echo "   - Admin-only access"
    echo "   - Full command functionality"
    
else
    echo "❌ Build failed! Check compilation errors."
    exit 1
fi
