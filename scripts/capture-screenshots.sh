#!/usr/bin/env bash
# Scripted screenshot capture for SproutPlay on the CI emulator.
# Runs inside reactivecircus/android-emulator-runner (emulator already booted).
set -euo pipefail
cd "$GITHUB_WORKSPACE"
mkdir -p shots

APK=app/build/outputs/apk/debug/app-debug.apk
echo "== emulator screen =="
adb shell wm size || true
echo "== install =="
adb install -r "$APK"
# Hide the status bar while our app is in the foreground (cleaner shots).
adb shell settings put global policy_control immersive.status=com.zaviyanllc.sproutplay || true

echo "== launch =="
adb shell am start -n com.zaviyanllc.sproutplay/.MainActivity
sleep 6
adb exec-out screencap -p > shots/01-onboarding.png
echo "shot 01 onboarding: $(stat -c%s shots/01-onboarding.png) bytes"

echo "== onboarding: name + age =="
python3 scripts/uiauto.py tap-id onboarding_name_input
adb shell input text "Alex"
adb shell input keyevent 66
sleep 1
python3 scripts/uiauto.py tap-id onboarding_next_btn
sleep 2
python3 scripts/uiauto.py tap-id age_chip_7
sleep 1
python3 scripts/uiauto.py tap-id onboarding_start_btn
sleep 4
adb exec-out screencap -p > shots/02-home.png
echo "shot 02 home: $(stat -c%s shots/02-home.png) bytes"

echo "== catalog =="
python3 scripts/uiauto.py tap-text "50 Games"
sleep 2
adb exec-out screencap -p > shots/03-catalog.png
echo "shot 03 catalog: $(stat -c%s shots/03-catalog.png) bytes"

echo "== open Number Crunch =="
python3 scripts/uiauto.py tap-id catalog_game_candy_math
sleep 2
python3 scripts/uiauto.py tap-text "Play Now"
sleep 3
adb exec-out screencap -p > shots/04-game-board.png
echo "shot 04 game board: $(stat -c%s shots/04-game-board.png) bytes"

echo "== make one swap, then capture mid-game =="
python3 scripts/uiauto.py swap || echo "swap not possible, keeping board shot"
sleep 3
adb exec-out screencap -p > shots/05-game-mid.png
echo "shot 05 game mid: $(stat -c%s shots/05-game-mid.png) bytes"

ls -la shots/
