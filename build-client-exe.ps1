$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Source = Join-Path $Root "launcher\TCToRPGLauncher.cs"
$Out = Join-Path $Root "TCToRPG-Client\TCToRPG-Client.exe"
$Csc = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"

if (-not (Test-Path $Csc)) {
    $Csc = Join-Path $env:WINDIR "Microsoft.NET\Framework\v4.0.30319\csc.exe"
}
if (-not (Test-Path $Csc)) {
    throw "C# compiler was not found."
}

& $Csc /nologo /target:winexe /platform:anycpu /optimize+ /out:$Out /reference:System.dll /reference:System.Core.dll /reference:System.Windows.Forms.dll $Source
if ($LASTEXITCODE -ne 0) {
    throw "Client launcher compile failed."
}

Write-Host "Created $Out"
