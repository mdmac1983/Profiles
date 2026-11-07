# CHECKPOINT 615 — read me first, then TASKS.md

**Written:** 2026-11-07T17:22:54Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `8b5a540` (this checkpoint is the commit after it)

## Just done
final-head runs dispatched 17:22 UTC on d530023: #300 id 42396001116 (pt,last) and #301 id 42396003579 (ae,ps,pa,x1,v): each prints 'INFO sections:' first

## Do this next
read both at ~17:40 UTC (list_workflow_jobs latest; the pt run: get_job_logs tail 200 (or delegate: a subagent extracts the FAIL lines and the PT/BC2, PT/BC5, PT/BC5b INFO lines), the other tail 70); all green -> bump app/build.gradle.kts to 0.8.4 / code 29, bash ship.sh, android.yml full_build on main, get_release_by_tag v0.8.4, tools/record-release.sh v0.8.4, HANDOFF 0g, tell Tj (release link on its own line; ask how the pull-up-for-PIN and the tap close look; BC4 screenshots + BD1/BD2 wait for a fresh session)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  d530023 ckpt 614: gate read: #297 and #298 green on a8f76b6, #299 (pt,last) 3 FAIL = the noisy o
  986ddfa ckpt 613: Tj corrected BC5 (17:17 UTC): the PIN-pad hide must work with NO power menu op
  4a16127 ckpt 612: BD1/BD2 leads written to TASKS (ReturnReset black cover, TaskOrigin/RMTASKS by
  6bf1bcb ckpt 611: Tj's new message (17:12 UTC): BD1 hidden apps (Telegram, Claude) were in the R
  9c8439b ckpt 610: BC5 coded: TjAccessibilityService pinPadCameUp (patch alpha 0 at once + untouc
  35fa162 ckpt 609: Tj's answer (17:15 UTC): for the pull up for the PIN the patch must disappear 
  6ec98fc ckpt 608: Tj's new message (17:05 UTC): 'Also the lock screen still takes screenshots so
  b8f4fb9 ckpt 607: gate dispatched 17:02 UTC on a8f76b6 (the v0.8.4 candidate: BC1 edge fix + BC2
  a8f76b6 ckpt 606: emulator #296 read: the tap close is followed along the stock fade-out (close_
  daf4f15 ckpt 605: emulator #295 read (BC1 edge covered to 0.1 level; BC2: click + dialog-removal
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
