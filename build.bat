@echo off
REM Builds OS-Scheduler.jar and runs the tests (Windows).
if exist build rmdir /s /q build
mkdir build\classes
mkdir build\test
javac --release 8 -encoding UTF-8 -d build\classes src\cpu\*.java src\disk\*.java src\gui\*.java || goto :error
javac --release 8 -encoding UTF-8 -cp build\classes -d build\test test\*.java || goto :error
java -cp build\classes;build\test AlgorithmTests || goto :error
echo Main-Class: gui.Main> build\manifest.txt
jar cfm OS-Scheduler.jar build\manifest.txt -C build\classes . || goto :error
echo Built OS-Scheduler.jar
goto :eof
:error
echo Build failed.
exit /b 1
