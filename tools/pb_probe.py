#!/usr/bin/env python3
"""pb_probe.py - the faint line under the hidden card: the stock panel's SHADOW against the patch's bottom edge (BB1, emulator section pt phase D).

    python3 -I tools/pb_probe.py <label> <bare.png> <shot.png> --panel L,T,R,B [--stock] [--grid]
    python3 -I tools/pb_probe.py --selftest

WHY (Tj, 2026-11-07, v0.8.2, after the square itself became invisible: "a very slight tinted line about where the bottom of the card would be. Like a faded tint"; TASKS BB1a). The lock-screen patch hides the stock
menu's third slot (Screenshot). The stock panel casts an elevation SHADOW downward from its real (hidden) bottom edge; the patch's opaque core reached only 2 dp past that edge and then faded out, so the shadow showed
under the patch: on Tj's tablet a dark band 6 to 10 levels deep fading over ~15 to 30 px (measured in his photos). This script measures it on the emulator, numerically.

INPUT: two 600 x 1024 screenshots of the SAME lock screen: <bare.png> = the menu closed (the resting patch is invisible), <shot.png> = the stock menu open and settled. With --stock the patch was OFF (the stock panel
alone: its shadow is what is measured); without it the patch was ON (what Tj sees: the line is what is measured). --panel is the learned stock panel (the helper's `CARD ... full=[L,T][R,B]` log line).
METHOD: the stock dialog's scrim is a flat layer, shown = s * before + o per channel: it is FITTED from a clean region far from the menu (left of it), so whatever scrim the emulator has (0.75 or 0.80) does not matter.
expected = s * bare + o everywhere; deviation = shot - expected, averaged over the columns inside the panel (15 px margin), per row, in levels of 255 (luminance and per channel). d = rows below the panel's bottom edge.
OUTPUT: one line  PBMETRIC label=.. kind=stock|patch scale=.. offset=.. shadow_max=L shadow_ext=D line_max=L line_at=D core_mean=L  and a profile line (d:deviation).
  stock: shadow_max = the deepest darkening below the panel, shadow_ext = the farthest d where it is still >= 1.0 level.
  patch: line_max = the largest |deviation| for d in 0..60 (the zone where the line lives), line_at = where, core_mean = the mean deviation inside the slot (d -70..-5; ~0 = the patch matches).
Reads pixels with ImageMagick `convert` (the CI runner has it; Pillow is not guaranteed). Run with python3 -I.
"""
import math
import os
import subprocess
import sys
import tempfile

PROBE_D = (-60, -30, -10, -4, 0, 2, 4, 6, 8, 10, 12, 15, 18, 21, 24, 27, 30, 35, 40, 50, 60)


def region(path, x, y, w, h):
    out = subprocess.run(["convert", path, "-alpha", "off", "-crop", "%dx%d+%d+%d" % (w, h, x, y), "+repage", "-depth", "8", "rgb:-"], capture_output=True, check=True).stdout
    if len(out) != w * h * 3:
        raise ValueError("region %s %dx%d+%d+%d gave %d bytes, wanted %d (image smaller than the crop?)" % (os.path.basename(path), w, h, x, y, len(out), w * h * 3))
    return out


def lumv(r, g, b):
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def fit_scrim(bare, shot, x, y, w, h):
    """per channel (s, o) with shot ~ s * bare + o over a clean region, trimmed least squares (two passes)"""
    A = region(bare, x, y, w, h)
    B = region(shot, x, y, w, h)
    out = []
    for c in range(3):
        xs = A[c::3]
        ys = B[c::3]
        keep = [True] * len(xs)
        s, o = 0.8, 0.0
        for _ in range(3):
            n = sum(keep)
            if n < 200:
                raise ValueError("too few clean pixels to fit the scrim")
            mx = sum(xv for xv, k in zip(xs, keep) if k) / n
            my = sum(yv for yv, k in zip(ys, keep) if k) / n
            vx = sum((xv - mx) ** 2 for xv, k in zip(xs, keep) if k)
            if vx < 1e-6:
                raise ValueError("the clean region has no brightness range (flat): cannot fit the scrim")
            s = sum((xv - mx) * (yv - my) for xv, yv, k in zip(xs, ys, keep) if k) / vx
            o = my - s * mx
            res = [yv - (s * xv + o) for xv, yv in zip(xs, ys)]
            rms = math.sqrt(sum(r * r for r, k in zip(res, keep) if k) / n)
            lim = max(6.0, 3.0 * rms)
            keep = [abs(r) <= lim for r in res]
        out.append((s, o))
    return out


