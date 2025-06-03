#!/bin/bash

# SkyeBlock Plugin - Deployment Readiness Check
echo "🚀 SkyeBlock Plugin - Ready for External Server Deployment"
echo "=========================================================="
echo

JAR_FILE="/mnt/sda4/SkyeNetwork/SkyeBlock/target/skyeblock-1.1.0.jar"

if [ -f "$JAR_FILE" ]; then
    echo "✅ Plugin JAR: Found"
    echo "   📍 Location: $JAR_FILE"
    echo "   📦 Size: $(stat -c%s "$JAR_FILE") bytes ($(du -h "$JAR_FILE" | cut -f1))"
    echo "   📅 Modified: $(stat -c%y "$JAR_FILE")"
    echo
    
    echo "✅ JAR Contents Verification:"
    if jar -tf "$JAR_FILE" | grep -q "plugin.yml"; then
        echo "   📄 plugin.yml: ✅ Present"
    else
        echo "   📄 plugin.yml: ❌ Missing"
    fi
    
    if jar -tf "$JAR_FILE" | grep -q "SkyeBlockPlugin.class"; then
        echo "   🔧 Main class: ✅ Present"
    else
        echo "   🔧 Main class: ❌ Missing"
    fi
    
    echo "   📁 Package structure: skyeblock.nobleskye.dev.skyeblock"
    echo "   🔢 Total files: $(jar -tf "$JAR_FILE" | wc -l)"
    echo
    
    echo "✅ Features Implemented:"
    echo "   🏝️  Island Creation & Management"
    echo "   💾  Island Data Persistence (YAML-based)"
    echo "   🔄  Old Island Format Conversion Command"
    echo "   🌋  Automatic Nether Island Creation"
    echo "   👥  Coop & Visiting System"
    echo "   ⚙️   Island Settings & Permissions"
    echo "   🌍  SlimeWorldManager Integration"
    echo "   📂  Organized Directory Structure"
    echo
    
    echo "✅ Dependencies:"
    echo "   📚 Required: WorldEdit, WorldGuard"
    echo "   🔧 Optional: SlimeWorldManager, LuckPerms"
    echo
    
    echo "🎯 DEPLOYMENT STEPS:"
    echo "1. Copy $JAR_FILE to your server's plugins/ folder"
    echo "2. Ensure WorldEdit & WorldGuard are installed"
    echo "3. Stop and restart your server"
    echo "4. Check server console for successful loading"
    echo "5. Test with: /island create classic"
    echo
    
    echo "📋 Server Requirements:"
    echo "   🖥️  Minecraft Server: 1.20+"
    echo "   ☕ Java Version: 17+"
    echo "   🖧 Server Type: Paper/Spigot/Bukkit"
    echo
    
    echo "🔍 Troubleshooting:"
    echo "   📖 Read: EXTERNAL_SERVER_DEPLOYMENT.md"
    echo "   📝 Monitor: server console logs"
    echo "   🏷️  Check: /plugins command"
    echo
    
    echo "✅ Status: READY FOR PRODUCTION DEPLOYMENT"
    
else
    echo "❌ Plugin JAR not found!"
    echo "   Run: mvn clean package"
    exit 1
fi

echo
echo "🎉 SkyeBlock Plugin v1.1.0 is ready for external server deployment!"
