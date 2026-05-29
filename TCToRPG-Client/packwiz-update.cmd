@echo off
setlocal

where packwiz >nul 2>nul
if errorlevel 1 (
  echo packwiz not found in PATH.
  echo Install packwiz and try again.
  exit /b 1
)

pushd "%~dp0"

if not exist index.toml (
  echo index.toml not found.
  exit /b 1
)

packwiz refresh
if errorlevel 1 exit /b 1

packwiz validate
if errorlevel 1 exit /b 1

popd

echo packwiz update complete.
