# CHECKPOINT 718 — read me first, then TASKS.md

**Written:** 2026-11-08T19:46:59Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `3d61f3e` (this checkpoint is the commit after it)

## Just done
GATE #397 read: 99 PASS 1 FAIL = UF2 (stock HOME .Launcher resumed after the Enter: order 'CUSTOM CUSTOM STOCK:.Launcher CUSTOM', span 2602 ms; check bans com.android.launcher3/.Launcher and contacts after the Enter). Triage runs dispatched ~19:50 UTC, SOLO: main uf, main cs, main hp, v0.8.10 uf (baseline). Hypotheses: UF2 = faster Recents/shade loop (BK1c shared roots) lets the stock HOME resume behind the black cover (real timing shift, invisible but check is strict); HP4 = emulator double-dialog scrim stack (fitted scale 0.75 vs patch 0.80, known artefact) or crash-related; CS-A3 = chain flake (cs solo passed in #396 on 894d06e)

## Do this next
find the 4 run ids (list_workflow_runs emulator.yml main workflow_dispatch perPage 4 + the v0.8.10 one), read each RESULTS (tail_lines 60-70 for a one-section run; they print ~65 lines), decide per FAIL: regression (fix product) / flaky check (fix the check, say so) ; ship only when every FAIL is explained

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  3d61f3e ckpt 717: GATE READ (head c4f47b5), all 3 runs ended RED at the scenario step: #398 id 4
  9963f0b ckpt 716: v0.8.11 release prep (no product change): versionCode 36 / 0.8.11 in app/build
  d35a776 ckpt 715: takeover (old session died 'Prompt is too long'); branch == main == c4f47b5; p
  c4f47b5 ckpt 714: BK1 v0.8.11 candidate code complete: warm-up draw (per look kind), pencil via 
  894d06e ckpt 713: BK1c: Recents loop (no helper) fetches the window roots ONCE per look instead 
  203a5b1 ckpt 712: BK1: review fixes in (UNLOCKSUM emitted when the cover goes + Launcher drawn/r
  d9732b2 ckpt 711: BK1: run #392 (b6,fp on 1ffdfa2): FP warm-up line present, warm lock cycles fi
  1ffdfa2 ckpt 710: BK1: runs #390 (b6,fp on e415cd0) and #391 (cs baseline) read. FP: warm-up wor
  0770182 ckpt 709: BK1c: single-figure unlock summary built (UNLOCKSUM lock-log line + report key
  e415cd0 ckpt 708: BK1a/BK1b fixes built: (a) warm-up draw of the OPEN look once per patch window
```
