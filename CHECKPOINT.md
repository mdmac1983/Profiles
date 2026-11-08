# CHECKPOINT 716 — read me first, then TASKS.md

**Written:** 2026-11-08T19:34:53Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `7244349` (this checkpoint is the commit after it)

## Just done
v0.8.11 release prep (no product change): versionCode 36 / 0.8.11 in app/build.gradle.kts, research/BK1_LOCK_FIRST_PRESS_PENCIL_UNLOCK_2026-11-08.md, research/TABLET_CHECKLIST_v0.8.11.md, TASKS BK1d/BK1e added. Tier 0 local: GRADLE_EXIT=0 (unit tests + lint). Gate runs #397 id 42514271314 job 128833767456, #398 id 42514273811, #399 id 42514276324 still running, expected done ~19:43 UTC

## Do this next
when the runs end: list_workflow_jobs (run id) -> get_job_logs tail 60-112; all FAIL lines judged (compare with tag v0.8.10 before blaming the diff); if green: bash ship.sh 'note', android.yml ref claude/new-session-2ax1r4 full_build=true, get_release_by_tag v0.8.11, bash tools/record-release.sh v0.8.11 'note', tick BK1a/b/c, final answer to Tj with the BK1e questions

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  d35a776 ckpt 715: takeover (old session died 'Prompt is too long'); branch == main == c4f47b5; p
  c4f47b5 ckpt 714: BK1 v0.8.11 candidate code complete: warm-up draw (per look kind), pencil via 
  894d06e ckpt 713: BK1c: Recents loop (no helper) fetches the window roots ONCE per look instead 
  203a5b1 ckpt 712: BK1: review fixes in (UNLOCKSUM emitted when the cover goes + Launcher drawn/r
  d9732b2 ckpt 711: BK1: run #392 (b6,fp on 1ffdfa2): FP warm-up line present, warm lock cycles fi
  1ffdfa2 ckpt 710: BK1: runs #390 (b6,fp on e415cd0) and #391 (cs baseline) read. FP: warm-up wor
  0770182 ckpt 709: BK1c: single-figure unlock summary built (UNLOCKSUM lock-log line + report key
  e415cd0 ckpt 708: BK1a/BK1b fixes built: (a) warm-up draw of the OPEN look once per patch window
  4c2e2e2 ckpt 707: BK1: measurement built, no product change yet: NEW section fp (films the FIRST
  efee840 ckpt 706: BK1 written into TASKS in Tj's words (18:16 UTC): first lock-screen power-menu
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
