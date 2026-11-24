#!/bin/sh
# Builds OS-Scheduler.jar and runs the tests (macOS/Linux).
set -e
rm -rf build
mkdir -p build/classes build/test
javac --release 8 -encoding UTF-8 -d build/classes src/cpu/*.java src/disk/*.java src/gui/*.java
javac --release 8 -encoding UTF-8 -cp build/classes -d build/test test/*.java
java -cp build/classes:build/test AlgorithmTests
printf 'Main-Class: gui.Main\n' > build/manifest.txt
jar cfm OS-Scheduler.jar build/manifest.txt -C build/classes .
echo "Built OS-Scheduler.jar"
