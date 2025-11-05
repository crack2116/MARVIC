@echo off
REM ========================================
REM  MARVIC - Build Script
REM ========================================

echo.
echo ========================================
echo  MARVIC Inventory - Compilacion
echo ========================================
echo.

REM Configurar Java Home
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"

REM Limpiar build anterior
echo [1/3] Limpiando build anterior...
call gradlew.bat clean >nul 2>&1

REM Compilar Kotlin
echo [2/3] Compilando codigo Kotlin...
call gradlew.bat app:compileDebugKotlin

REM Generar APK
echo [3/3] Generando APK...
call gradlew.bat assembleDebug

echo.
echo ========================================
echo  Build completado!
echo  APK: app\build\outputs\apk\debug\
echo ========================================
echo.

pause