def profile(bare, shot, model, xa, xb, y0, y1):
    """{y: (dev_lum, (dr, dg, db))}: shot - (s * bare + o), mean over the columns xa..xb, per row"""
    w, h = xb - xa, y1 - y0
    A = region(bare, xa, y0, w, h)
    B = region(shot, xa, y0, w, h)
    rows = {}
    for j in range(h):
        sums = [0.0, 0.0, 0.0]
        for i in range(w):
            k = (j * w + i) * 3
            for c in range(3):
                sums[c] += B[k + c] - (model[c][0] * A[k + c] + model[c][1])
        dv = [v / w for v in sums]
        rows[y0 + j] = (lumv(*dv), tuple(dv))
    return rows


def grid(label, bare, shot, model, panel, disp=(600, 1024)):
    """a coarse picture of (shot - expected) in levels: columns from 45 px left of the panel to 45 px right of it (step 15), rows from 30 px above its bottom edge to 90 px below (step 10)"""
    L, T, R, B = panel
    x0, x1, y0, y1 = max(0, L - 45), min(disp[0], R + 46), max(0, B - 30), min(disp[1], B + 91)
    w, h = x1 - x0, y1 - y0
    A, S = region(bare, x0, y0, w, h), region(shot, x0, y0, w, h)
    xs = list(range(0, w, 15))
    lines = ["PBGRID label=%s columns x=%s (values: shot minus expected, levels, luminance; rows y = panel bottom %+d ...)" % (label, ",".join(str(x0 + x) for x in xs), y0 - B)]
    for j in range(0, h, 10):
        row = []
        for i in xs:
            k = (j * w + i) * 3
            d = [S[k + c] - (model[c][0] * A[k + c] + model[c][1]) for c in range(3)]
            row.append("%+4d" % round(lumv(*d)))
        lines.append("PBGRID label=%s y=%d: %s" % (label, y0 + j, " ".join(row)))
    return lines


def analyse(label, bare, shot, panel, stock, disp=(600, 1024), want_grid=False):
    L, T, R, B = panel
    # the clean region: left of the menu, well away from its shadow, below the clock / date text, above the lock number
    cx0, cx1 = 60, max(120, min(L - 90, 400))
    cy0, cy1 = max(T + 60, 470), min(B + 120, 800)
    model = fit_scrim(bare, shot, cx0, cy0, cx1 - cx0, cy1 - cy0)
    xa, xb = L + 15, R - 15
    y0, y1 = max(0, B - 75), min(disp[1], B + 185)   # the emulator's menu has a second white card under the panel: reach far enough to see its bottom edge and the shadow below it
    prof = profile(bare, shot, model, xa, xb, y0, y1)
    d_of = lambda y: y - B
    out = {"scale": tuple(m[0] for m in model), "offset": tuple(m[1] for m in model), "profile": {d_of(y): v for y, v in prof.items()}}
    if stock:
        pr = out["profile"]
        # the stock panel's shadow, measured where it is clean: the emulator's menu has ANOTHER white card (a 4th entry) 4 px below the panel, so the shadow tail under the panel is hidden by it. The shadow below the LAST
        # white block has the same elevation. edge = the last row (d) of that block; j = rows below it (j = 0 is the first row under the edge, like d = 0 under the panel).
        edge = None
        for d in sorted((d for d, v in pr.items() if v[0] >= 40.0), reverse=True):
            if all(pr.get(d + k, (0.0,))[0] < 40.0 for k in (1, 2, 3)) and (d + 70) in pr:
                edge = d
                break
        if edge is None:
            edge = -1
        out["edge_d"] = edge
        sh = {j: -pr[edge + 1 + j][0] for j in range(0, 70) if (edge + 1 + j) in pr}      # positive = darker than the dimmed wallpaper
        out["shadow_prof"] = sh
        out["shadow_max"] = max(sh.values()) if sh else 0.0
        out["shadow_ext"] = max([j for j, v in sh.items() if v >= 1.0] or [0])
        out["shadow_near"] = max([-pr[d][0] for d in (0, 1, 2, 3) if d in pr] or [0.0])    # the first card's own shadow right under the panel (before the second card hides the rest)
    else:
        ln = {d: v[0] for d, v in out["profile"].items() if 0 <= d <= 60}
        at = max(ln, key=lambda d: abs(ln[d])) if ln else 0
        core = [v[0] for d, v in out["profile"].items() if -70 <= d <= -5]
        out["line_max"] = abs(ln[at]) if ln else 0.0
        out["line_at"] = at
        out["line_sign"] = (ln[at] > 0) - (ln[at] < 0) if ln else 0
        out["core_mean"] = sum(core) / len(core) if core else 0.0
    out["label"] = label
    out["kind"] = "stock" if stock else "patch"
    out["grid"] = grid(label, bare, shot, model, panel, disp) if want_grid else []
    return out


