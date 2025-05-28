#!/bin/bash

echo "=== ASP Server: Remove Conflicting ASWM Plugin ==="
echo ""
echo "Since you're using ASP server (which has built-in ASWM),"
echo "you need to REMOVE any ASWM plugin to avoid conflicts."
echo ""

# Function to find ASWM plugin files
find_aswm_plugins() {
    local server_dir="$1"
    if [ ! -d "$server_dir/plugins" ]; then
        echo "❌ Plugins directory not found: $server_dir/plugins"
        return 1
    fi
    
    echo "🔍 Searching for ASWM plugin files in: $server_dir/plugins"
    echo ""
    
    # Look for ASWM-related files
    local found_files=0
    
    for pattern in "*aswm*" "*AdvancedSlimeWorldManager*" "*SlimeWorldManager*" "*slime*world*"; do
        for file in "$server_dir/plugins"/$pattern; do
            if [ -f "$file" ]; then
                echo "❌ Found conflicting file: $(basename "$file")"
                echo "   Full path: $file"
                found_files=$((found_files + 1))
            fi
        done
    done
    
    if [ $found_files -eq 0 ]; then
        echo "✅ No ASWM plugin files found - good!"
        echo "   ASP built-in ASWM should work without conflicts"
    else
        echo ""
        echo "⚠️  Found $found_files conflicting ASWM plugin file(s)"
        echo "   These need to be removed for ASP built-in ASWM to work properly"
    fi
    
    return $found_files
}

# Function to remove ASWM plugins
remove_aswm_plugins() {
    local server_dir="$1"
    echo ""
    echo "🗑️  Removing conflicting ASWM plugin files..."
    
    local removed_files=0
    
    for pattern in "*aswm*" "*AdvancedSlimeWorldManager*" "*SlimeWorldManager*" "*slime*world*"; do
        for file in "$server_dir/plugins"/$pattern; do
            if [ -f "$file" ]; then
                echo "   Removing: $(basename "$file")"
                if rm "$file"; then
                    echo "   ✅ Removed successfully"
                    removed_files=$((removed_files + 1))
                else
                    echo "   ❌ Failed to remove"
                fi
            fi
        done
    done
    
    echo ""
    echo "✅ Removed $removed_files conflicting plugin file(s)"
}

# Main script
echo "Instructions:"
echo "1. Copy this script to your server directory"
echo "2. Run it from your server root (where plugins/ folder is)"
echo "3. Or specify your server path as argument"
echo ""

# Get server directory
if [ -n "$1" ]; then
    SERVER_DIR="$1"
elif [ -d "plugins" ]; then
    SERVER_DIR="."
else
    echo "Usage: $0 [server_directory]"
    echo ""
    echo "Examples:"
    echo "  $0 /path/to/your/server"
    echo "  $0 ~/minecraft-server"
    echo ""
    echo "Or run this script from your server directory (where plugins/ folder is)"
    exit 1
fi

echo "🎯 Server Directory: $SERVER_DIR"
echo ""

# Check if server is running
if pgrep -f "java.*$SERVER_DIR" > /dev/null; then
    echo "⚠️  WARNING: Server appears to be running!"
    echo "   Please stop the server before removing plugins"
    echo ""
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Cancelled. Stop the server and run this script again."
        exit 1
    fi
fi

# Find ASWM plugins
find_aswm_plugins "$SERVER_DIR"
conflict_count=$?

if [ $conflict_count -gt 0 ]; then
    echo ""
    read -p "Remove these conflicting files? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        remove_aswm_plugins "$SERVER_DIR"
        echo ""
        echo "🎉 Conflict resolution complete!"
        echo ""
        echo "Next steps:"
        echo "1. Copy your updated SkyeBlock plugin to plugins/"
        echo "2. Start your ASP server"
        echo "3. Check logs for 'ASWM Type: BUILTIN_ASWM'"
        echo "4. Test with: /island status"
    else
        echo "Cancelled. No files were removed."
    fi
else
    echo ""
    echo "🎉 No conflicts found! Your setup looks good."
    echo ""
    echo "Make sure to:"
    echo "1. Copy your updated SkyeBlock plugin to plugins/"
    echo "2. Restart your ASP server completely"
    echo "3. Test with: /island status"
fi

echo ""
echo "=== Summary ==="
echo "✅ ASP Server: Built-in ASWM (keep this)"
echo "❌ ASWM Plugin: Conflicts with built-in (remove this)"
echo "✅ SkyeBlock: Will detect ASP built-in ASWM automatically"
