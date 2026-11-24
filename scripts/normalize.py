#!/usr/bin/env python3
"""Normalize captured PNGs to exactly 1080x1920 (Play phone screenshots).

- 1080x1920: kept as-is.
- 1080x2400 (skin option ignored): centre-crop the 9:16 window.
- anything else: centre-crop to 9:16, then resize to 1080x1920.
"""
import sys
from PIL import Image

for path in sys.argv[1:]:
    im = Image.open(path)
    w, h = im.size
    if (w, h) == (1080, 1920):
        print(f"{path}: already 1080x1920")
        continue
    target = 9 / 16
    if w / h > target:
        nw = int(h * target)
        x = (w - nw) // 2
        box = (x, 0, x + nw, h)
    else:
        nh = int(w / target)
        y = (h - nh) // 2
        box = (0, y, w, y + nh)
    im.crop(box).resize((1080, 1920), Image.LANCZOS).save(path)
    print(f"{path}: {w}x{h} -> 1080x1920")
