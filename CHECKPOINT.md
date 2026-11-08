# CHECKPOINT 674 — read me first, then TASKS.md

**Written:** 2026-11-08T04:44:46Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-yhyifc` · **builds on:** `fbff289` (this checkpoint is the commit after it)

## Just done
BI1c: emulator section hp written (phases A-E: self-calibration, invisible resting patch MAE, open look via pb_probe on the cardless path, shifted negative control, finger-down + draw, new wallpaper recalibrated, fallbacks) + gate-map + workflow text + SetupActivity test_home_wallpaper hook; handoff 38 ok

## Do this next
dispatch emulator.yml sections=hp alone on ref main, read RESULTS (get_job_logs job tail ~70; INFO lines HP A/B/D carry the diagnostics); expect to iterate on the section/product; then /code-review high, full unit suite + lint, tiered gate, ship v0.8.9 (also carries the BH1e shade fix)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  4b6180c ckpt 673: BI1b step 5: Launcher control rows (switch + status + Calibrate now + Forget) 
  6005aa2 ckpt 672: BI1b steps 2-4 (compiles, nothing tested on the emulator yet): HomeShot + Home
  703bf5a ckpt 671: BI1b step 1: HomePatchRules (pure) + HomePatchRulesTest 19 green
  1a97981 ckpt 670: BI1a design written (research/HOME_PATCH_2026-11-08.md: wallpaper-only calibra
  1ca0e18 ckpt 669: Tj's 04:00 message written as TASKS BI1 (his go for BG2b (1): the lock-screen 
  314ea77 ckpt 668: v0.8.8 RELEASED 03:49 UTC (android.yml run 35 id 42444134135, https://github.c
  d99d60f ckpt 667: v0.8.8 RELEASE build triggered 03:47 UTC: android.yml run 35 id 42444134135 on
  b1399c9 ckpt 666: gated v0.8.8 (code 33) and pushed it: the handoff self-test, the full unit sui
  a3bc971 ckpt 665: v0.8.8 GATE read 03:41 UTC (head 73e65ff, product code final): #342 id 4244234
  0037ea5 ckpt 664: v0.8.8 GATE dispatched 03:21 UTC on 73e65ff (product code final, nq alone #341
```

(10 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
