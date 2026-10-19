@echo off
setlocal

set "MAVEN_VERSION=3.9.6"
set "MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%"
set "MVN_CMD=%MAVEN_DIR%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd"

if not exist "%MVN_CMD%" (
    echo Downloading Maven %MAVEN_VERSION% directly...
    mkdir "%MAVEN_DIR%" >nul 2>&1
    powershell -Command "$ProgressPreference = 'SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip', '%MAVEN_DIR%\maven.zip'); Expand-Archive -Path '%MAVEN_DIR%\maven.zip' -DestinationPath '%MAVEN_DIR%' -Force; Remove-Item '%MAVEN_DIR%\maven.zip' -Force"
)

if not exist "%MVN_CMD%" (
    echo Error: Failed to download Maven.
    exit /b 1
)

call "%MVN_CMD%" %*
