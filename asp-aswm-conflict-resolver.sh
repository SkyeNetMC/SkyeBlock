#!/bin/bash

echo "=== ASP + ASWM Plugin Conflict Resolution Guide ==="
echo ""
echo "ISSUE DETECTED: You have BOTH ASP server (with built-in ASWM) AND ASWM plugin installed."
echo "This creates conflicts and prevents proper ASWM detection."
echo ""

# Check current setup
echo "1. CURRENT SETUP ANALYSIS:"
echo "   - ASP Server: Contains built-in ASWM (com.infernalsuite.aswm.SlimePlugin)"
echo "   - ASWM Plugin: Separate plugin providing ASWM functionality"
echo "   - Conflict: Both provide ASWM but in different ways"
echo ""

echo "2. RESOLUTION OPTIONS:"
echo ""
echo "   OPTION A: Use ASP Built-in ASWM (Recommended)"
echo "   =============================================="
echo "   1. Remove ASWM plugin from plugins folder:"
echo "      rm /path/to/server/plugins/AdvancedSlimeWorldManager-*.jar"
echo "      rm /path/to/server/plugins/SlimeWorldManager-*.jar"
echo ""
echo "   2. Keep ASP server jar (has built-in ASWM)"
echo "   3. Restart server completely"
echo "   4. SkyeBlock will auto-detect built-in ASWM"
echo ""
echo "   OPTION B: Use Regular Paper + ASWM Plugin"
echo "   =========================================="
echo "   1. Replace ASP server jar with regular Paper:"
echo "      mv asp-server.jar asp-server.jar.backup"
echo "      # Download Paper 1.20.4 from https://papermc.io/downloads"
echo ""
echo "   2. Keep ASWM plugin in plugins folder"
echo "   3. Restart server completely"
echo "   4. SkyeBlock will detect ASWM plugin"
echo ""

echo "3. STEP-BY-STEP RESOLUTION (Option A - Recommended):"
echo ""

# Check for ASWM plugin files
if [ -d "/mnt/sda4/SkyeNetwork/server" ]; then
    SERVER_DIR="/mnt/sda4/SkyeNetwork/server"
elif [ -d "../server" ]; then
    SERVER_DIR="../server"
else
    echo "   Server directory not found. Please specify your server path:"
    echo "   SERVER_PATH=/path/to/your/server"
    SERVER_DIR="SERVER_PATH"
fi

echo "   Step 1: Remove ASWM plugin (since ASP has built-in ASWM):"
if [ "$SERVER_DIR" != "SERVER_PATH" ]; then
    ASWM_FILES=$(find "$SERVER_DIR/plugins" -name "*slime*" -o -name "*ASWM*" -o -name "*AdvancedSlimeWorldManager*" 2>/dev/null)
    if [ -n "$ASWM_FILES" ]; then
        echo "   Found ASWM plugin files:"
        echo "$ASWM_FILES"
        echo ""
        echo "   To remove them, run:"
        echo "   rm $ASWM_FILES"
    else
        echo "   No ASWM plugin files found in $SERVER_DIR/plugins"
    fi
else
    echo "   find /path/to/server/plugins -name '*slime*' -delete"
    echo "   find /path/to/server/plugins -name '*ASWM*' -delete"
fi
echo ""

echo "   Step 2: Copy updated SkyeBlock plugin:"
if [ -f "target/skyeblock-1.0.0.jar" ]; then
    echo "   cp target/skyeblock-1.0.0.jar $SERVER_DIR/plugins/"
else
    echo "   Build the plugin first: mvn clean package"
    echo "   Then: cp target/skyeblock-1.0.0.jar $SERVER_DIR/plugins/"
fi
echo ""

echo "   Step 3: Restart server completely (stop + start, not reload)"
echo ""

echo "   Step 4: Test the integration:"
echo "   - Join server and run: /island status"
echo "   - Check console for: '[SkyeBlock] ASWM integration successful'"
echo "   - Look for: 'Using built-in ASWM from ASP server'"
echo ""

echo "4. VERIFICATION:"
echo "   After restart, check server logs for:"
echo "   - '[SkyeBlock] ASWM Type: BUILTIN_ASWM'"
echo "   - '[SkyeBlock] ASWM integration successful'"
echo "   - No plugin loading conflicts"
echo ""

echo "5. TROUBLESHOOTING:"
echo "   If still not working:"
echo "   - Run: ./debug-aswm.sh"
echo "   - Check server logs for class loading errors"
echo "   - Verify ASP server jar contains ASWM classes"
echo ""

echo "=== Resolution Summary ==="
echo "Problem: ASP built-in ASWM + ASWM plugin = conflict"
echo "Solution: Remove ASWM plugin, use ASP built-in ASWM only"
echo "Result: SkyeBlock will detect and use built-in ASWM properly"
echo ""
