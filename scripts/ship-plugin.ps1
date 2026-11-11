# Build the Shell Reborn plugin jar and copy it to ~/.runelite/sideloaded-plugins.
# Sideloaded jars only load when RuneLite runs in developer mode (e.g. via
# C:\Users\Wesman\repos\bank-layouts-editor\runelite-bridge\run.ps1). This script never launches the client.
$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot

& (Join-Path $repo 'gradlew.bat') -p $repo --no-daemon -q test jar
if ($LASTEXITCODE -ne 0) { Write-Error 'Plugin build or tests failed'; exit 1 }

$jar = Get-ChildItem (Join-Path $repo 'build\libs\shellreborn-*.jar') | Sort-Object LastWriteTime | Select-Object -Last 1
$dest = Join-Path $env:USERPROFILE '.runelite\sideloaded-plugins'

# A running client keeps the classes it loaded; the new jar takes effect on the next start.
$running = Get-Process -ErrorAction SilentlyContinue | Where-Object { $_.ProcessName -match '^(RuneLite|java|javaw)$' -and $_.MainWindowTitle -match 'RuneLite' }
if ($running) {
    Write-Host "RuneLite is running (pid $($running.Id -join ', ')): restart it to load the new jar." -ForegroundColor Yellow
}
New-Item -ItemType Directory -Force $dest | Out-Null
$old = (Get-ChildItem (Join-Path $dest 'shellreborn-*.jar') -ErrorAction SilentlyContinue | Select-Object -First 1).Name
if (-not $old) { $old = 'none' }
try {
    # older versions of this plugin only; other sideloaded jars are left alone
    Get-ChildItem (Join-Path $dest 'shellreborn-*.jar') -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -ne $jar.Name } | Remove-Item -Force -ErrorAction Stop
    Copy-Item $jar.FullName $dest -Force
    if ((Get-FileHash $jar.FullName).Hash -ne (Get-FileHash (Join-Path $dest $jar.Name)).Hash) {
        Write-Error "The copy in $dest does not match the build"; exit 1
    }
    Write-Host "Deployed $($jar.Name) to $dest (start RuneLite in developer mode to load it)"
    Write-Host "==> Done: $old -> $($jar.Name)"
} catch [System.IO.IOException] {
    Write-Host 'Jar is locked: RuneLite is running. Close it and re-run (a restart is needed to load the new jar anyway).'
    exit 2
}
