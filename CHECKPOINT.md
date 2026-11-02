# CHECKPOINT 5 — read me first, then TASKS.md

**Written:** 2026-11-02T00:54:52Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-4ff66321-chzhhg` · **builds on:** `cf95d02` (this checkpoint is the commit after it)

## Just done
Registered android.yml with GitHub via a one-off path-filtered push trigger (now removed). CI already green on a real runner (dependabot PR run).

## Do this next
Dispatch android.yml on main full_build=true, read result, confirm preview APK artifact; add note to BRIEF/CLAUDE that a new tags+dispatch-only workflow needs this one-off registration; then ask Tj the OPEN decisions.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M .github/workflows/android.yml
     M CHECKPOINT.md

## Last ten checkpoints
```
  86b7b54 ckpt 3: Fixed dependabot ignores (AGP/Kotlin/Gradle stay pinned); turned MIRROR_MAIN on 
  f80b634 ckpt 1: Launcher foundation: ladder (inbox/autosave/ckpt/ship), cold-resume briefing, sc
```
