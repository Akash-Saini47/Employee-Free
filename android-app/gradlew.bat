@echo off
setlocal
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
set "VERSION=8.9"
set "CACHE=%USERPROFILE%\.gradle\salarywise-wrapper\gradle-%VERSION%"
if exist "%CACHE%\bin\gradle.bat" (
  call "%CACHE%\bin\gradle.bat" %*
  exit /b %ERRORLEVEL%
)
echo Gradle %VERSION% is required. Please install Gradle or use Android Studio to sync the project.
exit /b 1
