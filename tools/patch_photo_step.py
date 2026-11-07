#!/usr/bin/env python3
"""patch_photo_step.py - is the wallpaper PATCH under the stock power menu's two buttons darker or lighter than the real screen around it? Measured inside ONE phone photo.

    python3 -I tools/patch_photo_step.py <photo> [<photo> ...]
    python3 -I tools/patch_photo_step.py --selftest

WHY (Tj, 2026-11-07, v0.8.1: "the final wallpaper patch is very slightly too dark and I can see the square patch, but barely"; TASKS.md BA4a): on the lock screen the helper hides the stock menu's third slot (Screenshot)
with a patch of bare wallpaper. A patch that is a little off is a hard-edged rectangle on a smooth wallpaper, so the error shows as a STEP across its bottom edge. Measuring that inside one photo needs
no second photo (two photos of the tablet have different exposure and white balance: a menu-vs-bare ratio is useless; tried 2026-11-07).

INPUT: a photo of the tablet with the stock power menu OPEN on the lock screen (the two-item card: Power off + Restart), after the 300 ms fade, with the card and the area below it in the frame. Phone EXIF
orientation is honoured. The photo is Tj's PRIVATE data: this tool only READS it and prints numbers (never copy a photo into the repo; the repo is public).

METHOD: find the card (the biggest bright block), its item pitch = height / 2. Take the mean row profile of the columns under the card (12 % margin) and of a control strip left of the card. For each candidate edge row
between 0.97 and 1.15 item pitches below the card, fit a line on each side (0.05-0.40 pitch away), extrapolate both to the edge and subtract. Net step = inside - control (luminance; per channel too). Positive = the
patch is DARKER than the wallpaper under it, in camera units 0-255 (auto exposure and tone curve apply: the card's white reads ~247, so the gain is near 1; direction and rough size only, the helper's own screenshot
measurement is the exact one). The left edge (rows of the patch) is printed as a second opinion (raw: the wallpaper's own horizontal gradient is not removed).

RESULT 2026-11-07 on v0.8.1 (four photos, all agree): the edge sits 1.04 to 1.10 pitches below the card, the patch is darker by +4.1 to +6.4 units at brightness levels from 38 to 127 (a nearly constant step: an
ADDITIVE part of the real scrim, which a scalar dim can never remove; v0.8.2 measures scale + offset on the tablet). After v0.8.2 the same command should show 'no clear step'.
Needs numpy and Pillow only. Run it with python3 -I (the input is untrusted data).
"""
import importlib.util
import os
import sys

import numpy as np
from PIL import Image, ImageOps

EDGE_FROM, EDGE_TO = 0.97, 1.15      # where the patch's bottom edge is searched, in item pitches below the card (v0.8.1 geometry: 1.04-1.10 measured)
EXPECT_ASPECT = 1.93                 # card height / width of the two-item lock-screen menu


def _lcc():
    spec = importlib.util.spec_from_file_location("photo_rectify", os.path.join(os.path.dirname(os.path.abspath(__file__)), "photo_rectify.py"))
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod._largest_component


def lum(p):
    return 0.2126 * p[..., 0] + 0.7152 * p[..., 1] + 0.0722 * p[..., 2]


