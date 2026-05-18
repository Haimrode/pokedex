@echo off
setlocal

cd /d "%~dp0"
call gradlew.bat :app:assembleDebug

if %errorlevel% neq 0 (
    echo.
    echo Build failed.
    exit /b %errorlevel%
)

echo.
echo APK generated in app\build\outputs\apk\debug\app-debug.apk
endlocal

