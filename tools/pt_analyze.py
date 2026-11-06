#!/usr/bin/env python3
"""pt_analyze.py - how well does the lock-screen wallpaper patch follow the stock power menu's fade? (AZ1, emulator section pt)

The emulator films the lock screen while the stock power menu opens and closes (`screenrecord`, an mp4). Two small boxes are cut out of every frame:
  P = inside the patch (the hidden Screenshot slot: the PATCH is what shows there),
  S = beside the menu (plain lock wallpaper that only the stock dialog's own scrim darkens).
While the patch is invisible the ratio P/S stays constant (the same wallpaper, both darkened the same). Any step in that ratio is a block that
looks darker or lighter than its surround = the flicker Tj saw. The metric is the ratio relative to its value before the menu opened:
    dev(t) = (P(t) / S(t)) / r0 - 1        (0 = perfect, -0.10 = the patch is 10 % darker than its surround at that moment)

  python3 -I tools/pt_analyze.py <video.mp4> <label> --disp WxH --p X,Y --s X,Y [--box 20] [--fps 60]
  python3 -I tools/pt_analyze.py --selftest

Prints one line:  PTMETRIC label=... frames=N onset=0.84s r0=1.02 open_max=0.031 open_final=-0.004 open_at=0.11s close_max=... close_final=... rest_noise=0.002
Needs ffmpeg (the workflow installs it for section pt). Standard library only.
"""
import re
import statistics
import subprocess
import sys


def means(video, box, fps, vsize):
    """Mean grey of a box per frame (resampled to a constant fps). box = (x, y, w, h) in VIDEO pixels."""
    x, y, w, h = box
    cmd = ["ffmpeg", "-v", "error", "-i", video, "-vf", "fps=%d,crop=%d:%d:%d:%d,format=gray" % (fps, w, h, x, y), "-f", "rawvideo", "-"]
    data = subprocess.run(cmd, capture_output=True).stdout
    n = w * h
    return [sum(data[i:i + n]) / n for i in range(0, len(data) - n + 1, n)]


def video_size(video):
    p = subprocess.run(["ffmpeg", "-i", video], capture_output=True, text=True)
    m = re.search(r"Video:.*?, (\d{2,5})x(\d{2,5})", p.stderr)
    return (int(m.group(1)), int(m.group(2))) if m else None


def analyse(P, S, fps, thr=0.006):
    """P, S: per-frame mean brightness of the patch box and the surround box. Returns a dict of metrics (None when the menu never showed)."""
    n = min(len(P), len(S))
    if n < fps:
        return {"error": "too few frames (%d)" % n}
    P, S = P[:n], S[:n]
    base = statistics.median(S[: max(3, int(0.4 * fps))])
    if base <= 1:
        return {"error": "surround box is black"}
    onset = None
    for i in range(int(0.3 * fps), n - 4):
        if all(S[i + k] < base * (1 - thr) for k in range(4)):   # four frames in a row darker: the scrim has started
            onset = i
            break
    if onset is None:
        return {"error": "no darkening seen (the menu did not open?)"}
    pre = [P[j] / S[j] for j in range(max(0, onset - int(0.35 * fps)), max(1, onset - 2)) if S[j] > 1]
    if len(pre) < 3:
        return {"error": "no resting frames before the onset"}
    r0 = statistics.mean(pre)
    noise = max(abs(v / r0 - 1) for v in pre)
    dev = lambda j: (P[j] / S[j]) / r0 - 1 if S[j] > 1 else 0.0
    # opening: from just before the onset to 0.55 s after it; "final" = the last 0.12 s of that stretch
    o_end = min(n, onset + int(0.55 * fps))
    open_devs = [(j, dev(j)) for j in range(max(0, onset - 2), o_end)]
    j_max, d_max = max(open_devs, key=lambda t: abs(t[1]))
    tail = [d for (j, d) in open_devs if j >= o_end - int(0.12 * fps)]
    out = {"onset": onset / fps, "r0": r0, "rest_noise": noise, "open_max": abs(d_max), "open_at": (j_max - onset) / fps,
           "open_final": statistics.mean(tail) if tail else 0.0}
    # closing: after the surround reached its darkest point, the first time it brightens again by thr
    lo = min(range(o_end, n), key=lambda j: S[j]) if o_end < n else None
    if lo is not None:
        c_on = next((j for j in range(lo, n - 2) if all(S[j + k] > S[lo] * (1 + thr) for k in range(3))), None)
        if c_on is not None:
            c_end = min(n, c_on + int(0.55 * fps))
            cd = [(j, dev(j)) for j in range(max(0, c_on - 2), c_end)]
            jm, dm = max(cd, key=lambda t: abs(t[1]))
            ctail = [d for (j, d) in cd if j >= c_end - int(0.12 * fps)]
            out.update({"close_max": abs(dm), "close_at": (jm - c_on) / fps, "close_final": statistics.mean(ctail) if ctail else 0.0})
    out["frames"] = n
    return out


