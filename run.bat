@echo off
setlocal
echo ==============================================================
echo KHOI DONG HE THONG BAN QUAN AO DOMINO SHOP
echo ==============================================================
echo He thong dang kiem tra moi truong va tai cac thanh phan can thiet...
echo Qua trinh nay co the mat vai phut trong lan dau tien...
echo.

IF NOT EXIST ".mvn\wrapper\maven-wrapper.jar" (
    echo [1/3] Dang tai Maven Wrapper de chay doc lap khong can cai Maven...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar' -OutFile '.mvn\wrapper\maven-wrapper.jar'"
) ELSE (
    echo [1/3] Da tim thay Maven Wrapper.
)

echo [2/3] Dang khoi dong may chu va co so du lieu ngam...
echo [3/3] Ban mang trinh duyet va truy cap http://localhost:8080 sau khi he thong bao STARTED.
echo.
echo ==============================================================
echo Vui long giu nguyen cua so nay. 
echo ==============================================================
echo.
java -Dmaven.multiModuleProjectDirectory="%CD%" -cp ".mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain spring-boot:run
pause