def _ramp(dist, width):
    """PatchRest.ramp: the smoothstep of the fringe"""
    if width <= 0:
        return 0.0
    t = 1.0 - dist / (width + 1.0)
    return 0.0 if t <= 0.0 else t * t * (3.0 - 2.0 * t)


def predict(shadow, extra, bfeather, edge=2):
    """the line a geometry would leave on a screen whose menu has NO second card: residual(j) = (1 - alpha) * shadow(j), j rows below the panel, alpha = PatchRest.alphaAt below the core (the core ends edge + extra rows below the panel)"""
    cb = edge + extra
    worst, at = 0.0, 0
    for j in sorted(shadow):
        a = 1.0 if j < cb else _ramp(j - cb + 1, bfeather)
        r = (1.0 - a) * shadow[j]
        if abs(r) > abs(worst):
            worst, at = r, j
    return worst, at


SIM_GEOMETRIES = ((0, 9), (10, 9), (21, 9), (21, 4), (26, 4), (30, 0))


def report(o):
    keys = ("shadow_max", "shadow_ext") if o["kind"] == "stock" else ("line_max", "line_at", "core_mean")
    vals = " ".join(("%s=%d" % (k, o[k])) if k.endswith(("_ext", "_at")) else ("%s=%+.2f" % (k, o[k]) if k == "core_mean" else "%s=%.2f" % (k, o[k])) for k in keys)
    print("PBMETRIC label=%s kind=%s scale=%.3f/%.3f/%.3f offset=%.1f/%.1f/%.1f %s" % (o["label"], o["kind"], *o["scale"], *o["offset"], vals))
    print("PBPROFILE label=%s (d = px below the panel's bottom edge : deviation in levels, luminance; negative = darker than the dimmed wallpaper) %s" % (
        o["label"], " ".join("%d:%+.1f" % (d, o["profile"][d][0]) for d in PROBE_D if d in o["profile"])))
    if o["kind"] == "stock":
        print("PBSHADOW label=%s edge_d=%d near_panel=%.1f (the first card's shadow 0-3 px under the panel); shadow below the last white block, j px below its edge (levels darker): %s" % (
            o["label"], o["edge_d"], o["shadow_near"], " ".join("%d:%.1f" % (j, o["shadow_prof"][j]) for j in (0, 1, 2, 3, 4, 6, 8, 10, 12, 15, 18, 21, 24, 27, 30, 35, 40, 50, 60) if j in o["shadow_prof"])))
        print("PBSIM label=%s predicted line (levels) a geometry would leave on a menu with NO second card, from that shadow: %s" % (
            o["label"], "  ".join("extra%d/fringe%d=%.1f@%d" % (x, f, abs(predict(o["shadow_prof"], x, f)[0]), predict(o["shadow_prof"], x, f)[1]) for x, f in SIM_GEOMETRIES)))
    for ln in o.get("grid", []):
        print(ln)


# ------------------------------------------------------------------------------------------------------------------------------------------------
def _ppm(path, w, h, px):
    with open(path, "wb") as f:
        f.write(b"P6\n%d %d\n255\n" % (w, h))
        f.write(bytes(px))


