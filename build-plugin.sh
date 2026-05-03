#!/bin/bash
# Exit on error
set -e

# --- Configuration ---
PLUGIN_NAME="NoZombieKnockback"
SOURCE_DIR="src/main/java"
RESOURCES_DIR="src/main/resources"
BUILD_DIR="build"
OUTPUT_DIR="dist"

# Check for required commands
if ! command -v javac &> /dev/null; then
    echo "Error: javac is not installed or not in PATH."
    exit 1
fi

if ! command -v jar &> /dev/null; then
    echo "Error: jar is not installed or not in PATH."
    exit 1
fi

# --- Dependencies ---
# Ensure lib directory exists
mkdir -p lib

# Try to find a Spigot jar in the 'lib' folder
SPIGOT_JAR=$(find lib -name "spigot-*.jar" 2>/dev/null | head -n 1)

if [ -z "$SPIGOT_JAR" ]; then
    echo "----------------------------------------------------------"
    echo "Error: Spigot API jar not found in lib/ folder!"
    echo "To build this plugin, you need a Spigot or Bukkit API jar."
    echo ""
    echo "1. Download the Spigot API jar (version 1.21.11-R0.2-SNAPSHOT)."
    echo "   URL: https://hub.spigotmc.org/nexus/service/rest/repository/browse/snapshots/org/spigotmc/spigot-api/1.21.11-R0.2-SNAPSHOT"
    echo ""
    echo "   IMPORTANT: Download the file ending in '-shaded.jar'."
    echo "   The shaded jar includes necessary dependencies like Gson and Netty."
    echo ""
    echo "2. Place the downloaded jar in the 'lib/' folder."
    echo "3. Run this script again."
    echo "----------------------------------------------------------"
    exit 1
fi

echo "Starting build process for ${PLUGIN_NAME}..."
echo "Using dependency: ${SPIGOT_JAR}"

# Clean and create build directory
echo "Cleaning up previous builds..."
rm -rf "${BUILD_DIR}"
rm -rf "${OUTPUT_DIR}"
mkdir -p "${BUILD_DIR}"
mkdir -p "${OUTPUT_DIR}"

# Compile Java source files
echo "Compiling Java sources..."
find "${SOURCE_DIR}" -name "*.java" > sources.txt

# Use lib/* to include all jars in the lib folder (matching the original script's multi-jar approach)
javac -d "${BUILD_DIR}" -cp "lib/*" @sources.txt

# Copy resources (plugin.yml, config.yml, etc.)
if [ -d "${RESOURCES_DIR}" ]; then
    echo "Copying resources..."
    cp -r "${RESOURCES_DIR}/." "${BUILD_DIR}"
fi

# Create the JAR file
echo "Creating JAR file..."
JAR_FILE="${OUTPUT_DIR}/${PLUGIN_NAME}.jar"
jar -cvf "${JAR_FILE}" -C "${BUILD_DIR}" .

echo "----------------------------------------------------------"
echo "Build successful! JAR created at: ${JAR_FILE}"
echo "----------------------------------------------------------"

# Cleanup
rm sources.txt

exit 0
