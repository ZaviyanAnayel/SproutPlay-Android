# SproutPlay — Candy Crush-style learning games for kids

SproutPlay is a native Android app (Kotlin + Jetpack Compose) by **Zaviyan LLC**.
Kids aged 3–11 learn by playing Candy Crush-style match-3 games — no quizzes,
no worksheets, just juicy candy popping that teaches.

## The games

Five match-3 games on one shared engine (8×8 board, swap, match 3/4/5,
cascades, striped candies, color bombs, combos, stars), 10 levels each —
50 levels in total:

| Game | Teaches | How |
| --- | --- | --- |
| 🔢 Number Crunch | Math | Pop candies to add up to the target number |
| 🔤 Word Pop | Reading | Pop the letters to spell the magic word |
| 🧩 Pattern Pop | Logic | Collect the exact color pattern |
| 🌍 Science Pop | Science | Collect water, sunshine and leaves |
| 🍬 Candy Blast | Arcade | Pure score attack — biggest combos win |

## Privacy first

- **Fully offline** — no internet permission, no network calls, no ads,
  no accounts, no analytics. No API key is needed for anything.
- Child profiles (name, age, stars, stickers) live only on the device
  in a local Room database and never leave it.
- Parent Zone is protected by a math gate; a daily screen-time limit
  with a stretch-break reminder is built in.

## Build

**Prerequisites:** [Android Studio](https://developer.android.com/studio)
(AGP 9.1.1, Kotlin 2.4.21, Gradle 9.3.1, targetSdk 36)

```bash
./gradlew assembleDebug   # debug APK
./gradlew bundleRelease   # release AAB (needs the signing env vars below)
```

CI builds every push on GitHub Actions (`.github/workflows/build.yml`).

## Release signing

The release keystore is **not** in this repo. Set these environment
variables (in CI they are GitHub secrets):

- `SPROUT_KEYSTORE_PATH` — path to `sproutplay-upload-key.jks`
- `SPROUT_STORE_PASSWORD` — keystore password
- `SPROUT_KEY_ALIAS` — `sproutplay-upload`
- `SPROUT_KEY_PASSWORD` — key password

Without them the release build stays unsigned; the debug build is unaffected.

## Package

`com.zaviyanllc.sproutplay` — version 1.0 (versionCode 1).