def series(P, S, fps, onset, r0, span_ms=(-100, 420), step=1):
    """Compact curves around the onset, for the log: t (ms from the onset), S/S0 and P/P0 in per-mille, dev in tenths of a percent."""
    n = min(len(P), len(S))
    s0 = statistics.median(S[: max(3, int(0.4 * fps))])
    p0 = statistics.median(P[: max(3, int(0.4 * fps))])
    out = []
    for j in range(max(0, onset + int(span_ms[0] * fps / 1000)), min(n, onset + int(span_ms[1] * fps / 1000)), step):
        t = int((j - onset) * 1000 / fps)
        dev = ((P[j] / S[j]) / r0 - 1) if S[j] > 1 else 0.0
        out.append("%d:%d/%d/%+d" % (t, round(1000 * S[j] / s0), round(1000 * P[j] / p0), round(1000 * dev)))
    return " ".join(out)


def selftest():
    fps = 60
    # surround: 1.0 resting, then the stock scrim (FAST_OUT_SLOW_IN 300 ms, 20 %) from t=0.8 s, dismissed from t=2.0 s (slow-start log curve, 300 ms)
    def fosi(x):
        x = min(1.0, max(0.0, x)); lo, hi = 0.0, 1.0
        for _ in range(30):
            s = (lo + hi) / 2; u = 1 - s
            if 3 * u * u * s * 0.4 + 3 * u * s * s * 0.2 + s ** 3 < x: lo = s
            else: hi = s
        return 3 * (1 - s) * s * s + s ** 3
    def logacc(t):
        t = min(1.0, max(0.0, t)); c = lambda v: -(100 ** (-v)) + 1
        return 1 - c(1 - t) * (1 / c(1.0))
    def surround(t):
        a = 0.0
        if t >= 0.8: a = 0.2 * fosi((t - 0.8) / 0.3)
        if t >= 2.0: a = 0.2 * (1 - logacc((t - 2.0) / 0.3))
        return 100.0 * (1 - a)
    ts = [i / fps for i in range(int(3.2 * fps))]
    S = [surround(t) for t in ts]
    # a perfect patch: same curve, constant ratio 1.3
    P_ok = [1.3 * s for s in S]
    m = analyse(P_ok, S, fps)
    assert m["open_max"] < 0.002 and m["close_max"] < 0.002, m
    # the v0.8.0 patch: jumps to 0.80 of its brightness 80 ms after the scrim began, and back to 1.0 only when the window is gone (t = 2.35 s)
    P_jump = [1.3 * 100.0 * (0.8 if 0.88 <= t < 2.35 else 1.0) for t in ts]
    j = analyse(P_jump, S, fps)
    assert j["open_max"] > 0.10 and j["close_max"] > 0.05, j
    assert j["open_max"] > 10 * m["open_max"] + 0.05
    # a patch that follows the curve but 60 ms late is better than the jump and worse than the perfect one
    P_late = [1.3 * surround(t - 0.06) for t in ts]
    l = analyse(P_late, S, fps)
    assert m["open_max"] < l["open_max"] < j["open_max"], (m, l, j)
    # a filmed run in which the menu never opened
    flat = analyse([130.0] * 200, [100.0] * 200, fps)
    assert "error" in flat
    print("pt_analyze selftest OK: perfect %.4f / late %.4f / jump %.4f (open_max)" % (m["open_max"], l["open_max"], j["open_max"]))
    return 0


def main(argv):
    if "--selftest" in argv:
        return selftest()
    if len(argv) < 2:
        print(__doc__)
        return 2
    video, label = argv[0], argv[1]
    opt = {"--disp": "600x1024", "--p": None, "--s": None, "--box": "40", "--fps": "60"}
    i = 2
    while i + 1 < len(argv):
        if argv[i] in opt:
            opt[argv[i]] = argv[i + 1]
        i += 2
    if not opt["--p"] or not opt["--s"]:
        print("PTMETRIC label=%s error=missing --p/--s" % label)
        return 1
    dw, dh = (int(v) for v in opt["--disp"].split("x"))
    vs = video_size(video)
    if not vs:
        print("PTMETRIC label=%s error=cannot read the video" % label)
        return 1
    sx, sy = vs[0] / dw, vs[1] / dh
    box, fps = int(opt["--box"]), int(opt["--fps"])
    def cut(xy):
        x, y = (int(v) for v in xy.split(","))
        w, h = max(4, int(box * sx)), max(4, int(box * sy))
        return (max(0, int(x * sx - w / 2)), max(0, int(y * sy - h / 2)), w, h)
    P = means(video, cut(opt["--p"]), fps, vs)
    S = means(video, cut(opt["--s"]), fps, vs)
    m = analyse(P, S, fps)
    if "error" in m:
        print("PTMETRIC label=%s frames=%d error=%s" % (label, min(len(P), len(S)), m["error"]))
        return 1
    on_idx = int(round(m["onset"] * fps))
    print("PTSERIES label=%s (t_ms:S/P/dev in per-mille from the onset) %s" % (label, series(P, S, fps, on_idx, m["r0"])))
    print("PTMETRIC label=%s frames=%d onset=%.2fs r0=%.3f rest_noise=%.4f open_max=%.4f open_at=%.2fs open_final=%+.4f close_max=%.4f close_at=%.2fs close_final=%+.4f" % (
        label, m["frames"], m["onset"], m["r0"], m["rest_noise"], m["open_max"], m["open_at"], m["open_final"], m.get("close_max", -1), m.get("close_at", -1), m.get("close_final", 0.0)))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
