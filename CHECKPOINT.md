# CHECKPOINT 722 — read me first, then TASKS.md

**Written:** 2026-11-08T20:29:51Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `198fa0e` (this checkpoint is the commit after it)

## Just done
b6 samples #408 id 42519172669 / #409 id 42519175423 (head 1986107): quickstep phase had a cover shown BEFORE any panel opened in both (stray?), later openings 'none' in one phase each (uiautomator dump unbinds a11y services = measurement artifact), control ok in one. Rewrote b6lock: no dump inside sampling, sample = cover state + keyguard showing + StatusBar frame, lock-log lines; offline tests tools/test_pen_sample.sh + test_cover_resumes.sh. Dispatched 2 samples ~20:34 UTC on e6d9f50 (main + branch ref)

## Do this next
read both b6 runs (~20:40): look at 'NO panel open, unlocked / PIN set / lock screen' samples: cover=shown with sb=[0,0][600,24] and showing=false = a STRAY cover over nothing = product bug (fix in schedulePencil/findPencil, regate b6 + pt/fp); if clean: bash ship.sh, android.yml full_build on claude/new-session-2ax1r4, record-release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  f945d11 ckpt 721: b6 #407 id 42518553791: BK1b lock phases cover shown at 3 openings in BOTH sto
  f8e6d37 ckpt 720: scenario-only edits on main 540e4e5: cover_resumes helper (UF2 + CS-B5 cover-a
  6e87f60 ckpt 719: TRIAGE DONE. Solo runs on main: uf #400 (UF2 FAIL, same order as TAG v0.8.10 #
  6e0299f ckpt 718: GATE #397 read: 99 PASS 1 FAIL = UF2 (stock HOME .Launcher resumed after the E
  3d61f3e ckpt 717: GATE READ (head c4f47b5), all 3 runs ended RED at the scenario step: #398 id 4
  9963f0b ckpt 716: v0.8.11 release prep (no product change): versionCode 36 / 0.8.11 in app/build
  d35a776 ckpt 715: takeover (old session died 'Prompt is too long'); branch == main == c4f47b5; p
  c4f47b5 ckpt 714: BK1 v0.8.11 candidate code complete: warm-up draw (per look kind), pencil via 
  894d06e ckpt 713: BK1c: Recents loop (no helper) fetches the window roots ONCE per look instead 
  203a5b1 ckpt 712: BK1: review fixes in (UNLOCKSUM emitted when the cover goes + Launcher drawn/r
```

(8 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