def find_card(a):
    """(L, T, R, B) of the biggest bright block, in px, or None."""
    sc = max(1, min(a.shape[0], a.shape[1]) // 400)
    small = a[: a.shape[0] // sc * sc, : a.shape[1] // sc * sc].reshape(a.shape[0] // sc, sc, a.shape[1] // sc, sc, 3).mean((1, 3))
    m = lum(small) > 190
    if m.sum() < 20:
        return None
    m = _lcc()(m)
    ys, xs = np.nonzero(m)
    y0, y1, x0, x1 = max(0, ys.min() * sc - 2 * sc), min(a.shape[0], (ys.max() + 1) * sc + 2 * sc), max(0, xs.min() * sc - 2 * sc), min(a.shape[1], (xs.max() + 1) * sc + 2 * sc)
    sub = lum(a[y0:y1, x0:x1]) > 190
    rows, cols = sub.sum(1), sub.sum(0)
    r, c = np.where(rows > 0.5 * rows.max())[0], np.where(cols > 0.5 * cols.max())[0]
    return int(c.min() + x0), int(r.min() + y0), int(c.max() + x0), int(r.max() + y0)


def _at(x, v, at):
    return np.polyval(np.polyfit(x, v, 1), at)


def _step(y, prof, yb, item):
    lo = (y >= yb - 0.40 * item) & (y <= yb - 0.05 * item)
    hi = (y >= yb + 0.05 * item) & (y <= yb + 0.40 * item)
    low = np.array([_at(y[lo], prof[lo, c], yb) for c in range(3)])
    high = np.array([_at(y[hi], prof[hi, c], yb) for c in range(3)])
    return high - low, high


def _left_step(a, L, W, y0, y1):
    x = np.arange(max(0, int(L - 0.45 * W)), int(L + 0.45 * W))
    prof = a[int(y0):int(y1), x[0]:x[-1] + 1].mean(0)
    lo, hi = (x >= L - 0.40 * W) & (x <= L - 0.04 * W), (x >= L + 0.04 * W) & (x <= L + 0.40 * W)
    return np.array([_at(x[hi], prof[hi, c], L) - _at(x[lo], prof[lo, c], L) for c in range(3)])


def measure(a):
    """dict of results for an oriented photo array (H, W, 3 float), or a dict with 'error'."""
    box = find_card(a)
    if box is None:
        return {"error": "no bright card found (is the stock power menu open and in the frame?)"}
    L, T, R, B = box
    W, H = R - L, B - T
    item = H / 2.0
    out = {"card": (L, T, R, B), "item": item, "aspect": H / W, "flags": []}
    if not 1.6 <= H / W <= 2.3:
        out["flags"].append("the card's height/width is %.2f, a two-item lock-screen menu is ~%.2f: is the third item (Screenshot) visible, or is the card cut off?" % (H / W, EXPECT_ASPECT))
    y0, y1 = int(B + 0.03 * item), min(a.shape[0], int(B + 1.75 * item))
    if y1 - y0 < 1.3 * item or L - 0.95 * W < 0:
        return dict(out, error="the photo does not show enough below / left of the card (need 1.75 item pitches below it and a strip on its left)")
    y = np.arange(y0, y1)
    x_a, x_m, x_b = int(L + 0.12 * W), int(L + 0.50 * W), int(R - 0.12 * W)
    prof = lambda x0, x1: np.median(a[y0:y1, x0:x1], axis=1)      # median over columns: a bright dust speck does not move it
    p_in, p_ct = prof(x_a, x_b), prof(int(L - 0.95 * W), int(L - 0.20 * W))
    p_l, p_r = prof(x_a, x_m), prof(x_m, x_b)                    # the two halves of the patch: repeatability
    cand = []
    for yb in np.arange(B + EDGE_FROM * item, B + EDGE_TO * item, max(1.0, item / 80)):
        s_in, lvl = _step(y, p_in, yb, item)
        s_ct, _ = _step(y, p_ct, yb, item)
        cand.append((lum(s_in) - lum(s_ct), yb, s_in, s_ct, lvl))
    k = int(np.abs(np.array([c[0] for c in cand])).argmax())
    net, yb, s_in, s_ct, lvl = cand[k]
    half = [float(lum(_step(y, p, yb, item)[0]) - lum(s_ct)) for p in (p_l, p_r)]
    out.update(edge=(yb - B) / item, net=float(net), per_channel=s_in - s_ct, control=s_ct, level=lvl, level_lum=float(lum(lvl)), halves=half)
    agree = half[0] * net > 0 and half[1] * net > 0 and min(abs(half[0]), abs(half[1])) >= 0.4 * abs(net)
    out["clear"] = bool(abs(net) >= 2.0 and agree and abs(lum(s_ct)) < 0.5 * abs(net))
    if (lvl > 240).any():
        out["flags"].append("a channel is above 240 on the wallpaper side (%s): the camera clips it, ignore that channel" % "".join("RGB"[i] for i in range(3) if lvl[i] > 240))
    if abs(lum(s_ct)) >= 0.5 * abs(net) and abs(net) >= 2.0:
        out["flags"].append("the control strip shows a similar step (%+.1f): the wallpaper itself is not smooth here, do not trust the number" % lum(s_ct))
    if abs(net) >= 2.0 and not agree:
        out["flags"].append("the left and right halves of the patch disagree (%+.1f / %+.1f): do not trust the number" % (half[0], half[1]))
    out["left_raw"] = _left_step(a, L, W, B + 0.15 * item, B + 0.90 * item)
    return out


def report(name, r):
    print("== " + name)
    if "card" in r:
        L, T, R, B = r["card"]
        print("   card x %d..%d y %d..%d (%d x %d px, h/w %.2f), item pitch %.0f px" % (L, R, T, B, R - L, B - T, r["aspect"], r["item"]))
    if "error" in r:
        print("   ERROR: " + r["error"])
        return
    pc, lv = r["per_channel"], r["level"]
    if r["clear"]:
        word = "DARKER" if r["net"] > 0 else "LIGHTER"
        print("   the patch is %s than the wallpaper under it by %.1f units of 255 (%.1f %% of the level %.0f); halves %+.1f / %+.1f; edge %.2f pitches below the card" % (word, abs(r["net"]), 100 * abs(r["net"]) / max(1.0, r["level_lum"]), r["level_lum"], r["halves"][0], r["halves"][1], r["edge"]))
    else:
        print("   NO CLEAR STEP (net %+.1f, halves %+.1f / %+.1f, control %+.1f): the patch is invisible in this photo, or the photo is too soft / the wallpaper too busy to tell" % (r["net"], r["halves"][0], r["halves"][1], lum(r["control"])))
    print("   per channel (positive = patch darker): R %+.1f  G %+.1f  B %+.1f | wallpaper-side levels R %.0f G %.0f B %.0f | control strip %+.1f | left edge raw R %+.1f G %+.1f B %+.1f (negative = patch darker)" % (
        pc[0], pc[1], pc[2], lv[0], lv[1], lv[2], lum(r["control"]), r["left_raw"][0], r["left_raw"][1], r["left_raw"][2]))
    for f in r["flags"]:
        print("   NOTE: " + f)


def synth(offset, seed=1):
    """a fake photo: smooth wallpaper with streaks, a white two-item card, the surround = 0.8*wall + 5, the patch = 0.8*wall + offset"""
    rng = np.random.default_rng(seed)
    h, w = 1000, 800
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    wall = np.stack([40 + 0.05 * xx + 6 * np.sin(xx / 23.0), 90 + 0.08 * yy - 0.03 * xx + 5 * np.sin(xx / 31.0), 170 + 0.03 * yy], axis=2)
    img = 0.8 * wall + 5.0
    L, R, T = 400, 520, 300
    B = T + int(round((R - L) * EXPECT_ASPECT))
    item = (B - T) / 2.0
    pe = int(B + 1.05 * item)
    img[B:pe, L:R] = 0.8 * wall[B:pe, L:R] + offset
    img[T:B, L:R] = 245.0
    return (img + rng.normal(0, 1.2, img.shape)).clip(0, 255)


def selftest():
    ok = True
    for off, expect in ((0.0, +5.0), (5.0, 0.0), (10.0, -5.0)):
        r = measure(synth(off))
        got = r.get("net", float("nan"))
        good = abs(got - expect) <= 1.0 and (r["clear"] == (abs(expect) > 0)) and 0.95 <= r.get("edge", 0) <= 1.2 if expect else (abs(got) <= 1.0 and not r["clear"])
        print("selftest patch offset %+.0f (surround +5): net step %+.2f (expect %+.1f), clear=%s, edge %.2f pitches -> %s" % (off, got, expect, r.get("clear"), r.get("edge", 0), "ok" if good else "FAILED"))
        ok = ok and good
    print("selftest:", "OK" if ok else "FAILED")
    return 0 if ok else 1


def main(argv):
    if "--selftest" in argv:
        return selftest()
    paths = [a for a in argv if not a.startswith("--")]
    if not paths:
        print(__doc__)
        return 2
    for p in paths:
        a = np.asarray(ImageOps.exif_transpose(Image.open(p)).convert("RGB")).astype(float)
        report(os.path.basename(p), measure(a))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
