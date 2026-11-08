# CHECKPOINT 720 — read me first, then TASKS.md

**Written:** 2026-11-08T19:57:02Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `540e4e5` (this checkpoint is the commit after it)

## Just done
scenario-only edits on main 540e4e5: cover_resumes helper (UF2 + CS-B5 cover-aware), pencil_min pixel check + unlocked-Quickstep control for BK1b. Dispatched solo on main ~19:58 UTC: uf, cs, b6 (ids via list_workflow_runs in_progress). Product code unchanged since c4f47b5

## Do this next
read the 3 runs (~20:04): uf INFO 'UF2 (gate triage)' shows cover life vs stock resumes; cs 'CS-B5 (gate triage)'; b6 'BK1b ... darkest pixel' custom/quickstep + control. If BK1b quickstep pixel < 0.98 = real product bug: fix the pencil follow-up after the lock-screen quiet wait, then rerun b6 + pt/fp/hf

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  6e87f60 ckpt 719: TRIAGE DONE. Solo runs on main: uf #400 (UF2 FAIL, same order as TAG v0.8.10 #
  6e0299f ckpt 718: GATE #397 read: 99 PASS 1 FAIL = UF2 (stock HOME .Launcher resumed after the E
  3d61f3e ckpt 717: GATE READ (head c4f47b5), all 3 runs ended RED at the scenario step: #398 id 4
  9963f0b ckpt 716: v0.8.11 release prep (no product change): versionCode 36 / 0.8.11 in app/build
  d35a776 ckpt 715: takeover (old session died 'Prompt is too long'); branch == main == c4f47b5; p
  c4f47b5 ckpt 714: BK1 v0.8.11 candidate code complete: warm-up draw (per look kind), pencil via 
  894d06e ckpt 713: BK1c: Recents loop (no helper) fetches the window roots ONCE per look instead 
  203a5b1 ckpt 712: BK1: review fixes in (UNLOCKSUM emitted when the cover goes + Launcher drawn/r
  d9732b2 ckpt 711: BK1: run #392 (b6,fp on 1ffdfa2): FP warm-up line present, warm lock cycles fi
  1ffdfa2 ckpt 710: BK1: runs #390 (b6,fp on e415cd0) and #391 (cs baseline) read. FP: warm-up wor
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
