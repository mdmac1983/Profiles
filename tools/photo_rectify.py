#!/usr/bin/env python3
"""photo_rectify.py - turn a phone PHOTO of the tablet screen into a true 600 x 1024 screen image, so sizes can be measured in device px.

    python3 -I tools/photo_rectify.py <photo> <outdir> [--rotate 90] [--scale 2] [--name N] [--crop x0,y0,x1,y1]
    python3 -I tools/photo_rectify.py --selftest

WHY (Tj, 2026-11-06, measuring the lock screen against the stock tablet): a phone photo of a tablet is a perspective view; the only reliable way to compare sizes is to find the screen's four edges, undo the perspective
(a homography) and measure in device px. What the 2026-11-06 sessions learned (all of it is built in or noted here):
  * Photos from the phone often have NO EXIF orientation but are stored sideways: pass --rotate 90 (counter-clockwise) when the screen's top is at the photo's right edge.
  * The screen is found by fitting a line to each of its four edges (middle 70 % of each edge, outliers rejected), NOT by a bounding box. Edges that are almost black (a dark wallpaper at the screen's bottom) or cut
    off by the photo border make a corner wrong: the tool prints the quad and its aspect ratio next to the real 600 : 1024 = 0.586; if they differ by more than ~3 %, do not trust vertical measurements.
  * Best practice: BOTH tablets (stock and ours) in ONE photo, same pose and light; compare ratios inside the photo and use identical stock elements (clock digits, lock icon) as the scale / brightness standard.
  * The photos themselves are Tj's PRIVATE data: this tool refuses to write anywhere inside the git repository (the repo is public). Describe results in numbers in TASKS.md.
Needs numpy and Pillow only (no OpenCV / scipy). Run it with python3 -I (the input is untrusted data).
"""
import os
import sys

import numpy as np
from PIL import Image, ImageOps

DEV_W, DEV_H = 600, 1024


def load(path, rotate=0):
    im = ImageOps.exif_transpose(Image.open(path)).convert("RGB")
    if rotate:
        im = im.rotate(rotate, expand=True)
    return im


def _erode(m, n=1):
    for _ in range(n):
        m = m & np.roll(m, 1, 0) & np.roll(m, -1, 0) & np.roll(m, 1, 1) & np.roll(m, -1, 1)
    return m


def _dilate(m, n=1):
    for _ in range(n):
        m = m | np.roll(m, 1, 0) | np.roll(m, -1, 0) | np.roll(m, 1, 1) | np.roll(m, -1, 1)
    return m


def _largest_component(mask):
    h, w = mask.shape
    seen = np.zeros_like(mask, dtype=bool)
    best = []
    ys, xs = np.nonzero(mask)
    for y0, x0 in zip(ys.tolist(), xs.tolist()):
        if seen[y0, x0]:
            continue
        stack = [(y0, x0)]
        seen[y0, x0] = True
        comp = []
        while stack:
            y, x = stack.pop()
            comp.append((y, x))
            for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                ny, nx = y + dy, x + dx
                if 0 <= ny < h and 0 <= nx < w and mask[ny, nx] and not seen[ny, nx]:
                    seen[ny, nx] = True
                    stack.append((ny, nx))
        if len(comp) > len(best):
            best = comp
    out = np.zeros_like(mask)
    for y, x in best:
        out[y, x] = True
    return out


def _fit(p, q):
    """least squares q = m*p + c with outlier rejection"""
    p = np.asarray(p, float)
    q = np.asarray(q, float)
    keep = np.ones(len(p), bool)
    m = c = 0.0
    for _ in range(3):
        m, c = np.polyfit(p[keep], q[keep], 1)
        r = np.abs(q - (m * p + c))
        keep = r < max(2.0, 2.5 * np.std(r[keep]))
    return np.polyfit(p[keep], q[keep], 1)


