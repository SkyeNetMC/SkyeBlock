#!/bin/bash

# ASWM Integration Fix Guide
echo "🔧 SkyeBlock ASWM Integration Fix Guide"
echo "======================================="
echo

echo "📋 Steps to diagnose and fix ASWM integration:"
echo

echo "1️⃣  COPY THE NEW PLUGIN:"
echo "   cp target/skyeblock-1.0.0.jar /path/to/your/server/plugins/"
echo "   # Replace with your actual server path"
echo

echo "2️⃣  VERIFY ASWM INSTALLATION:"
echo "   Download ASWM from: https://infernalsuite.com/docs/asp/"
echo "   Ensure you have the PLUGIN version, not the server jar"
echo "   Place AdvancedSlimeWorldManager-X.X.X.jar in plugins/"
echo

echo "3️⃣  CHECK PLUGIN LOADING ORDER:"
echo "   ASWM must load BEFORE SkyeBlock"
echo "   Check server startup logs for loading order"
echo

echo "4️⃣  RESTART YOUR SERVER COMPLETELY:"
echo "   Stop server completely (not just reload)"
echo "   Start server fresh"
echo "   Monitor startup logs carefully"
echo

echo "5️⃣  TEST THE INTEGRATION:"
echo "   Run '/island status' in-game (as admin/op)"
echo "   Should show: 'World Manager: SlimeWorldManager (ASWM)'"
echo "   If it shows 'Standard Bukkit Worlds', ASWM is not detected"
echo

echo "6️⃣  CHECK SERVER LOGS:"
echo "   Look for these messages in logs/latest.log:"
echo "   ✅ 'Found AdvancedSlimeWorldManager plugin, attempting to initialize...'"
echo "   ✅ 'Advanced Slime World Manager integration initialized successfully!'"
echo "   ❌ 'Failed to initialize ASWM integration: ...'"
echo

echo "📞 COMMON ISSUES:"
echo
echo "❌ Issue: 'No compatible SlimeWorldManager found'"
echo "   Solution: Install ASWM plugin, restart server"
echo
echo "❌ Issue: 'Failed to initialize ASWM integration'"
echo "   Solution: Check ASWM version compatibility (use 3.0.0+)"
echo
echo "❌ Issue: 'getLoader returned null'"
echo "   Solution: ASWM configuration issue, check ASWM config.yml"
echo

echo "🔍 DEBUGGING COMMANDS:"
echo "   ./debug-aswm.sh                    # Run diagnostics"
echo "   grep -i 'slime' logs/latest.log    # Check logs for ASWM messages"
echo "   /island status                     # In-game status check"
echo

echo "📈 EXPECTED BENEFITS WHEN WORKING:"
echo "   • Island creation: Instant (instead of 5-10 seconds)"
echo "   • Memory usage: ~95% reduction per island"
echo "   • Disk space: ~90% reduction per island"
echo "   • Better server performance overall"
echo

echo "✨ The enhanced plugin now provides detailed logging!"
echo "   Check your server logs for specific error messages."
