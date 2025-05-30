#!/bin/bash

echo "🔍 SkyeBlock Plugin Version Verification"
echo "========================================"

# Check plugin.yml version
echo "📄 plugin.yml version:"
grep "version:" /mnt/sda4/SkyeNetwork/SkyeBlock/plugin.yml

# Check pom.xml version  
echo ""
echo "📦 pom.xml version:"
grep "<version>" /mnt/sda4/SkyeNetwork/SkyeBlock/pom.xml | head -1

# Check built JAR files
echo ""
echo "🏗️ Built JAR files:"
ls -la /mnt/sda4/SkyeNetwork/SkyeBlock/target/*.jar

# Check README version reference
echo ""
echo "📚 README version reference:"
grep "Version 1.1.0" /mnt/sda4/SkyeNetwork/SkyeBlock/README.md

echo ""
echo "✅ Version verification complete!"
echo "🚀 Plugin updated to version 1.1.0 with directory structure improvements"
