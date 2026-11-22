$ErrorActionPreference = "Stop"
Set-Location "c:\Users\kshet\OneDrive\Desktop\calculator"

$JPACKAGE = "C:\Program Files\Java\jdk-27\bin\jpackage.exe"
$MVN      = "C:\Tools\maven\apache-maven-3.10.1\bin\mvn.cmd"
$WIX3     = "C:\Program Files (x86)\WiX Toolset v3.14\bin"
$BASE     = "c:\Users\kshet\OneDrive\Desktop\calculator"

# Put WiX 3 at the FRONT of PATH so jpackage finds candle/light from v3, not v4
$env:PATH = "$WIX3;" + $env:PATH

# --- 1. Build fat JAR ---
Write-Host "[1/3] Building JAR..." -ForegroundColor Cyan
& $MVN package -DskipTests -q
if ($LASTEXITCODE -ne 0) { throw "Maven failed" }

# --- 2. Stage input dir ---
$inputDir   = Join-Path $BASE "target\jpackage-input"
$runtimeDir = Join-Path $BASE "target\runtime-image"
$destDir    = Join-Path $BASE "target\installer"

foreach ($d in @($inputDir, $destDir)) {
    if (Test-Path $d) { Remove-Item -Recurse -Force $d }
    New-Item -ItemType Directory -Force -Path $d | Out-Null
}

$fatJar = (Get-ChildItem (Join-Path $BASE "target\*-fat.jar"))[0].FullName
Copy-Item $fatJar (Join-Path $inputDir "app.jar")

$m2fx = "$env:USERPROFILE\.m2\repository\org\openjfx"
foreach ($jar in (Get-ChildItem "$m2fx\javafx-*\21.0.2\javafx-*-21.0.2-win.jar")) {
    Copy-Item $jar.FullName (Join-Path $inputDir $jar.Name)
}

$fxJarList = (Get-ChildItem "$inputDir\javafx-*-win.jar" | Select-Object -ExpandProperty FullName) -join ";"

Write-Host "WiX candle.exe: $(& candle.exe -? 2>&1 | Select-Object -First 1)"

# --- 3. jpackage -> .exe installer ---
Write-Host "[3/3] Building .exe installer..." -ForegroundColor Cyan

& $JPACKAGE `
    "--type"          "exe" `
    "--name"          "AnimationExposureCalculator" `
    "--app-version"   "1.0.0" `
    "--description"   "Exposure calculator for 2D animators" `
    "--vendor"        "Animation Tools" `
    "--input"         $inputDir `
    "--main-jar"      "app.jar" `
    "--main-class"    "com.animationcalc.App" `
    "--runtime-image" $runtimeDir `
    "--dest"          $destDir `
    "--win-dir-chooser" `
    "--win-menu" `
    "--win-shortcut" `
    "--java-options"  ("-p `"" + $fxJarList + "`"") `
    "--java-options"  "--add-modules=javafx.controls" `
    "--java-options"  "--add-opens=javafx.base/com.sun.javafx.event=ALL-UNNAMED"

if ($LASTEXITCODE -ne 0) { throw "jpackage failed" }

$exe = Get-ChildItem (Join-Path $destDir "*.exe") | Select-Object -First 1
if (-not $exe) { throw "No .exe found in $destDir" }

$sizeMB = [math]::Round($exe.Length / 1MB, 1)
Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host " DONE: $($exe.Name)  ($sizeMB MB)"          -ForegroundColor Green
Write-Host " Path: $($exe.FullName)"                     -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
