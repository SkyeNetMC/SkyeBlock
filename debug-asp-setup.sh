#!/bin/bash

# ASP Server + ASWM Plugin Conflict Resolution Script
echo "🔧 ASP Server + ASWM Plugin Setup Analysis"
echo "==========================================="
echo

echo "📋 Your Setup: ASP Server + ASWM Plugin"
echo "---------------------------------------"
echo "You mentioned having both:"
echo "• ASP (ASWM) Server Jar - Built-in ASWM"
echo "• ASWM Plugin - Separate plugin"
echo
echo "⚠️  POTENTIAL CONFLICT:"
echo "Having both can cause detection issues!"
echo

echo "🔍 Checking your current setup..."
echo

# Check plugins directory
echo "📁 Plugin Directory Analysis:"
echo "-----------------------------"
if [ -d plugins/ ]; then
    echo "Plugins directory found. Checking for ASWM-related plugins:"
    
    if ls plugins/ | grep -i "AdvancedSlimeWorldManager" > /dev/null 2>&1; then
        echo "🔴 ASWM Plugin found:"
        ls -la plugins/ | grep -i "AdvancedSlimeWorldManager"
        echo "   ⚠️  This might conflict with your ASP server!"
    else
        echo "✅ No ASWM plugin found (good for ASP server setup)"
    fi
    
    if ls plugins/ | grep -i "SlimeWorldManager" > /dev/null 2>&1; then
        echo "🔴 Legacy SWM Plugin found:"
        ls -la plugins/ | grep -i "SlimeWorldManager"
        echo "   ⚠️  Remove this when using ASP server!"
    fi
    
    if ls plugins/ | grep -i "skyeblock" > /dev/null 2>&1; then
        echo "✅ SkyeBlock plugin found:"
        ls -la plugins/ | grep -i "skyeblock"
    else
        echo "❌ SkyeBlock plugin not found"
        echo "   Copy target/skyeblock-1.0.0.jar to plugins/"
    fi
else
    echo "❌ No plugins directory found"
    echo "   Run this from your server directory"
fi

echo

echo "🎯 RECOMMENDED SOLUTIONS:"
echo "========================"
echo

echo "Option 1: Use ASP Server Only (Recommended)"
echo "-------------------------------------------"
echo "✅ Keep: ASP server jar (built-in ASWM)"
echo "❌ Remove: ASWM plugin from plugins/"
echo "✅ Keep: SkyeBlock plugin"
echo
echo "Commands:"
echo "  rm plugins/AdvancedSlimeWorldManager*.jar    # Remove ASWM plugin"
echo "  # Keep your ASP server jar as the main server"
echo

echo "Option 2: Use Regular Paper + ASWM Plugin"
echo "-----------------------------------------"
echo "❌ Replace: ASP server with regular Paper"
echo "✅ Keep: ASWM plugin in plugins/"
echo "✅ Keep: SkyeBlock plugin"
echo
echo "Commands:"
echo "  # Download regular Paper server jar"
echo "  # Keep ASWM plugin in plugins/"
echo

echo

echo "🔧 DETECTION ENHANCEMENT:"
echo "========================"
echo "The SkyeBlock plugin has been enhanced to detect both:"
echo "• Built-in ASWM (from ASP server)"
echo "• Plugin-based ASWM"
echo
echo "But conflicts can occur when both are present!"

echo

echo "📝 TESTING STEPS:"
echo "=================="
echo
echo "1. Choose one option above"
echo "2. Copy the updated SkyeBlock plugin:"
echo "   cp target/skyeblock-1.0.0.jar plugins/"
echo
echo "3. Start your server and check logs for:"
echo "   ✅ 'Found AdvancedSlimeWorldManager plugin, attempting to initialize...'"
echo "   ✅ 'Advanced Slime World Manager integration initialized successfully!'"
echo
echo "4. Test in-game with '/island status' (as admin)"
echo "   Should show: 'World Manager: SlimeWorldManager (ASWM)'"
echo
echo "5. Create a test island and check creation speed:"
echo "   '/island create classic'"
echo "   Should be instant with ASWM working"

echo

echo "🚨 TROUBLESHOOTING:"
echo "==================="
echo
echo "If still showing 'Standard Bukkit Worlds':"
echo
echo "1. Check server startup logs carefully"
echo "2. Look for plugin loading order issues"
echo "3. Ensure only ONE ASWM source (built-in OR plugin)"
echo "4. Try with a fresh server restart"
echo
echo "Debug commands:"
echo "  grep -i 'slime' logs/latest.log | tail -20"
echo "  grep -i 'skyeblock' logs/latest.log | tail -10"

echo
echo "💡 The enhanced SkyeBlock plugin now provides detailed"
echo "   logging to help identify exactly what's happening!"
