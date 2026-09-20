$ErrorActionPreference = "Stop"

$ProjectDir = $PSScriptRoot
$RunDir = Join-Path $ProjectDir "run"
$PluginsDir = Join-Path $RunDir "plugins"
$TargetDir = Join-Path $ProjectDir "target"

Write-Host ""
Write-Host "=== Building AFKArea ==="

Set-Location $ProjectDir

mvn clean package

if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven build failed."
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "=== Finding plugin JAR ==="

$PluginJar = Get-ChildItem $TargetDir -Filter "afkarea-*.jar" |
    Where-Object {
        $_.Name -notlike "original-*"
    } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $PluginJar) {
    Write-Error "Could not find AFKArea JAR in target/."
    exit 1
}

New-Item -ItemType Directory -Force -Path $PluginsDir | Out-Null

$Destination = Join-Path $PluginsDir "AFKArea.jar"

Write-Host "Copying:"
Write-Host "  $($PluginJar.FullName)"
Write-Host "to:"
Write-Host "  $Destination"

Copy-Item $PluginJar.FullName $Destination -Force

$PaperJar = Join-Path $RunDir "paper-26.2-121.jar"

if (-not (Test-Path $PaperJar)) {
    Write-Error "run/paper.jar does not exist."
    exit 1
}

Write-Host ""
Write-Host "=== Starting Paper ==="
Write-Host ""

Set-Location $RunDir

java -Xms1G -Xmx2G -jar paper-26.2-121.jar --nogui