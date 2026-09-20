# CalcNova — v2.5.0

A modern, fully offline Android calculator app: Basic, Scientific,
Developer/Programmer, and GST calculators, with live auto-calculation,
calculation history, and Material You theming — built with Kotlin and
Jetpack Compose.

## Features

- **Basic calculator** — live results as you type (no need to press `=`,
  toggleable in Settings), `+ − × ÷ % ( )`.
- **Scientific calculator** — a real button-grid scientific calculator
  supporting `sin cos tan asin acos atan sinh cosh tanh log ln sqrt cbrt
  abs`, constants `π` and `e`, power `^`, factorial `!`, and DEG/RAD/GRAD
  angle modes.
- **Developer calculator** — base conversion (BIN/OCT/DEC/HEX) and
  bitwise operations (`AND OR XOR NOT NAND NOR XNOR SHL SHR`) that update
  live as you type, plus an 8-bit visual grid.
- **GST calculator** — add or remove GST at common Indian tax rates (0,
  5, 12, 18, 28%), with CGST/SGST breakdown.
- **History** — every calculation is saved locally with search and
  per-entry delete.
- **Settings** — theme (System/Light/Dark), Material You dynamic color
  toggle, auto-calculate toggle, default angle mode, haptic feedback.
- **Material You** — on Android 12+, the app's colors are generated from
  your wallpaper by default, so it visually blends in with the rest of
  your phone. Falls back to a designed brand palette on older devices.
- **Animations** — buttons scale on press, results animate in/out, and
  screen transitions fade/slide.
- **Predictive back gesture and edge-to-edge display** supported.

## Privacy & Legal

- [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md) — CalcNova is 100% offline,
  requests zero permissions, and collects no data of any kind.
- [`TERMS_OF_SERVICE.md`](TERMS_OF_SERVICE.md)
- [`LICENSE`](LICENSE) — MIT

## Key design decision: no database

There is **no Room, no SQLite, no DBMS anywhere in this app.**

- **Calculations go through a JSON API layer** (`api/CalculatorApi.kt`).
  Each function is written like a REST endpoint: it takes a
  `@Serializable` request data class and returns a **JSON string**
  response (or a JSON error object).
- **History is a JSON file** (`core/JsonHistoryStore.kt`) — `history.json`
  in the app's private storage.
- **Settings are a JSON file** (`core/JsonSettingsStore.kt`) —
  `settings.json`, same pattern.

## Project structure

```
app/src/main/java/com/calcnova/app/
├── api/                    # JSON "endpoints" + advanced expression engine
├── core/                   # JSON-file persistence (history, settings)
├── feature/
│   ├── hub/                # Calculator tab container (Basic/Scientific/Developer/GST)
│   ├── basic/
│   ├── scientific/
│   ├── programmer/
│   ├── gst/
│   ├── history/
│   └── settings/
├── ui/
│   ├── navigation/         # Bottom nav: Calculator / History / Settings
│   ├── theme/              # Color, Type, Theme (Material You aware)
│   └── components/         # Shared animated CalcButton
└── MainActivity.kt
```

## Requirements & versions

- **compileSdk / targetSdk: 36** (Android 16) — required for new Play
  Store submissions since August 31, 2026.
- **minSdk: 24** (Android 7.0+)
- **Android Gradle Plugin: 8.13.0**, **Gradle: 8.13**, **Kotlin: 2.1.20**
  (Compose compiler as a Kotlin plugin, not the old
  `kotlinCompilerExtensionVersion`).
- **Compose BOM: 2026.08.00**

## How to build

**Option A — Android Studio:** open the `CalcNova/` folder directly
(Hedgehog or newer). Let Gradle sync, then Run.

**Option B — GitHub Actions (no computer needed):** push this repo to
GitHub. The included workflow (`.github/workflows/build-apk.yml`) builds
a debug APK automatically and uploads it as a downloadable artifact.

**Option C — command line:**
```
gradle assembleDebug
```
(No project-local Gradle wrapper is bundled; use a system Gradle 8.13+
install, or Android Studio's bundled Gradle.)

## Publishing to Google Play

This app is already configured to meet the current target API
requirement (API 36). Before publishing:

1. **Enable minification for the release build.** It's off by default in
   `app/build.gradle.kts` (`isMinifyEnabled = false`) so this project
   stays predictable without an emulator to verify R8 output. Turn
   `isMinifyEnabled` and `isShrinkResources` both to `true` together, then
   do a full test pass — a `proguard-rules.pro` with the necessary
   `kotlinx.serialization` keep rules is already included.
2. **Sign the release build** with your own keystore (Android Studio:
   Build > Generate Signed App Bundle/APK). Never commit a keystore or
   its passwords to version control.
3. **Host `PRIVACY_POLICY.md` somewhere public** (e.g. GitHub's raw file
   URL, or GitHub Pages) and paste that URL into Play Console's Privacy
   Policy field and the Data Safety form. Since the app collects nothing,
   the Data Safety form should declare no data collection.
4. Fill in the Play Console listing (screenshots, description, content
   rating) as usual.

## What's not built yet

A Binomial Theorem / Pascal's Triangle module, and an optional
Python/FastAPI backend for very large symbolic calculations, were part of
the original concept but aren't implemented in this version. The JSON API
+ live-calculation pattern used throughout this app is designed so they
can slot in the same way if you want them next.

## Version history

- **2.5.0** — Modernized build (AGP 8.13 / API 36 / Kotlin 2.1.20), GST
  calculator, Calculator tab hub (Basic/Scientific/Developer/GST),
  Material You dynamic color toggle, refreshed typography and color
  system, history search, predictive-back and edge-to-edge support,
  LICENSE/Privacy Policy/Terms of Service added.
- **2.0** — Auto-calculate, advanced expression engine (functions,
  constants, power, factorial), rebuilt Scientific calculator, live
  Developer calculator with bit visualization, Settings screen,
  animations, app icon.
- **1.0** — Basic, Scientific, and Developer calculators with a JSON API
  layer and JSON-file history (Phase 1).
