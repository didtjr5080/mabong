$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$GradleVersion = "8.10.2"
$NeoForgeVersion = "21.1.231"

function Info($message) {
    Write-Host "[TCToRPG] $message"
}

function Ensure-Directory($path) {
    if (-not (Test-Path $path)) {
        New-Item -ItemType Directory -Path $path | Out-Null
    }
}

function Get-GradleCommand {
    $wrapper = Join-Path $Root "gradlew.bat"
    if (Test-Path $wrapper) {
        return $wrapper
    }

    $systemGradle = Get-Command gradle -ErrorAction SilentlyContinue
    if ($systemGradle) {
        return $systemGradle.Source
    }

    $bootstrap = Join-Path $Root ".gradle\bootstrap"
    $gradleHome = Join-Path $bootstrap "gradle-$GradleVersion"
    $gradleBat = Join-Path $gradleHome "bin\gradle.bat"
    Ensure-Directory $bootstrap

    if (-not (Test-Path $gradleBat)) {
        $zip = Join-Path $bootstrap "gradle-$GradleVersion-bin.zip"
        $url = "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip"
        Info "Downloading Gradle $GradleVersion..."
        Invoke-WebRequest -Uri $url -OutFile $zip
        Info "Extracting Gradle..."
        Expand-Archive -Path $zip -DestinationPath $bootstrap -Force
    }

    return $gradleBat
}

function Copy-ModJar($jar) {
    $serverMods = Join-Path $Root "server\mods"
    $clientMods = Join-Path $Root "TCToRPG-Client\mods"
    $clientGameMods = Join-Path $Root "TCToRPG-Client\minecraft\mods"
    Ensure-Directory $serverMods
    Ensure-Directory $clientMods
    Ensure-Directory $clientGameMods
    Copy-Item -LiteralPath $jar -Destination (Join-Path $serverMods "tctorpg.jar") -Force
    Copy-Item -LiteralPath $jar -Destination (Join-Path $clientMods "tctorpg.jar") -Force
    Copy-Item -LiteralPath $jar -Destination (Join-Path $clientGameMods "tctorpg.jar") -Force
}

function Sync-ResourcePack {
    $resourceRoot = Join-Path $Root "TCToRPG-Client\resourcepacks\TCToRPG-Resources"
    $assetsOut = Join-Path $resourceRoot "assets\tctorpg"
    $assetsIn = Join-Path $Root "src\main\resources\assets\tctorpg"
    Ensure-Directory $assetsOut
    Copy-Item -Path (Join-Path $assetsIn "*") -Destination $assetsOut -Recurse -Force

    $packMeta = Join-Path $resourceRoot "pack.mcmeta"
    @"
{
  "pack": {
    "pack_format": 34,
    "description": "TCToRPG resources"
  }
}
"@ | Set-Content -LiteralPath $packMeta -Encoding UTF8

    $zip = Join-Path $Root "TCToRPG-Client\resourcepacks\TCToRPG-Resources.zip"
    if (Test-Path $zip) {
        Remove-Item -LiteralPath $zip -Force
    }
    Compress-Archive -Path (Join-Path $resourceRoot "*") -DestinationPath $zip -Force

    $gameResourcepacks = Join-Path $Root "TCToRPG-Client\minecraft\resourcepacks"
    Ensure-Directory $gameResourcepacks
    Copy-Item -LiteralPath $zip -Destination (Join-Path $gameResourcepacks "TCToRPG-Resources.zip") -Force

    $options = Join-Path $Root "TCToRPG-Client\minecraft\options.txt"
    $resourceLine = 'resourcePacks:["vanilla","file/TCToRPG-Resources.zip"]'
    if (Test-Path $options) {
        $lines = Get-Content -LiteralPath $options
        $updated = $false
        $lines = $lines | ForEach-Object {
            if ($_ -like "resourcePacks:*") {
                $updated = $true
                $resourceLine
            } else {
                $_
            }
        }
        if (-not $updated) {
            $lines += $resourceLine
        }
        Set-Content -LiteralPath $options -Value $lines -Encoding UTF8
    } else {
        Set-Content -LiteralPath $options -Value $resourceLine -Encoding UTF8
    }
}

