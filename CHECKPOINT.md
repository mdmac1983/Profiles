# CHECKPOINT 719 — read me first, then TASKS.md

**Written:** 2026-11-08T19:53:55Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `6e0299f` (this checkpoint is the commit after it)

## Just done
TRIAGE DONE. Solo runs on main: uf #400 (UF2 FAIL, same order as TAG v0.8.10 #403 = pre-existing), cs #401 (CS-A3 PASS, CS-B5 FAIL: stock .Launcher resumed behind cover; passed in #396/#398 = race), hp #402 (all PASS = HP4 was a flake of the crashed chain). BK1b quickstep FAIL in #399 is a log-line check (no line when cover already shown): unproven, not disproven

## Do this next
edit tools/emulator_scenario.sh only: UF2 + CS-B5 become cover-aware (stock resumes allowed only while 'RESET cover shown..removed'), BK1b gets a pixel check of the pencil rect + PATCHEV diagnostics; then solo runs uf, cs, b6; ship only if no product change is needed

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  6e0299f ckpt 718: GATE #397 read: 99 PASS 1 FAIL = UF2 (stock HOME .Launcher resumed after the E
  3d61f3e ckpt 717: GATE READ (head c4f47b5), all 3 runs ended RED at the scenario step: #398 id 4
  9963f0b ckpt 716: v0.8.11 release prep (no product change): versionCode 36 / 0.8.11 in app/build
  d35a776 ckpt 715: takeover (old session died 'Prompt is too long'); branch == main == c4f47b5; p
  c4f47b5 ckpt 714: BK1 v0.8.11 candidate code complete: warm-up draw (per look kind), pencil via 
  894d06e ckpt 713: BK1c: Recents loop (no helper) fetches the window roots ONCE per look instead 
  203a5b1 ckpt 712: BK1: review fixes in (UNLOCKSUM emitted when the cover goes + Launcher drawn/r
  d9732b2 ckpt 711: BK1: run #392 (b6,fp on 1ffdfa2): FP warm-up line present, warm lock cycles fi
  1ffdfa2 ckpt 710: BK1: runs #390 (b6,fp on e415cd0) and #391 (cs baseline) read. FP: warm-up wor
  0770182 ckpt 709: BK1c: single-figure unlock summary built (UNLOCKSUM lock-log line + report key
```
