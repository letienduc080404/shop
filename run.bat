@echo off
TITLE Aura Shop - System Runner (Portable)
COLOR 0A
echo ==========================================
echo        AURA SHOP - PROJECT RUNNER
echo ==========================================
echo.
echo [1/3] Da tim thay Maven Wrapper.
echo [2/3] Dang kiem tra moi truong Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java khong duoc tim thay. Vui long cai dat JDK.
    pause
    exit /b
)

echo [3/3] Dang khoi dong ung dung bang Maven Wrapper...
echo.

java -Dmaven.multiModuleProjectDirectory=%CD% -cp .mvn\wrapper\maven-wrapper.jar org.apache.maven.wrapper.MavenWrapperMain spring-boot:run

echo.
echo ==========================================
echo UNG DUNG DA DUNG.
echo ==========================================
pause