def selftest():
    """a synthetic lock screen: smooth wallpaper, a scrim 0.78 * x + 6, a white panel with a downward shadow; the patch: old geometry (core 2 px, fringe 9 px) vs the extended one (core 23 px, fringe 9 px)"""
    W, H = 600, 1024
    L, T, R, B = 485, 339, 596, 642
    tmp = tempfile.mkdtemp(prefix="pbprobe")
    wall = lambda x, y: (40 + 0.15 * x + 20 * math.sin(y / 40.0), 70 + 0.12 * y / 4 + 10 * math.sin(x / 33.0), 140 + 0.04 * x + 0.02 * y)
    S, O = 0.78, 6.0

    def shadow(d):          # fractional darkening below the panel: 12 % at the edge, decaying
        return 0.12 * math.exp(-max(0, d) / 16.0) if d >= 0 else 0.0   # Tj's photos: still ~10 % of the level at 19 px below the panel

    def ramp(dist, width):  # the patch's smoothstep fringe
        t = 1 - dist / (width + 1.0)
        return 0.0 if t <= 0 else t * t * (3 - 2 * t)

    def make(kind, core_bottom):
        px = bytearray(W * H * 3)
        for y in range(H):
            for x in range(W):
                base = wall(x, y)
                if kind == "bare":
                    v = base
                else:
                    v = [S * c + O for c in base]
                    inx = L <= x < R
                    if inx and T <= y <= B:
                        v = [240.0, 242.0, 244.0]                 # the stock panel
                    elif kind in ("stock", "old", "new") and L - 12 <= x < R + 12 and y > B:
                        k = 1.0 - shadow(y - B) * (1.0 if inx else math.exp(-((L - x) if x < L else (x - R + 1)) / 5.0))
                        v = [c * k for c in v]                    # the shadow, a little narrower sideways
                    if kind in ("old", "new") and L - 11 <= x < R + 11 and B - 95 <= y:
                        a = ramp(y - core_bottom + 1, 9) if y >= core_bottom else 1.0     # as PatchRest.alphaAt: dist = y - cb + 1 below the core
                        a *= ramp(L - 2 - x, 9) if x < L - 2 else (ramp(x - (R + 2) + 1, 9) if x >= R + 2 else 1.0)
                        patchv = [S * c + O for c in base]        # an exact patch: the dimmed wallpaper
                        v = [a * p + (1 - a) * q for p, q in zip(patchv, v)]
                i = (y * W + x) * 3
                px[i] = max(0, min(255, int(round(v[0])))); px[i + 1] = max(0, min(255, int(round(v[1])))); px[i + 2] = max(0, min(255, int(round(v[2]))))
        return px

    paths = {}
    for kind, cb in (("bare", 0), ("stock", 0), ("old", B + 2), ("new", B + 2 + 21)):
        paths[kind] = os.path.join(tmp, kind + ".ppm")
        _ppm(paths[kind], W, H, make(kind, cb))
    st = analyse("selftest", paths["bare"], paths["stock"], (L, T, R, B), True)
    old = analyse("old", paths["bare"], paths["old"], (L, T, R, B), False)
    new = analyse("new", paths["bare"], paths["new"], (L, T, R, B), False)
    for o in (st, old, new):
        report(o)
    ok = abs(st["scale"][1] - S) < 0.01 and abs(st["offset"][1] - O) < 1.5
    ok = ok and st["shadow_max"] > 6.0 and st["shadow_ext"] >= 15
    ok = ok and old["line_max"] > 0.3 * st["shadow_max"] and abs(old["core_mean"]) < 1.0
    ok = ok and new["line_max"] < 0.3 * old["line_max"] and new["line_max"] < 2.0 and abs(new["core_mean"]) < 1.0
    print("selftest: shadow %.1f levels to %d px; old geometry line %.1f at %d; extended line %.1f; core %+.2f / %+.2f -> %s" % (
        st["shadow_max"], st["shadow_ext"], old["line_max"], old["line_at"], new["line_max"], old["core_mean"], new["core_mean"], "OK" if ok else "FAILED"))
    return 0 if ok else 1


def main(argv):
    if "--selftest" in argv:
        return selftest()
    args = [a for a in argv if not a.startswith("--")]
    opt = {}
    for i, a in enumerate(argv):
        if a == "--panel" and i + 1 < len(argv):
            opt["panel"] = [int(v) for v in argv[i + 1].split(",")]
    if len(args) < 3 or "panel" not in opt or len(opt["panel"]) != 4:
        print(__doc__)
        return 2
    label, bare, shot = args[0], args[1], args[2]
    try:
        report(analyse(label, bare, shot, tuple(opt["panel"]), "--stock" in argv, want_grid="--grid" in argv))
    except Exception as e:   # never take the scenario down: say what went wrong
        print("PBMETRIC label=%s error=%s" % (label, str(e).replace(" ", "_")[:120]))
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
