# CHECKPOINT 621 — read me first, then TASKS.md

**Written:** 2026-11-07T18:19:25Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-823c5a56-yg8voe` · **builds on:** `c1ddc45` (this checkpoint is the commit after it)

## Just done
confirmation {base,x1,e1} #306 id 42399859690 = success: the #303 e1 failure was a flake; ALL gates green for v0.8.4; android.yml full_build dispatched on main at 18:19 UTC (head c6cb66b, app code = cc1f17f)

## Do this next
find the android.yml run (list_workflow_runs android.yml branch main event workflow_dispatch perPage 1), wait ~4 min (background timer, then list_workflow_jobs), get_release_by_tag v0.8.4 (Launcher-v0.8.4.apk), bash tools/record-release.sh v0.8.4 '<note>', HANDOFF 0g SHIPPED line, tell Tj (link on its own line)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  c6cb66b ckpt 620: HANDOFF 0g refreshed for v0.8.4 (contents, evidence pointers, test lessons, wh
  1231dd8 ckpt 619: core on the final code: #304 {n,o,rb} green; #303 {base,x1,e1,ac} 2 FAIL in e1
  a1c477e ckpt 618: v0.8.4 gated and pushed (code 29); #302 id 42397572681 (v alone) green after t
  43cfe53 ckpt 617: gated v0.8.4 (code 29) and pushed it: the handoff self-test, the full unit sui
  7f3b5d6 ckpt 616: final-head gate read on d530023: #300 (pt,last) green; #301 (ae,ps,pa,x1,v) 43
  253981c ckpt 615: final-head runs dispatched 17:22 UTC on d530023: #300 id 42396001116 (pt,last)
  d530023 ckpt 614: gate read: #297 and #298 green on a8f76b6, #299 (pt,last) 3 FAIL = the noisy o
  986ddfa ckpt 613: Tj corrected BC5 (17:17 UTC): the PIN-pad hide must work with NO power menu op
  4a16127 ckpt 612: BD1/BD2 leads written to TASKS (ReturnReset black cover, TaskOrigin/RMTASKS by
  6bf1bcb ckpt 611: Tj's new message (17:12 UTC): BD1 hidden apps (Telegram, Claude) were in the R
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
