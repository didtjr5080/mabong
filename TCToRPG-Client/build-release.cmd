@echo off
setlocal

set ROOT=%~dp0
set DIST=%ROOT%dist

if not exist "%DIST%" mkdir "%DIST%"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$items = Get-ChildItem -LiteralPath '%ROOT%' -Force | Where-Object { $_.Name -ne 'dist' }; Compress-Archive -Path $items.FullName -DestinationPath '%DIST%\TCToRPG-Client.zip' -Force"
if errorlevel 1 exit /b 1

echo Release package created: %DIST%\TCToRPG-Client.zip
