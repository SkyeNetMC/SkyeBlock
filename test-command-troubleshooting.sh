#!/bin/bash

echo "=== SkyeBlock Command Verification & Troubleshooting Guide ==="
echo

echo "🔧 TROUBLESHOOTING STEPS:"
echo "=========================="
echo

echo "1. VERIFY PLUGIN IS LOADED:"
echo "   In-game, run: /plugins"
echo "   Look for 'SkyeBlock' in green text"
echo "   If red or missing, check server logs for errors"
echo

echo "2. CHECK PERMISSIONS:"
echo "   Make sure you have admin permissions:"
echo "   • skyeblock.admin - for /sba command access"
echo "   • skyeblock.admin.bypass - for bypassing restrictions"
echo "   • skyeblock.* - for all permissions"
echo

echo "3. VERIFY COMMANDS EXIST:"
echo "   Try these commands in-game:"
echo "   • /sba (should show admin help)"
echo "   • /island (should work for basic island management)"
echo "   • /sb (should show regular help)"
echo

echo "4. IF /sba DOESN'T WORK:"
echo "   a) Check server console for errors like:"
echo "      'Failed to register sba command'"
echo "   b) Try the alias: /skyeblockadmin"
echo "   c) Use regular commands with admin permissions:"
echo "      - /island delete (admin can bypass restrictions)"
echo "      - /delete <player> (admin delete others' islands)"
echo

echo "5. MANUAL ADMIN BYPASS:"
echo "   Even without /sba, admins can bypass restrictions:"
echo "   • Island deletion cooldowns are bypassed"
echo "   • Can delete at max tries (3/3)"
echo "   • Can delete other players' islands"
echo "   • Can visit islands with full permissions"
echo

echo "6. IMPLEMENTED FEATURES (working now):"
echo "   ✅ Teleport to spawn if no island found"
echo "   ✅ 1-hour cooldown (instead of 5 minutes)"
echo "   ✅ Cooldown persists even after 3 deletions"
echo "   ✅ Cannot delete when on 3rd island (unless admin)"
echo "   ✅ Admin bypass with skyeblock.admin permission"
echo

echo "7. TEST THE FEATURES:"
echo "   As regular player:"
echo "   • Try /island delete (should show cooldown/limits)"
echo "   • Try /island home when no island (should go to spawn)"
echo "   "
echo "   As admin (with skyeblock.admin):"
echo "   • /island delete bypasses all restrictions"
echo "   • /delete <player> works on others' islands"
echo

echo "8. SERVER RESTART:"
echo "   If commands still don't work:"
echo "   a) Copy the new plugin: cp target/skyeblock-3.1.0.jar /path/to/server/plugins/"
echo "   b) Restart the server (not reload)"
echo "   c) Check console for 'SkyeBlock enabled' message"
echo

echo "9. CURRENT CONFIG CHANGES:"
echo "   • Cooldown is now 3600 seconds (1 hour)"
echo "   • Max tries still 3, but cooldown persists"
echo "   • Admin permissions bypass all restrictions"
echo

echo "🚀 Ready for testing! All features implemented."
echo "   If /sba still doesn't work, the admin bypass works"
echo "   through the skyeblock.admin permission on regular commands."
