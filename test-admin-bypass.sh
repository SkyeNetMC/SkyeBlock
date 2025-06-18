#!/bin/bash

# SkyeBlock Admin Bypass System Test
# Tests the /sba command system and admin bypass functionality

echo "🔑 Testing SkyeBlock Admin Bypass System (/sba command)"
echo "==========================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to check if a file exists and contains specific content
check_file_content() {
    local file=$1
    local pattern=$2
    local description=$3
    
    if [ -f "$file" ]; then
        if grep -q "$pattern" "$file"; then
            echo -e "${GREEN}✅ $description${NC}"
            ((TESTS_PASSED++))
        else
            echo -e "${RED}❌ $description - Pattern not found${NC}"
            ((TESTS_FAILED++))
        fi
    else
        echo -e "${RED}❌ $description - File not found${NC}"
        ((TESTS_FAILED++))
    fi
}

echo ""
echo "🔧 Test 1: Checking SkyeBlockAdminCommand Implementation"
echo "--------------------------------------------------------"

ADMIN_COMMAND="src/main/java/skyeblock/nobleskye/dev/skyeblock/commands/SkyeBlockAdminCommand.java"

check_file_content "$ADMIN_COMMAND" "class SkyeBlockAdminCommand implements CommandExecutor" "SkyeBlockAdminCommand class exists"
check_file_content "$ADMIN_COMMAND" "skyeblock.admin" "Admin permission check"
check_file_content "$ADMIN_COMMAND" "handleAdminIslandCommand" "Admin island command handler"
check_file_content "$ADMIN_COMMAND" "handleAdminCreateCommand" "Admin create command handler"
check_file_content "$ADMIN_COMMAND" "handleAdminDeleteCommand" "Admin delete command handler"
check_file_content "$ADMIN_COMMAND" "handleAdminVisitCommand" "Admin visit command handler"

echo ""
echo "📜 Test 2: Checking Plugin Registration"
echo "---------------------------------------"

PLUGIN_FILE="src/main/java/skyeblock/nobleskye/dev/skyeblock/SkyeBlockPlugin.java"

check_file_content "$PLUGIN_FILE" "SkyeBlockAdminCommand" "SkyeBlockAdminCommand import/usage"
check_file_content "$PLUGIN_FILE" "new.*SkyeBlockAdminCommand" "SkyeBlockAdminCommand instantiation"
check_file_content "$PLUGIN_FILE" "getCommand.*sba" "SBA command registration"

echo ""
echo "📋 Test 3: Checking Plugin.yml Configuration"
echo "---------------------------------------------"

PLUGIN_YML="plugin.yml"

check_file_content "$PLUGIN_YML" "sba:" "SBA command definition"
check_file_content "$PLUGIN_YML" "SkyeBlock Admin command" "SBA command description"
check_file_content "$PLUGIN_YML" "skyeblock.admin.bypass" "Admin bypass permission"
check_file_content "$PLUGIN_YML" "skyeblockadmin" "SBA command alias"

echo ""
echo "🛡️ Test 4: Checking Admin Bypass in Visitor Protection"
echo "--------------------------------------------------------"

VISITOR_LISTENER="src/main/java/skyeblock/nobleskye/dev/skyeblock/listeners/VisitorProtectionListener.java"

check_file_content "$VISITOR_LISTENER" "skyeblock.admin.bypass" "Admin bypass permission in visitor protection"
check_file_content "$VISITOR_LISTENER" "hasPermission.*bypass" "Admin bypass permission check"
check_file_content "$VISITOR_LISTENER" "return false" "Admin bypass return statement"

echo ""
echo "🎯 Test 5: Checking Admin Command Subcommands"
echo "----------------------------------------------"

check_file_content "$ADMIN_COMMAND" "case.*island" "Island subcommand"
check_file_content "$ADMIN_COMMAND" "case.*create" "Create subcommand"
check_file_content "$ADMIN_COMMAND" "case.*delete" "Delete subcommand"
check_file_content "$ADMIN_COMMAND" "case.*visit" "Visit subcommand"
check_file_content "$ADMIN_COMMAND" "case.*bypass" "Bypass info subcommand"
check_file_content "$ADMIN_COMMAND" "case.*reload" "Reload subcommand"
check_file_content "$ADMIN_COMMAND" "case.*debug" "Debug subcommand"

