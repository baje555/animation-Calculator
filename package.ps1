# ============================================================
#  package.ps1  –  Build a self-contained installer for
#                  Animation Exposure Calculator (Windows)
#
#  No admin rights required. No WiX required.
#
#  What this produces:
#    target\installer\AnimationExposureCalculator-Setup.exe
#
#  When the user runs that .exe it:
#    - Extracts the app to %LOCALAPPDATA%\AnimationExposureCalculator
#    - Creates a Desktop shortcut
#    - Creates a Start Menu shortcut
#
#  Run from the project root:
#    .\package.ps1
# ============================================================

$ErrorActionPreference = "Stop"

$MVN      = "C:\Tools\maven\apache-maven-3.10.1\bin\mvn.cmd"
$JPACKAGE = "C:\Program Files\Java\jdk-27\bin\jpackage.exe"
$JLINK    = "C:\Program Files\Java\jdk-27\bin\jlink.exe"

$PROJECT   = $PSScriptRoot
$TARGET    = "$PROJECT\target"
$RUNTIME   = "$TARGET\runtime-image"
$INPUT_DIR = "$TARGET\jpackage-input"
$APP_IMAGE = "$TARGET\app-image"
$INSTALLER = "$TARGET\installer"

# ── 1. Maven compile + fat JAR ───────────────────────────────────────────────
Write-Host ""
Write-Host "[1/4] Compiling with Maven..." -ForegroundColor Cyan
& $MVN -f "$PROJECT\pom.xml" package "-DskipTests"
if ($LASTEXITCODE -ne 0) { throw "Maven build failed." }

$FAT_JAR = Get-ChildItem "$TARGET\*-fat.jar" | Select-Object -First 1
if (-not $FAT_JAR) { throw "Fat JAR not found under $TARGET" }
Write-Host "   JAR: $($FAT_JAR.Name)"

# ── 2. Build a minimal JRE with jlink (JDK modules only) ─────────────────────
Write-Host ""
Write-Host "[2/4] Building minimal JRE with jlink..." -ForegroundColor Cyan

if (Test-Path $RUNTIME) { Remove-Item -Recurse -Force $RUNTIME }

$JDK_JMODS = "C:\Program Files\Java\jdk-27\jmods"
if (-not (Test-Path $JDK_JMODS)) { throw "JDK jmods not found at $JDK_JMODS" }

& $JLINK `
    "--module-path" $JDK_JMODS `
    "--add-modules" "java.base,java.desktop,java.logging,java.xml,java.prefs,java.naming,jdk.unsupported,jdk.unsupported.desktop" `
    "--output"      $RUNTIME `
    "--strip-debug" `
    "--no-header-files" `
    "--no-man-pages" `
    "--compress"    "zip-6"

if ($LASTEXITCODE -ne 0) { throw "jlink failed." }
$runtimeMB = [math]::Round((Get-ChildItem $RUNTIME -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB, 0)
Write-Host "   Runtime: $runtimeMB MB"

# ── 3. jpackage → app-image (folder, no WiX needed) ──────────────────────────
Write-Host ""
Write-Host "[3/4] Building app image with jpackage..." -ForegroundColor Cyan

if (Test-Path $INPUT_DIR) { Remove-Item -Recurse -Force $INPUT_DIR }
New-Item -ItemType Directory -Force -Path $INPUT_DIR | Out-Null
Copy-Item $FAT_JAR.FullName "$INPUT_DIR\app.jar"

if (Test-Path $APP_IMAGE) { Remove-Item -Recurse -Force $APP_IMAGE }
New-Item -ItemType Directory -Force -Path $APP_IMAGE | Out-Null

# Collect JavaFX win-classifier jars for the module path
$FX_VER  = "21.0.2"
$M2_FX   = "$env:USERPROFILE\.m2\repository\org\openjfx"
$FX_JARS = (Get-ChildItem "$M2_FX\javafx-*\$FX_VER\javafx-*-$FX_VER-win.jar" -ErrorAction SilentlyContinue).FullName -join [IO.Path]::PathSeparator

if (-not $FX_JARS) { throw "JavaFX win JARs not found in Maven cache. Run 'mvn javafx:run' once first." }

& $JPACKAGE `
    "--type"          "app-image" `
    "--name"          "AnimationExposureCalculator" `
    "--input"         $INPUT_DIR `
    "--main-jar"      "app.jar" `
    "--main-class"    "com.animationcalc.App" `
    "--runtime-image" $RUNTIME `
    "--dest"          $APP_IMAGE `
    "--java-options"  "-p `"$FX_JARS`"" `
    "--java-options"  "--add-modules=javafx.controls,javafx.fxml" `
    "--java-options"  "--add-opens=javafx.base/com.sun.javafx.event=ALL-UNNAMED"

if ($LASTEXITCODE -ne 0) { throw "jpackage app-image failed." }

$APP_DIR = "$APP_IMAGE\AnimationExposureCalculator"
if (-not (Test-Path $APP_DIR)) { throw "Expected app image at $APP_DIR but it was not created." }
Write-Host "   App image: $APP_DIR"

# ── 4. Wrap in a self-installing .exe using a PowerShell stub ─────────────────
Write-Host ""
Write-Host "[4/4] Building self-extracting installer .exe..." -ForegroundColor Cyan

if (Test-Path $INSTALLER) { Remove-Item -Recurse -Force $INSTALLER }
New-Item -ItemType Directory -Force -Path $INSTALLER | Out-Null

# Zip the app image
$ZIP_PATH = "$TARGET\app-image.zip"
if (Test-Path $ZIP_PATH) { Remove-Item $ZIP_PATH }
Write-Host "   Compressing app image..."
Compress-Archive -Path "$APP_DIR\*" -DestinationPath $ZIP_PATH -CompressionLevel Optimal
$zipMB = [math]::Round((Get-Item $ZIP_PATH).Length / 1MB, 0)
Write-Host "   Zip: $zipMB MB"

# Read zip as Base64
Write-Host "   Encoding..."
$zipBytes  = [System.IO.File]::ReadAllBytes($ZIP_PATH)
$zipBase64 = [System.Convert]::ToBase64String($zipBytes)

# Write the installer PowerShell script
$PS_INSTALLER = "$TARGET\installer-script.ps1"
@"
# Self-extracting installer for Animation Exposure Calculator
`$ErrorActionPreference = "Stop"
`$APP_NAME   = "AnimationExposureCalculator"
`$INSTALL_TO = "`$env:LOCALAPPDATA\`$APP_NAME"

