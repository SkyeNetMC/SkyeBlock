#!/bin/bash

# Deploy and Test Script for SkyeBlock Server Brand Feature

echo "🚀 Deploy and Test SkyeBlock Server Brand Feature"
echo "=================================================="
echo

# Define colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration (modify these variables)
SERVER_DIR="${SERVER_DIR:-/path/to/test/server}"
JAVA_CMD="${JAVA_CMD:-java}"
MEMORY="2G"

# Check if SERVER_DIR is set correctly
if [ "$SERVER_DIR" == "/path/to/test/server" ]; then
    echo -e "${YELLOW}⚠️ Warning: Please edit this script and set SERVER_DIR to your test server location${NC}"
    echo -e "${YELLOW}   You can also run with: SERVER_DIR=/path/to/server ./deploy-and-test.sh${NC}"
    echo
    read -p "Continue with current directory? (y/n): " choice
    if [ "$choice" != "y" ]; then
        exit 1
    fi
fi

# Check if server directory exists
if [ ! -d "$SERVER_DIR" ]; then
    echo -e "${RED}❌ Error: Server directory not found at $SERVER_DIR${NC}"
    echo "   Please ensure the directory exists and try again."
    exit 1
fi

# Step 1: Build the plugin
echo -e "${BLUE}📦 Building the plugin...${NC}"
./build.sh

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Build failed!${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Plugin built successfully${NC}"
echo

# Step 2: Deploy to test server
echo -e "${BLUE}🚚 Deploying plugin to test server...${NC}"

# Create plugins directory if it doesn't exist
mkdir -p "$SERVER_DIR/plugins"

# Copy the plugin JAR
cp target/skyeblock-1.0.0.jar "$SERVER_DIR/plugins/"

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to copy plugin to server directory${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Plugin deployed to test server${NC}"
echo

# Step 3: Create a simple start script for testing
TEST_SCRIPT="$SERVER_DIR/start-test.sh"
echo -e "${BLUE}📝 Creating server test startup script...${NC}"

cat > "$TEST_SCRIPT" << EOF
#!/bin/bash
echo "Starting test server to verify server brand..."
# Add -Dcom.mojang.eula.agree=true if you agree to Mojang's EULA
$JAVA_CMD -Xms${MEMORY} -Xmx${MEMORY} -jar spigot.jar --nogui
EOF

chmod +x "$TEST_SCRIPT"

echo -e "${GREEN}✅ Created server test startup script${NC}"
echo

# Display next steps
echo -e "${BLUE}📋 Next steps for testing:${NC}"
echo -e "1. Navigate to your server directory: ${YELLOW}cd $SERVER_DIR${NC}"
echo -e "2. Start the server with: ${YELLOW}./start-test.sh${NC}"
echo -e "3. Once the server is running, join with a Minecraft client"
echo -e "4. Press F3 in-game to open the debug screen"
echo -e "5. Look for the server brand: ${GREEN}LegitiSkyeSlimePaper${NC}"
echo -e "6. Check server logs for confirmation messages about the brand change"
echo
echo -e "${BLUE}📊 Expected server log entries:${NC}"
echo -e "- \"Server brand set to: LegitiSkyeSlimePaper\""
echo -e "- \"Successfully modified server brand to: LegitiSkyeSlimePaper\""
echo -e "- \"Updated server brand for [player] using [method]\""
echo
echo -e "${YELLOW}⚠️ Note: Make sure your server is running Paper or Spigot for best compatibility${NC}"
echo -e "${YELLOW}   If using other server software, some methods may not work${NC}"