echo ""
echo "📚 Test 6: Checking Tab Completion"
echo "----------------------------------"

check_file_content "$ADMIN_COMMAND" "onTabComplete" "Tab completion method"
check_file_content "$ADMIN_COMMAND" "Arrays.asList.*island.*create.*delete" "Tab completion subcommands"
check_file_content "$ADMIN_COMMAND" "creation.*deletion.*visitor" "Bypass tab completion"

echo ""
echo "💬 Test 7: Checking Help System"
echo "-------------------------------"

check_file_content "$ADMIN_COMMAND" "showAdminHelp" "Admin help method"
check_file_content "$ADMIN_COMMAND" "SkyeBlock Admin Commands" "Admin help title"
check_file_content "$ADMIN_COMMAND" "bypassing restrictions" "Admin help descriptions"

echo ""
echo "🔧 Test 8: Checking Compilation"
echo "-------------------------------"

echo "Attempting to compile the project..."
if mvn clean compile -q > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Project compiles successfully${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Project compilation failed${NC}"
    ((TESTS_FAILED++))
fi

echo ""
echo "🚀 Test 9: Checking JAR Build"
echo "-----------------------------"

echo "Attempting to build JAR..."
if mvn clean package -q > /dev/null 2>&1; then
    echo -e "${GREEN}✅ JAR builds successfully${NC}"
    ((TESTS_PASSED++))
    
    # Check if JAR file exists
    if ls target/skyeblock-*.jar > /dev/null 2>&1; then
        echo -e "${GREEN}✅ JAR file created${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ JAR file not found${NC}"
        ((TESTS_FAILED++))
    fi
else
    echo -e "${RED}❌ JAR build failed${NC}"
    ((TESTS_FAILED++))
fi

echo ""
echo "🎯 Test 10: Checking Admin Bypass Messages"
echo "-------------------------------------------"

check_file_content "$ADMIN_COMMAND" "admin privileges" "Admin privilege messages"
check_file_content "$ADMIN_COMMAND" "bypassing.*restrictions" "Bypass restriction messages"
check_file_content "$ADMIN_COMMAND" "bypassing.*cooldowns" "Bypass cooldown messages"

echo ""
echo "==============================================="
echo "🏁 Admin Bypass System Test Results"
echo "==============================================="

TOTAL_TESTS=$((TESTS_PASSED + TESTS_FAILED))

echo -e "Total Tests: ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"

if [ $TESTS_FAILED -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 ALL TESTS PASSED! Admin bypass system is fully implemented.${NC}"
    echo ""
    echo "✅ Admin System Features:"
    echo "   • /sba command with full admin privileges"
    echo "   • Bypasses island creation cooldowns and limits"
    echo "   • Bypasses island deletion cooldowns and location checks"
    echo "   • Bypasses all visitor restrictions"
    echo "   • Admin-only debug and reload commands"
    echo "   • Comprehensive help and bypass information"
    echo ""
    echo "✅ Commands Available:"
    echo "   • /sba island - Island management with admin privileges"
    echo "   • /sba create [type] - Create islands bypassing restrictions"
    echo "   • /sba delete [player] - Delete islands bypassing cooldowns"
    echo "   • /sba visit <player> - Visit with full admin access"
    echo "   • /sba bypass [type] - Show bypass information"
    echo "   • /sba reload [force] - Reload plugin configuration"
    echo "   • /sba debug - Show debug information"
    echo ""
    echo "✅ Required Permission: skyeblock.admin"
    echo "✅ Bypass Permission: skyeblock.admin.bypass"
    echo ""
    exit 0
else
    echo ""
    echo -e "${RED}❌ SOME TESTS FAILED! Please review the implementation.${NC}"
    echo ""
    exit 1
fi
