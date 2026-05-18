@echo off
setlocal

cd /d "%~dp0"
call gradlew.bat :app:installDebug

if %errorlevel% neq 0 (
    echo.
    echo Install failed. Make sure an emulator or device is connected and unlocked.
    exit /b %errorlevel%
)

echo.
echo App installed successfully on the connected device/emulator.
endlocal