Write-Host "Installing Animation Exposure Calculator..."
Write-Host "Destination: `$INSTALL_TO"

# Extract
if (Test-Path `$INSTALL_TO) { Remove-Item -Recurse -Force `$INSTALL_TO }
New-Item -ItemType Directory -Force -Path `$INSTALL_TO | Out-Null

`$zipB64 = "$zipBase64"
`$bytes  = [System.Convert]::FromBase64String(`$zipB64)
`$tmp    = [System.IO.Path]::GetTempFileName() + ".zip"
[System.IO.File]::WriteAllBytes(`$tmp, `$bytes)
Expand-Archive -Path `$tmp -DestinationPath `$INSTALL_TO -Force
Remove-Item `$tmp

`$EXE = "`$INSTALL_TO\`$APP_NAME.exe"
if (-not (Test-Path `$EXE)) {
    # Try one level deeper (some jpackage versions add a subfolder)
    `$EXE = (Get-ChildItem "`$INSTALL_TO\*.exe" -Recurse | Select-Object -First 1).FullName
}
if (-not `$EXE) { throw "Could not find application executable after extraction." }

# Desktop shortcut
`$WS = New-Object -ComObject WScript.Shell
`$SC = `$WS.CreateShortcut("`$env:USERPROFILE\Desktop\Animation Exposure Calculator.lnk")
`$SC.TargetPath       = `$EXE
`$SC.WorkingDirectory = `$INSTALL_TO
`$SC.Description      = "Animation Exposure Calculator"
`$SC.Save()

# Start Menu shortcut
`$SM = "`$env:APPDATA\Microsoft\Windows\Start Menu\Programs"
`$SC2 = `$WS.CreateShortcut("`$SM\Animation Exposure Calculator.lnk")
`$SC2.TargetPath       = `$EXE
`$SC2.WorkingDirectory = `$INSTALL_TO
`$SC2.Description      = "Animation Exposure Calculator"
`$SC2.Save()

Write-Host ""
Write-Host "Done! Installed to: `$INSTALL_TO"
Write-Host "Shortcuts added to Desktop and Start Menu."
Write-Host ""
Write-Host "Press Enter to close..."
Read-Host
"@ | Set-Content -Path $PS_INSTALLER -Encoding UTF8

# Compile the PowerShell installer into an .exe using ps2exe
# First check if ps2exe is available
$ps2exe = Get-Command "ps2exe" -ErrorAction SilentlyContinue
if (-not $ps2exe) {
    Write-Host "   Installing ps2exe (PowerShell → EXE compiler)..."
    Install-Module -Name ps2exe -Scope CurrentUser -Force -AllowClobber
}

$EXE_OUT = "$INSTALLER\AnimationExposureCalculator-Setup.exe"
Invoke-PS2EXE `
    -InputFile  $PS_INSTALLER `
    -OutputFile $EXE_OUT `
    -NoConsole:$false `
    -Title      "Animation Exposure Calculator Setup" `
    -Description "Installs Animation Exposure Calculator" `
    -Version    "1.0.0"

if (-not (Test-Path $EXE_OUT)) { throw "ps2exe did not produce an .exe" }

$exeMB = [math]::Round((Get-Item $EXE_OUT).Length / 1MB, 1)
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host " SUCCESS" -ForegroundColor Green
Write-Host " Installer: $EXE_OUT" -ForegroundColor Green
Write-Host " Size:      $exeMB MB" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
Write-Host " Give that .exe to anyone running Windows."
Write-Host " Double-clicking it installs the app to %LOCALAPPDATA%"
Write-Host " and creates Desktop + Start Menu shortcuts."