function Sync-ServerRuntimeData {
    $serverData = Join-Path $Root "server\data\tctorpg\rpg"
    $sourceData = Join-Path $Root "src\main\resources\data\tctorpg\rpg"
    if (Test-Path $serverData) {
        Remove-Item -LiteralPath $serverData -Recurse -Force
    }
    Ensure-Directory $serverData
    Copy-Item -Path (Join-Path $sourceData "*") -Destination $serverData -Recurse -Force

    $serverContentPacks = Join-Path $Root "server\content_packs"
    $sourceContentPacks = Join-Path $Root "content_packs"
    if (Test-Path $serverContentPacks) {
        Remove-Item -LiteralPath $serverContentPacks -Recurse -Force
    }
    Ensure-Directory $serverContentPacks
    Copy-Item -Path (Join-Path $sourceContentPacks "*") -Destination $serverContentPacks -Recurse -Force

    $serverOperators = Join-Path $Root "server\config\tctorpg\operators.json"
    $sourceOperators = Join-Path $Root "config\tctorpg\operators.json"
    Ensure-Directory (Split-Path -Parent $serverOperators)
    Copy-Item -LiteralPath $sourceOperators -Destination $serverOperators -Force
}

function Ensure-NeoForgeInstaller {
    $installer = Join-Path $Root "server\neoforge-installer.jar"
    if (Test-Path $installer) {
        Remove-Item -LiteralPath $installer -Force
    }

    $url = "https://maven.neoforged.net/releases/net/neoforged/neoforge/$NeoForgeVersion/neoforge-$NeoForgeVersion-installer.jar"
    Info "Downloading NeoForge installer $NeoForgeVersion..."
    Invoke-WebRequest -Uri $url -OutFile $installer
}

function Package-Directory($source, $destination, $excludeDist) {
    if (Test-Path $destination) {
        Remove-Item -LiteralPath $destination -Force
    }

    $items = Get-ChildItem -LiteralPath $source -Force | Where-Object {
        -not ($excludeDist -and $_.Name -eq "dist")
    }
    Compress-Archive -Path $items.FullName -DestinationPath $destination -Force
}

function Build-ClientLauncher {
    $script = Join-Path $Root "build-client-exe.ps1"
    Info "Building client launcher exe..."
    & powershell -NoProfile -ExecutionPolicy Bypass -File $script
    if ($LASTEXITCODE -ne 0) {
        throw "Client launcher build failed."
    }
}

Info "Preparing runnable client and server packages..."

$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java) {
    throw "Java 21 is required, but java was not found in PATH."
}

$gradle = Get-GradleCommand
Info "Building mod jar..."
Push-Location $Root
try {
    & $gradle clean build --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed."
    }
}
finally {
    Pop-Location
}

$jar = Get-ChildItem -LiteralPath (Join-Path $Root "build\libs") -Filter "*.jar" |
    Where-Object { $_.Name -notmatch "(sources|javadoc|dev|plain)" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $jar) {
    throw "Built mod jar was not found under build/libs."
}

Info "Copying mod jar to server and client..."
Copy-ModJar $jar.FullName

Info "Syncing resource pack..."
Sync-ResourcePack

Info "Syncing server runtime data..."
Sync-ServerRuntimeData

Info "Preparing server installer..."
Ensure-NeoForgeInstaller

Build-ClientLauncher

$dist = Join-Path $Root "dist"
Ensure-Directory $dist

Info "Packaging client zip..."
Package-Directory (Join-Path $Root "TCToRPG-Client") (Join-Path $dist "TCToRPG-Client.zip") $true

Info "Packaging server zip..."
Package-Directory (Join-Path $Root "server") (Join-Path $dist "TCToRPG-Server.zip") $false

Info "Done."
Info "Client package: $dist\TCToRPG-Client.zip"
Info "Server package: $dist\TCToRPG-Server.zip"
