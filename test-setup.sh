#!/bin/bash
# SkyeBlock Plugin Test Setup Script

echo "=== SkyeBlock Plugin Test Setup ==="

# Check if we have a Minecraft server directory
SERVER_DIR="/mnt/sda4/minecraft-test-server"

if [ ! -d "$SERVER_DIR" ]; then
    echo "Creating test server directory at $SERVER_DIR"
    mkdir -p "$SERVER_DIR"
fi

# Copy plugin to plugins directory
echo "Copying plugin to test server..."
mkdir -p "$SERVER_DIR/plugins"
cp "/mnt/sda4/SkyeNetwork/SkyeBlock/target/skyeblock-1.0.0.jar" "$SERVER_DIR/plugins/"

echo "Plugin copied to: $SERVER_DIR/plugins/skyeblock-1.0.0.jar"

# Create basic server files if they don't exist
if [ ! -f "$SERVER_DIR/eula.txt" ]; then
    echo "Creating EULA acceptance..."
    echo "eula=true" > "$SERVER_DIR/eula.txt"
fi

if [ ! -f "$SERVER_DIR/server.properties" ]; then
    echo "Creating basic server.properties..."
    cat > "$SERVER_DIR/server.properties" << EOF
# Minecraft Test Server Properties
level-name=world
gamemode=survival
difficulty=easy
spawn-protection=0
max-players=10
online-mode=false
white-list=false
enable-command-block=true
EOF
fi

echo ""
echo "=== Test Server Setup Complete ==="
echo "Server directory: $SERVER_DIR"
echo "Plugin location: $SERVER_DIR/plugins/skyeblock-1.0.0.jar"
echo ""
echo "To test the plugin:"
echo "1. Download Paper/Spigot 1.20+ server JAR to $SERVER_DIR"
echo "2. Start the server: java -jar server.jar"
echo "3. Use commands: /island create [classic|desert|nether]"
echo ""
echo "Available commands:"
echo "  /island create <type>  - Create an island (types: classic, desert, nether)"
echo "  /island types          - Show available island types"
echo "  /island tp             - Teleport to your island"
echo "  /island help           - Show help"
echo ""
