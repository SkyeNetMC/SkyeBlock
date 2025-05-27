#!/bin/bash

# SkyeBlock Build Script

echo "Building SkyeBlock plugin..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Clean and compile
mvn clean package

if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "Plugin JAR location: target/skyeblock-1.0.0.jar"
    echo ""
    echo "To install:"
    echo "1. Copy the JAR to your server's plugins/ folder"
    echo "2. Make sure you have WorldEdit and WorldGuard installed"
    echo "3. Create island schematics in plugins/SkyeBlock/schematics/"
    echo "4. Restart your server"
else
    echo "Build failed!"
    exit 1
fi
