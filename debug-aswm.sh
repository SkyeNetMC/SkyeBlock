#!/bin/bash

# SkyeBlock ASWM Debugging Script
echo "🔍 SkyeBlock ASWM Integration Debug"
echo "=================================="
echo

# Check if server is running
echo "📊 Server Status Check:"
echo "-----------------------"
if pgrep -f "java.*paper" > /dev/null || pgrep -f "java.*spigot" > /dev/null; then
    echo "✅ Minecraft server is running"
else
    echo "❌ Minecraft server is not running"
    echo "   Please start your server first"
    exit 1
fi

echo

# Check plugins directory
echo "📁 Plugin Installation Check:"
echo "------------------------------"
if ls plugins/ | grep -i "AdvancedSlimeWorldManager" > /dev/null 2>&1; then
    echo "✅ ASWM plugin found in plugins directory:"
    ls -la plugins/ | grep -i "AdvancedSlimeWorldManager"
else
    echo "❌ ASWM plugin not found in plugins directory"
    echo "   Install ASWM from: https://infernalsuite.com/docs/asp/"
fi

if ls plugins/ | grep -i "SlimeWorldManager" > /dev/null 2>&1; then
    echo "✅ Legacy SWM plugin found:"
    ls -la plugins/ | grep -i "SlimeWorldManager"
fi

if ls plugins/ | grep -i "skyeblock" > /dev/null 2>&1; then
    echo "✅ SkyeBlock plugin found:"
    ls -la plugins/ | grep -i "skyeblock"
else
    echo "❌ SkyeBlock plugin not found"
    echo "   Copy target/skyeblock-1.0.0.jar to plugins/"
fi

echo

# Check server logs for ASWM detection
echo "📋 Server Log Analysis:"
echo "-----------------------"
if [ -f logs/latest.log ]; then
    echo "Recent ASWM/SWM related messages:"
    
    # Look for SkyeBlock ASWM messages
    if grep -i "slime.*integration.*initialized" logs/latest.log | tail -5; then
        echo "✅ Found ASWM integration messages"
    else
        echo "⚠️  No ASWM integration success messages found"
    fi
    
    echo
    echo "Plugin loading order:"
    grep -E "(Enabling|Loading) (AdvancedSlimeWorldManager|SlimeWorldManager|SkyeBlock)" logs/latest.log | tail -10
    
    echo
    echo "Any ASWM/SWM errors:"
    grep -i -E "(slime|aswm).*error|error.*slime" logs/latest.log | tail -5
    
else
    echo "❌ Cannot find logs/latest.log"
    echo "   Run this script from your server directory"
fi

echo

# Provide troubleshooting steps
echo "🛠️  Troubleshooting Steps:"
echo "---------------------------"
echo "1. Ensure ASWM loads BEFORE SkyeBlock:"
echo "   - Check plugin loading order in logs"
echo "   - ASWM should load first, then SkyeBlock"
echo
echo "2. Verify ASWM version compatibility:"
echo "   - Use ASWM 3.0.0 or newer"
echo "   - Download from: https://infernalsuite.com/docs/asp/"
echo
echo "3. Check for errors:"
echo "   - Look for any ASWM initialization errors in logs"
echo "   - Ensure Java version compatibility"
echo
echo "4. Test manually:"
echo "   - Run '/island status' in-game (as admin)"
echo "   - Should show 'SlimeWorldManager (ASWM)'"
echo
echo "5. Restart server:"
echo "   - Stop server completely"
echo "   - Start server again"
echo "   - Monitor startup logs carefully"

echo
echo "📞 If issues persist:"
echo "   - Check logs/latest.log for detailed error messages"
echo "   - Ensure you have the latest versions of both plugins"
echo "   - Test without ASWM to confirm SkyeBlock works with standard worlds"
