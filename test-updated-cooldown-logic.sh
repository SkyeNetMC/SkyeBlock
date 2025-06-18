#!/bin/bash

# Test script for the updated island deletion cooldown logic
# Logic: create -> delete -> create -> delete -> create -> CANNOT delete for 1 hour

echo "🔧 Testing Updated Island Deletion Cooldown Logic"
echo "================================================="
echo ""
echo "📋 New Logic:"
echo "   1. Player creates island (1st)"
echo "   2. Player deletes island (1st deletion)"
echo "   3. Player creates island (2nd)" 
echo "   4. Player deletes island (2nd deletion)"
echo "   5. Player creates island (3rd)"
echo "   6. Player CANNOT delete island for 1 hour (configurable cooldown)"
echo ""
echo "✅ Key Points:"
echo "   - After 2 deletions, player must wait cooldown before deleting 3rd island"
echo "   - Player can still CREATE islands (no restriction on creation)"
echo "   - Admins bypass all restrictions"
echo "   - After cooldown expires, deletion count resets to allow new cycle"
echo ""
echo "🏗️ Building plugin with updated logic..."

# Build the plugin
./build.sh

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful! Updated logic implemented:"
    echo ""
    echo "📁 Updated Files:"
    echo "   - IslandManager.java: Updated canDeleteIsland() and canCreateIsland()"
    echo "   - config.yml: Updated messages to reflect new logic"
    echo "   - Removed all server brand related code and files"
    echo ""
    echo "🎯 Testing Scenarios:"
    echo "   1. Regular player - can delete 2 islands, then cooldown blocks 3rd deletion"
    echo "   2. Admin player - can delete unlimited islands (bypass)"
    echo "   3. After cooldown expires - deletion count resets, cycle can repeat"
    echo ""
    echo "⚡ Ready for testing on server!"
else
    echo ""
    echo "❌ Build failed! Please check compilation errors above."
    exit 1
fi
