#!/usr/bin/env bash
# Scripted screenshot capture for SproutPlay on the CI emulator.
# Resilient by design: a missed tap NEVER kills the run — every step warns and
# continues, and diagnostics (UI dumps, pids, sizes) are saved under shots/
# so a failed run can still be debugged from the repo.
set -uo pipefail
cd "$GITHUB_WORKSPACE"
mkdir -p shots

APK=app/build/outputs/apk/debug/app-debug.apk

diag() { echo "[$(date +%T)] $*" | tee -a shots/diagnostics.txt; }
tap_id() { python3 scripts/uiauto.py tap-id "$1"; }
tap_text() { python3 scripts/uiauto.py tap-text "$1"; }
shot() {
  if adb exec-out screencap -p > "shots/$1"; then
    diag "shot $1: $(stat -c%s "shots/$1") bytes"
  else
    diag "WARN: shot $1 failed"
  fi
}
dump_ui() {
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 \
    && adb pull /sdcard/ui.xml "shots/$1" >/dev/null 2>&1 \
    || diag "WARN: ui dump $1 failed"
}

diag "wm size: $(adb shell wm size | tr -d '\r')"
diag "installing $APK"
adb install -r "$APK" || diag "WARN: install failed"
# Hide the status bar while our app is in the foreground (cleaner shots).
adb shell settings put global policy_control immersive.status=com.zaviyanllc.sproutplay || true

diag "launching app"
adb shell am start -n com.zaviyanllc.sproutplay/.MainActivity
sleep 6
diag "app pid: $(adb shell pidof com.zaviyanllc.sproutplay | tr -d '\r' || echo NONE)"
dump_ui dump-01-onboarding.xml
shot 01-onboarding.png

diag "onboarding: name + age"
tap_id onboarding_name_input || tap_text "Child's Name" || diag "WARN: name field not tappable"
adb shell input text "Alex" || diag "WARN: input text failed"
adb shell input keyevent 66 || true
sleep 1
tap_id onboarding_next_btn || tap_text "Next: Pick Age" || diag "WARN: next button missed"
sleep 2
tap_id age_chip_7 || tap_text "7" || diag "WARN: age chip missed"
sleep 1
tap_id onboarding_start_btn || tap_text "Let's Play & Learn!" || diag "WARN: start button missed"
sleep 4
diag "app pid after start: $(adb shell pidof com.zaviyanllc.sproutplay | tr -d '\r' || echo NONE)"
dump_ui dump-02-home.xml
shot 02-home.png

diag "catalog"
tap_text "50 Games" || diag "WARN: 50 Games nav missed"
sleep 2
shot 03-catalog.png

diag "open Number Crunch"
tap_id catalog_game_candy_math || tap_text "Number Crunch" || diag "WARN: math card missed"
sleep 2
tap_text "Play Now" || diag "WARN: Play Now missed"
sleep 3
dump_ui dump-04-game.xml
shot 04-game-board.png

diag "make one swap, then capture mid-game"
python3 scripts/uiauto.py swap || diag "WARN: swap not possible, keeping board shot"
sleep 3
shot 05-game-mid.png

diag "done. files:"
ls -la shots/ | tee -a shots/diagnostics.txt
