# CHECKPOINT 723 — read me first, then TASKS.md

**Written:** 2026-11-08T20:48:52Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `57f6d1c` (this checkpoint is the commit after it)

## Just done
gated v0.8.11 (code 36) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.8.11 "BK1: the first power-menu press on the lock screen no longer flickers (the open look is drawn once, invisibly, as soon as the lock screen is up); the pencil in the lock screen's notification panel is covered in both launcher modes; the no-helper unlock looks at the screen once per step and every unlock is timed start to finish (Launcher control + tablet report)"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M OUTBOX.md

## Last ten checkpoints
```
  7ae1ed7 ckpt 722: b6 samples #408 id 42519172669 / #409 id 42519175423 (head 1986107): quickstep
  f945d11 ckpt 721: b6 #407 id 42518553791: BK1b lock phases cover shown at 3 openings in BOTH sto
  f8e6d37 ckpt 720: scenario-only edits on main 540e4e5: cover_resumes helper (UF2 + CS-B5 cover-a
  6e87f60 ckpt 719: TRIAGE DONE. Solo runs on main: uf #400 (UF2 FAIL, same order as TAG v0.8.10 #
  6e0299f ckpt 718: GATE #397 read: 99 PASS 1 FAIL = UF2 (stock HOME .Launcher resumed after the E
  3d61f3e ckpt 717: GATE READ (head c4f47b5), all 3 runs ended RED at the scenario step: #398 id 4
  9963f0b ckpt 716: v0.8.11 release prep (no product change): versionCode 36 / 0.8.11 in app/build
  d35a776 ckpt 715: takeover (old session died 'Prompt is too long'); branch == main == c4f47b5; p
  c4f47b5 ckpt 714: BK1 v0.8.11 candidate code complete: warm-up draw (per look kind), pencil via 
  894d06e ckpt 713: BK1c: Recents loop (no helper) fetches the window roots ONCE per look instead 
```

(9 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
