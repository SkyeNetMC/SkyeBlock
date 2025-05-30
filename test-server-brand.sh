#!/bin/bash

# This script tests the server brand modification functionality

echo "🌟 Testing LegitiSkyeSlimePaper Server Brand Modification 🌟"
echo "==========================================================="
echo

# Check if JAR exists
if [ ! -f "target/skyeblock-1.0.0.jar" ]; then
    echo "❌ Error: Plugin JAR not found. Please build the plugin first with ./build.sh"
    exit 1
fi

echo "✅ Found plugin JAR: target/skyeblock-1.0.0.jar"
echo

# Display what will be modified
echo "🔧 This plugin will modify the server brand according to your configuration"
echo "   Default brand is 'LegitiSkyeSlimePaper' but can be changed in config.yml"
echo "   This will be visible to players in F3 debug screen"
echo
echo "📝 Configuration (in config.yml):"
echo "   server-brand.enabled: true       # Enable/disable the feature"
echo "   server-brand.name: 'LegitiSkyeSlimePaper'  # Your custom brand name"
echo "   server-brand.periodic-updates: true        # Periodically update the brand"
echo "   server-brand.update-interval-minutes: 10   # Update interval"
echo
echo "📝 Implementation Methods:"
echo "   1. Direct server property modification"
echo "   2. Reflection-based field modification"
echo "   3. Plugin messaging channel approach"
echo "   4. Spigot-specific brand modification"
echo

echo "🚀 To test this feature:"
echo "   1. Install the plugin on your server:"
echo "      cp target/skyeblock-1.0.0.jar /path/to/your/server/plugins/"
echo "   2. Start/restart your server"
echo "   3. Join the server and press F3 to view the debug screen"
echo "   4. Look for 'LegitiSkyeSlimePaper' in the server brand info"
echo

echo "📊 Server compatibility:"
echo "   ✅ Paper/Spigot servers"
echo "   ✅ ASP servers with built-in ASWM"
echo "   ✅ Standard Bukkit servers"
echo

echo "❓ Troubleshooting:"
echo "   If the brand isn't showing, check server logs for errors related to:"
echo "   - ServerBrandListener"
echo "   - ServerBrandChanger" 
echo "   - SpigotBrandModifier"
echo
echo "🔄 Managing the server brand in-game:"
echo "   Use the /serverbrand command (requires skyeblock.admin.serverbrand permission)"
echo "   - /serverbrand           View current settings"
echo "   - /serverbrand reload    Reload configuration"
echo "   - /serverbrand update    Update brand for all online players"
echo "   - /serverbrand set <name> Set custom brand name"
echo "   - /serverbrand toggle    Enable/disable the feature"
echo

echo "🎯 For best results, use on Paper or Spigot servers"
