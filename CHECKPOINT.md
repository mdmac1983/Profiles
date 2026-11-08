# CHECKPOINT 721 — read me first, then TASKS.md

**Written:** 2026-11-08T20:15:59Z · **tests:** all 4 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `1986107` (this checkpoint is the commit after it)

## Just done
b6 #407 id 42518553791: BK1b lock phases cover shown at 3 openings in BOTH stored modes; control invalid (plain unlock flips stored mode to custom) -> fixed (setmode quickstep first); added stray-cover samples BK_P1/BK_P2 (cover window before the panel opens). 2 samples dispatched ~20:17 UTC (main + branch ref) on 1986107

## Do this next
read both b6 runs (~20:23): need BK1b all PASS incl. 'NO cover before the panel is opened'; if BK_P2 = shown in quickstep = stray white square on the lock screen = product bug (fix, regate b6+pt/fp); if clean: tick TASKS, bash ship.sh 'note', android.yml full_build on claude/new-session-2ax1r4, record-release

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  f8e6d37 ckpt 720: scenario-only edits on main 540e4e5: cover_resumes helper (UF2 + CS-B5 cover-a
  6e87f60 ckpt 719: TRIAGE DONE. Solo runs on main: uf #400 (UF2 FAIL, same order as TAG v0.8.10 #
  6e0299f ckpt 718: GATE #397 read: 99 PASS 1 FAIL = UF2 (stock HOME .Launcher resumed after the E
  3d61f3e ckpt 717: GATE READ (head c4f47b5), all 3 runs ended RED at the scenario step: #398 id 4
  9963f0b ckpt 716: v0.8.11 release prep (no product change): versionCode 36 / 0.8.11 in app/build
  d35a776 ckpt 715: takeover (old session died 'Prompt is too long'); branch == main == c4f47b5; p
  c4f47b5 ckpt 714: BK1 v0.8.11 candidate code complete: warm-up draw (per look kind), pencil via 
  894d06e ckpt 713: BK1c: Recents loop (no helper) fetches the window roots ONCE per look instead 
  203a5b1 ckpt 712: BK1: review fixes in (UNLOCKSUM emitted when the cover goes + Launcher drawn/r
  d9732b2 ckpt 711: BK1: run #392 (b6,fp on 1ffdfa2): FP warm-up line present, warm lock cycles fi
```

(12 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
