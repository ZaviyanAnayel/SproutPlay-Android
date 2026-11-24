#!/usr/bin/env python3
"""UIAutomator helpers for SproutPlay screenshot capture.

Usage:
  uiauto.py tap-id <substring>        tap node whose resource-id/content-desc contains substring
  uiauto.py tap-text <exact text>     tap node whose text equals the given text
  uiauto.py swap                      tap two horizontally adjacent tiles in the match-3 board
"""
import subprocess
import sys
import xml.etree.ElementTree as ET


def run(cmd, check=False):
    return subprocess.run(cmd, capture_output=True, text=True, check=check)


def screen_size():
    out = run(["adb", "shell", "wm", "size"]).stdout
    # "Physical size: 1080x1920"
    try:
        dims = out.strip().split(":")[1].strip().split("x")
        return int(dims[0]), int(dims[1])
    except Exception:
        return 1080, 1920


def dump(path="/tmp/ui.xml"):
    run(["adb", "shell", "uiautomator", "dump", "/sdcard/ui.xml"])
    run(["adb", "pull", "/sdcard/ui.xml", path])
    return ET.parse(path).getroot()


def bounds_center(b):
    parts = b.replace("[", "").replace("]", ",").strip(",").split(",")
    x1, y1, x2, y2 = map(int, parts[:4])
    return (x1 + x2) // 2, (y1 + y2) // 2


def tap(x, y):
    print(f"tap {x},{y}", flush=True)
    run(["adb", "shell", "input", "tap", str(x), str(y)], check=True)


def cmd_tap_id(substr):
    root = dump()
    for node in root.iter("node"):
        a = node.attrib
        hay = a.get("resource-id", "") + " " + a.get("content-desc", "")
        if substr in hay:
            x, y = bounds_center(a["bounds"])
            tap(x, y)
            return 0
    print(f"NOT FOUND id:{substr}", flush=True)
    return 1


def cmd_tap_text(text):
    root = dump()
    for node in root.iter("node"):
        a = node.attrib
        if a.get("text", "") == text:
            x, y = bounds_center(a["bounds"])
            tap(x, y)
            return 0
    print(f"NOT FOUND text:{text}", flush=True)
    return 1


def cmd_swap():
    w, h = screen_size()
    print(f"screen {w}x{h}", flush=True)
    root = dump()
    # The candy board fills the lower ~2/3 of the screen. Tile faces show as
    # text nodes (digits / letters / emoji) centred on their tiles.
    y_low, y_high = int(h * 0.31), int(h * 0.92)
    cell = w // 8
    tiles = []
    for node in root.iter("node"):
        a = node.attrib
        t = a.get("text", "")
        if not t:
            continue
        x, y = bounds_center(a["bounds"])
        if 20 <= x <= w - 20 and y_low <= y <= y_high:
            tiles.append((y, x, t))
    tiles.sort()
    for i in range(len(tiles)):
        for j in range(i + 1, len(tiles)):
            y1, x1, _ = tiles[i]
            y2, x2, _ = tiles[j]
            if abs(y1 - y2) <= 15 and cell - 35 <= abs(x2 - x1) <= cell + 35:
                tap(x1, y1)
                tap(x2, y2)
                print(f"swapped ({x1},{y1}) <-> ({x2},{y2})", flush=True)
                return 0
    print(f"no adjacent tile pair found among {len(tiles)} text nodes", flush=True)
    return 1


if __name__ == "__main__":
    cmd = sys.argv[1]
    if cmd == "tap-id":
        sys.exit(cmd_tap_id(sys.argv[2]))
    if cmd == "tap-text":
        sys.exit(cmd_tap_text(sys.argv[2]))
    if cmd == "swap":
        sys.exit(cmd_swap())
    print(f"unknown command: {cmd}")
    sys.exit(2)
