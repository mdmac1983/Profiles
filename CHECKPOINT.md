# CHECKPOINT 552 — read me first, then TASKS.md

**Written:** 2026-11-06T23:42:23Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-019b8e69-16dn91` · **builds on:** `7d85b50` (this checkpoint is the commit after it)

## Just done
v0.8.1 gated (ship.sh OK, commit 7d85b50 pushed, main = branch tip) and android.yml full_build=true DISPATCHED on ccr-019b8e69-16dn91 (about 23:45 UTC)

## Do this next
find the android.yml run (list_workflow_runs resource_id android.yml filter in_progress), wait (it takes ~10 min), get_job_logs tail 60 must be green; mcp__github__get_release_by_tag v0.8.1; bash tools/record-release.sh v0.8.1 '<the note from ship.sh>' ONLY after green; tick AW6 AZ0 AZ1 AZ2 in TASKS; give Tj the Release link (plain text line) + research/TABLET_CHECKLIST_v0.8.1.md summary

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  7d85b50 ckpt 551: gated v0.8.1 (code 26) and pushed it: the handoff self-test, the full unit sui
  b23199b ckpt 550: TASKS: 21 boxes ticked with proof (tools/tick_v081.py), AZ FINAL + AZ4 written
  2808072 ckpt 549: final gate: #260 {x1,e1,b6,o,bs,lq,ao,rb} PASS, #262 pt PASS, #259 {base,y2,t,
  0919477 ckpt 548: final delta gate dispatched on ckpt 547 (sha 04e6cd1, 23:11 UTC): #259 id 4231
  04e6cd1 ckpt 547: FINAL CODE for v0.8.1 (versionCode 26): v+ps run #258 22 PASS / 1 FAIL (PS C s
  92016c2 ckpt 546: dispatched on ckpt 545 (23:01 UTC): ps #254 id 42308896343, ae #255 id 4230890
  1d45987 ckpt 545: gate round 1 (ckpt 540 code) read: pt #249 9 PASS; ps B + pa PA2 + ae AE3 fail
  3cb5281 ckpt 544: PatchFadeTest adjusted for LEAD_MS 10 (unit suite + lint green locally)
  b4391f8 ckpt 543: pt run 249: 9 PASS: fade open_max 3-6 % vs jump 26 %; found the mid-open glitc
  964beab ckpt 542: review of v0.8.1 (agent report) fixed: stored crop invalidated when it no long
```
