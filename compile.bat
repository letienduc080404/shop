@echo off
TITLE Aura Shop - Compiler (Portable)
COLOR 0B
echo ==========================================
echo       AURA SHOP - PROJECT COMPILER
echo ==========================================
echo.
echo [1/2] Dang lam sach va bien dich du an...
echo.

java -Dmaven.multiModuleProjectDirectory=%CD% -cp .mvn\wrapper\maven-wrapper.jar org.apache.maven.wrapper.MavenWrapperMain clean install -DskipTests

echo.
echo ==========================================
echo BIEN DICH HOAN TAT!
echo ==========================================
pause
