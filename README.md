# Twin Trap

**Follow GOLD and PINK. Only 10% can solve it.**

Twin Trap is a native Kotlin + Jetpack Compose cup/shell game for Android.
Watch the balls, follow the cups through the shuffle, and tap the cup
hiding the ball — before the 15-second timer runs out.

- 10 levels, each harder than the last: 6 to 12 cups, 1–2 balls, swaps from
  8 up to 30, speeds down to 0.3s per swap. Dimmed "Lights Out" shuffles,
  a two-row "Split Brain", and a spinning "Orbit" ring.
- 3 wrong picks and the level is lost — retry until you beat it.
- Stars (1–3), coins (10 × level per ball found), and trophies
  ("Halfway Hero" at level 5, "Top 10% Club" at level 10).
- Original synthesized music and sound effects (generated with ffmpeg,
  bundled in `app/src/main/res/raw/`) — no copyrighted audio.
- Fully offline. No ads, no accounts, no tracking, no purchases.
  Progress (stars, coins, trophies) is stored locally in Room.

## Tech

- Package: `com.zaviyanllc.sproutplay` (the Play Console draft was
  registered under this package; the package is invisible to users).
- Display name: **Twin Trap**.
- Build: GitHub Actions (`.github/workflows/build.yml`) — debug APK on
  every push, signed release AAB + APK on manual dispatch.
- Release signing comes from environment variables only
  (`SPROUT_KEYSTORE_PATH`, `SPROUT_STORE_PASSWORD`, `SPROUT_KEY_ALIAS`,
  `SPROUT_KEY_PASSWORD`) — never hardcode keystore passwords.
- Gradle builds are blocked on the dev VM by sandbox TCP policy; use CI.

## Levels

| # | Name | Cups | Balls | Swaps | Speed |
|---|------|------|-------|-------|-------|
| 1 | First Look | 6 | gold | 8 | 1.00s |
| 2 | Twin Trouble | 6 | gold+pink | 10 | 0.90s |
| 3 | Lights Out | 6 | pink | 12 | 0.90s (dimmed) |
| 4 | Speed Run | 6 | gold | 14 | 0.55s |
| 5 | Nine Cups | 9 | gold | 16 | 0.80s |
| 6 | Split Brain | 6 (2 rows) | gold+pink | 12 | 0.80s |
| 7 | Orbit | 9 (circle) | gold | 18 | 0.70s |
| 8 | Brain Breaker | 12 | gold | 20 | 0.45s |
| 9 | Boss Level | 12 | lime | 24 | 0.35s |
| 10 | Impossible | 12 | gold+pink | 30 | 0.30s (dimmed) |
