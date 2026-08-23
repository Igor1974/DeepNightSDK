#!/bin/bash

# DEEP NIGHT SDK Release Script
# Builds all modules and collects .aar files into 'release' folder.

set -e

echo "🚀 Starting DeepNight SDK Release Build..."

# 1. Clean and Build
./gradlew clean
./gradlew :tv-input:assembleRelease
./gradlew :dap-core:assembleRelease
./gradlew :ai-commands:assembleRelease
./gradlew :text-tools:assembleRelease
./gradlew :tv-ui-kit:assembleRelease
./gradlew :sample-app:assembleDebug

# 2. Create Release Folder
mkdir -p release/aar
mkdir -p release/unity
mkdir -p release/demo

# 3. Copy AARs
cp tv-input/build/outputs/aar/tv-input-release.aar release/aar/
cp dap-core/build/outputs/aar/dap-core-release.aar release/aar/
cp ai-commands/build/outputs/aar/ai-commands-release.aar release/aar/
cp text-tools/build/outputs/aar/text-tools-release.aar release/aar/
cp tv-ui-kit/build/outputs/aar/tv-ui-kit-release.aar release/aar/

# 4. Copy Demo APK
cp sample-app/build/outputs/apk/debug/sample-app-debug.apk release/demo/DeepNight_SDK_Demo.apk

# 4. Copy Unity Bridge
cp unity-bridge/DeepNightDapBridge.cs release/unity/

echo "✅ SDK Release build complete!"
echo "📦 Files are ready in 'release/' folder."