def screen_quad(im, threshold=22, sc=8):
    """[(x, y)] of the screen's top-left, top-right, bottom-right, bottom-left in photo px, from four fitted edge lines."""
    small = im.resize((max(1, im.width // sc), max(1, im.height // sc)), Image.BILINEAR)
    mask = np.asarray(small).astype(np.int16).max(axis=2) > threshold
    mask = _largest_component(_dilate(_erode(mask, 2), 2))
    ys, xs = np.nonzero(mask)
    if len(ys) < 100:
        raise ValueError("no lit screen found: lower --threshold or check the photo")
    y0, y1, x0, x1 = ys.min(), ys.max(), xs.min(), xs.max()
    L, R, T, B = [], [], [], []
    for y in range(int(y0 + 0.15 * (y1 - y0)), int(y0 + 0.85 * (y1 - y0))):
        xx = np.nonzero(mask[y])[0]
        if len(xx):
            L.append((y, xx.min()))
            R.append((y, xx.max()))
    for x in range(int(x0 + 0.15 * (x1 - x0)), int(x0 + 0.85 * (x1 - x0))):
        yy = np.nonzero(mask[:, x])[0]
        if len(yy):
            T.append((x, yy.min()))
            B.append((x, yy.max()))
    L, R, T, B = (np.array(v) for v in (L, R, T, B))
    lines = [_fit(v[:, 0], v[:, 1]) for v in (L, R, T, B)]   # L, R: x = m*y + c ; T, B: y = m*x + c

    def corner(vert, hor):
        a, b = vert
        c, d = hor
        x = (a * d + b) / (1 - a * c)
        return ((x) * sc + sc / 2, (c * x + d) * sc + sc / 2)
    return [corner(lines[0], lines[2]), corner(lines[1], lines[2]), corner(lines[1], lines[3]), corner(lines[0], lines[3])]


def homography_coeffs(dst, src):
    """PIL PERSPECTIVE coefficients mapping OUTPUT (x, y) -> INPUT (u, v)."""
    a, b = [], []
    for (x, y), (u, v) in zip(dst, src):
        a.append([x, y, 1, 0, 0, 0, -u * x, -u * y]); b.append(u)
        a.append([0, 0, 0, x, y, 1, -v * x, -v * y]); b.append(v)
    return np.linalg.solve(np.array(a, float), np.array(b, float))


def rectify(im, quad, scale=2):
    w, h = DEV_W * scale, DEV_H * scale
    co = homography_coeffs([(0, 0), (w, 0), (w, h), (0, h)], quad)
    return im.transform((w, h), Image.PERSPECTIVE, tuple(co), Image.BICUBIC)


def quad_report(quad):
    top = np.hypot(quad[1][0] - quad[0][0], quad[1][1] - quad[0][1]); bot = np.hypot(quad[2][0] - quad[3][0], quad[2][1] - quad[3][1])
    lef = np.hypot(quad[3][0] - quad[0][0], quad[3][1] - quad[0][1]); rig = np.hypot(quad[2][0] - quad[1][0], quad[2][1] - quad[1][1])
    return "quad %s; top/bottom width %d/%d, left/right height %d/%d, aspect %.3f (a real screen is %.3f)" % (
        [(round(x), round(y)) for x, y in quad], top, bot, lef, rig, (top + bot) / (lef + rig), DEV_W / DEV_H)


def inside_repo(path):
    here = os.path.dirname(os.path.abspath(__file__))
    root = os.path.abspath(os.path.join(here, ".."))
    p = os.path.realpath(path)
    return p == root or p.startswith(root + os.sep)


def selftest():
    """a synthetic 600 x 1024 test screen photographed under a known perspective must come back within 2 % MAE and the quad within 3 photo px"""
    yy, xx = np.mgrid[0:DEV_H, 0:DEV_W]
    screen = np.stack([40 + 200 * xx / DEV_W, 60 + 120 * yy / DEV_H, 120 + 100 * ((xx // 60 + yy // 64) % 2)], axis=2).clip(0, 255).astype(np.uint8)
    scr = Image.fromarray(screen)
    W, H = 1500, 2000
    quad = [(180, 160), (1290, 120), (1330, 1860), (140, 1900)]   # a known trapezoid inside the photo
    co = homography_coeffs([(0, 0), (DEV_W, 0), (DEV_W, DEV_H), (0, DEV_H)], quad)   # screen -> photo needs the inverse mapping: build photo by sampling
    # photo pixel (u, v) -> screen (x, y): solve the inverse homography directly
    inv = homography_coeffs(quad, [(0, 0), (DEV_W, 0), (DEV_W, DEV_H), (0, DEV_H)])
    photo = scr.transform((W, H), Image.PERSPECTIVE, tuple(inv), Image.BICUBIC)
    found = screen_quad(photo)
    err = max(max(abs(a - c), abs(b - d)) for (a, b), (c, d) in zip(found, quad))
    rec = np.asarray(rectify(photo, found, 1)).astype(int)
    ref = np.asarray(scr).astype(int)
    core = (slice(40, DEV_H - 40), slice(40, DEV_W - 40))
    mae = np.abs(rec[core] - ref[core]).mean() / 255.0
    print("selftest: corner error %.1f photo px, rectified MAE %.4f" % (err, mae))
    ok = err <= 3.5 and mae < 0.02
    print("selftest:", "OK" if ok else "FAILED")
    return 0 if ok else 1


def main(argv):
    if "--selftest" in argv:
        return selftest()
    args = [a for a in argv if not a.startswith("--")]
    opts = {}
    for i, a in enumerate(argv):
        if a.startswith("--") and i + 1 < len(argv) and not argv[i + 1].startswith("--"):
            opts[a] = argv[i + 1]
    pos = []
    skip = False
    for i, a in enumerate(argv):
        if skip:
            skip = False
            continue
        if a.startswith("--"):
            skip = a in opts
            continue
        pos.append(a)
    if len(pos) != 2:
        print(__doc__)
        return 2
    photo, outdir = pos
    if inside_repo(outdir):
        print("REFUSED: %s is inside the git repository, which is public. Photos and anything derived from them stay OUT of it (CLAUDE.md); use the scratchpad." % outdir)
        return 3
    os.makedirs(outdir, exist_ok=True)
    im = load(photo, int(opts.get("--rotate", 0)))
    quad = screen_quad(im, int(opts.get("--threshold", 22)))
    print(quad_report(quad))
    scale = int(opts.get("--scale", 2))
    rec = rectify(im, quad, scale)
    name = opts.get("--name", os.path.splitext(os.path.basename(photo))[0])
    out = os.path.join(outdir, "rect_%s.png" % name)
    rec.save(out)
    print("wrote", out, "(%d x %d px = %d x %d device px at %dx)" % (rec.width, rec.height, DEV_W, DEV_H, scale))
    if "--crop" in opts:
        x0, y0, x1, y1 = (int(float(v) * scale) for v in opts["--crop"].split(","))
        cp = os.path.join(outdir, "crop_%s.png" % name)
        rec.crop((x0, y0, x1, y1)).save(cp)
        print("wrote", cp)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
