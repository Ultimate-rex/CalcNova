# CalcNova — Phase 1 (v2 — advanced UI + live calculation)

Basic Calculator + Scientific Calculator + Developer/Programmer Calculator,
built with Kotlin + Jetpack Compose.

## What's new in this update

- **Auto-calculate (live results)** — type an expression and the result
  updates as you go, no need to press `=`. Turn it off in Settings if you
  prefer the classic press-equals flow.
- **Advanced math engine** — the expression evaluator now understands
  functions (`sin cos tan asin acos atan sinh cosh tanh log ln sqrt cbrt abs`),
  constants (`π`, `e`), power (`^`), and factorial (`!`) — not just +−×÷.
- **Scientific calculator rebuilt** — it's now a real button-grid calculator
  (like a physical scientific calculator) instead of a dropdown+text-field
  form. DEG/RAD/GRAD switch included.
- **Developer calculator improvements** — base conversion and bitwise
  operations update live as you type (debounced), plus an 8-bit visual
  grid showing the binary representation.
- **New Settings screen** — theme (System/Light/Dark), auto-calculate
  toggle, default angle mode, haptic feedback toggle. Stored in
  `settings.json` — still no database.
- **Animations** — buttons scale down slightly on press, results animate
  in/out when they change, and switching tabs now fades/slides instead of
  cutting instantly.
- **App icon** added (blue rounded calculator glyph).

## Key design decision: no database

There is **no Room, no SQLite, no DBMS anywhere in this app.**

Instead:

- **Calculations go through a JSON API layer** (`api/CalculatorApi.kt`).
  Each function is written like a REST endpoint: it takes a `@Serializable`
  request data class and returns a **JSON string** response (or a JSON
  error object).
- **History is a JSON file** (`core/JsonHistoryStore.kt`) — `history.json`
  in the app's private storage.
- **Settings are a JSON file** (`core/JsonSettingsStore.kt`) — `settings.json`,
  same pattern.

## What's included in Phase 1

| Screen | File |
|---|---|
| Basic calculator (live calc) | `feature/basic/` |
| Scientific calculator (live calc, button grid) | `feature/scientific/` |
| Developer/Programmer calculator (live conversion + bit grid) | `feature/programmer/` |
| Settings (theme, auto-calc, haptics) | `feature/settings/` |
| History (JSON file, not DB) | `feature/history/HistoryScreen.kt` |
| JSON API layer + advanced expression engine | `api/CalculatorApi.kt`, `api/ApiModels.kt` |
| JSON persistence (history + settings) | `core/JsonHistoryStore.kt`, `core/JsonSettingsStore.kt` |
| Shared animated button component | `ui/components/CalcButton.kt` |
| Navigation + bottom bar + screen transitions | `ui/navigation/NavGraph.kt` |

## How to open and run

1. Open the `CalcNova/` folder directly in Android Studio (Hedgehog or newer),
   or push it to GitHub and let the included GitHub Actions workflow
   (`.github/workflows/build-apk.yml`) build a debug APK for you.
2. Let Gradle sync — dependencies: Compose, Material 3, Navigation Compose,
   Compose Animation, kotlinx.serialization. Still no Room, no networking
   library required for Phase 1.
3. Run on an emulator or device (minSdk 24).

## What's NOT in Phase 1 yet

GST calculator, Binomial/Pascal engine, and the optional Python backend from
the original spec are not built yet. Say the word and we build GST next,
then Binomial/Pascal — both slot into the same JSON API + live-calculation
pattern used here.

