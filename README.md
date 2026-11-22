# Animation Exposure Calculator

A desktop tool for 2D animators to work out which frame each drawing goes on
and to convert freely between frames, timecodes, seconds, and milliseconds.

---

## What it does

**Tab 1 – Exposure Calculator**
- Enter a total length (as frames, or as seconds + frames).
- Build a timing pattern from one or more segments (e.g. "12 frames on 2s, then
  12 frames on 3s").
- See a results table (Drawing #, Frame, Held For, Segment, Timecode), a
  summary (total frames, duration, drawing count, average drawings/second),
  and a colour-coded exposure-strip visualisation.
- Copy the frame list to the clipboard or export to CSV.

**Tab 2 – Time ↔ Frame Converter**
- Convert in every direction, live as you type:
  - Seconds + frames ↔ total frames
  - Total frames ↔ HH:MM:SS:FF timecode
  - Total frames ↔ decimal seconds and milliseconds
  - Minutes + seconds + frames (for longer shots)
  - Same duration at 12, 24, 25, and 30 fps side by side
- "Send to calculator" pushes the result straight into Tab 1.

Both tabs share the same FPS setting and start-frame option (frame 0 or 1),
shown in the bar at the top of the window.

---

## Prerequisites

You need two things installed before you can build or run the project.

### 1 – Java Development Kit (JDK) 17 or later

1. Go to <https://adoptium.net> and download **Temurin 21** (or any JDK 17+).
2. Run the installer and accept the defaults.
3. Open a new PowerShell window and confirm it worked:
   ```
   java -version
   ```
   You should see something like `openjdk version "21.0.x"`.

### 2 – Apache Maven 3.9+

1. Go to <https://maven.apache.org/download.cgi> and download the
   **Binary zip archive** (e.g. `apache-maven-3.9.x-bin.zip`).
2. Unzip it somewhere permanent, e.g. `C:\Tools\maven`.
3. Add `C:\Tools\maven\bin` to your `PATH`:
   - Search Windows for **"Edit the system environment variables"**.
   - Click **Environment Variables → Path → Edit → New** and paste the path.
4. Open a **new** PowerShell window and confirm:
   ```
   mvn -version
   ```
   You should see `Apache Maven 3.9.x`.

---

## Project structure

```
calculator/
├── pom.xml                                      ← Maven build file
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── module-info.java                 ← JavaFX module descriptor
    │   │   └── com/animationcalc/
    │   │       ├── App.java                     ← Entry point
    │   │       ├── logic/
    │   │       │   ├── TimingCalculator.java    ← Exposure logic (no UI)
    │   │       │   └── TimeFrameConverter.java  ← Conversion logic (no UI)
    │   │       ├── model/
    │   │       │   ├── Segment.java
    │   │       │   ├── DrawingEntry.java
    │   │       │   └── CalculatorResult.java
    │   │       └── ui/
    │   │           ├── AppState.java            ← Shared observable state
    │   │           ├── ExposureCalculatorTab.java
    │   │           └── TimeFrameConverterTab.java
    │   └── resources/com/animationcalc/
    │       ├── light.css
    │       └── dark.css
    └── test/
        └── java/com/animationcalc/
            ├── TimingCalculatorTest.java        ← 21 JUnit 5 tests
            └── TimeFrameConverterTest.java      ← 35 JUnit 5 tests
```

---

## Running the app

Open a PowerShell window, navigate to the project folder, and run:

```powershell
cd "C:\Users\kshet\OneDrive\Desktop\calculator"
mvn javafx:run
```

Maven will download all dependencies (JavaFX, JUnit) on the first run —
this takes a minute or two and requires an internet connection.
Subsequent runs are instant.

The window opens at 960 × 700 px and can be resized freely.

---

## Running the tests

```powershell
cd "C:\Users\kshet\OneDrive\Desktop\calculator"
mvn test
```

Expected output: **56 tests, 0 failures, 0 errors**.

A full HTML report is written to:
```
target/surefire-reports/
```

---

## Building a runnable JAR

```powershell
cd "C:\Users\kshet\OneDrive\Desktop\calculator"
mvn package
```

This creates two JARs in `target/`:

| File | Contents |
|------|----------|
| `animation-exposure-calculator-1.0.0.jar` | Thin JAR (no dependencies) |
| `animation-exposure-calculator-1.0.0-fat.jar` | **Fat JAR** – everything bundled in |

### Running the fat JAR

Because JavaFX requires explicit module flags, the simplest way to launch the
fat JAR is:

```powershell
java --module-path "C:\Path\To\javafx-sdk-21\lib" `
     --add-modules javafx.controls,javafx.fxml `
     -jar "target\animation-exposure-calculator-1.0.0-fat.jar"
```

Replace `C:\Path\To\javafx-sdk-21\lib` with the actual path to the JavaFX SDK
`lib` folder.  You can download the JavaFX SDK separately from
<https://gluonhq.com/products/javafx/>.

**Easier alternative – just use `mvn javafx:run`** every time; it handles all
module flags automatically and is the recommended way to launch during
development.

---

## Keyboard tips

| Action | How |
|--------|-----|
| Edit a segment's frames or step | Double-click the cell in the Segments table |
| Confirm an edit | Press **Enter** |
| Cancel an edit | Press **Escape** |
| Reorder segments | Select a row, then click **▲ Up** or **▼ Down** |

---

## Understanding start frame (Frame 0 vs Frame 1)

Traditional animation counts the first frame of a scene as **frame 1**.
Some software (and some compositing pipelines) counts from **frame 0**.

The toggle in the shared bar at the top applies to both tabs:

- **Frame 1** (default): frame 1 = timecode 00:00:00:00, frame 24 = 00:00:00:23 at 24 fps.
- **Frame 0**: frame 0 = timecode 00:00:00:00, frame 23 = 00:00:00:23 at 24 fps.

The converter tab shows a tooltip on each input field as a reminder.

---

## Common problems

### "mvn: command not found"
Maven is not on your PATH.  Re-open PowerShell **after** editing environment
variables, or close and reopen your terminal.

### The window is blank or throws a module error
Make sure you are running **JDK 17 or higher** (`java -version`).  JavaFX 21
does not work on JDK 11 or earlier.

### First build is slow
Maven is downloading ~40 MB of dependencies.  This only happens once; they are
cached in `C:\Users\<you>\.m2\repository`.

### "Package does not exist" compile error
Run `mvn dependency:resolve` to force Maven to re-download the dependency index,
then try `mvn javafx:run` again.

---

## Architecture notes for developers

The design keeps logic and UI strictly separated:

- `TimingCalculator` and `TimeFrameConverter` are plain Java classes with no
  imports from `javafx.*` or `javax.swing.*`.  They can be tested with plain
  JUnit without launching a display.
- `AppState` is a small JavaFX observable bean.  Both tabs bind to it; changing
  FPS in one tab is immediately visible in the other because they share the same
  instance.
- The UI tabs are constructed programmatically (no FXML) so the whole project
  compiles and runs with a single `mvn javafx:run` without any IDE setup.
