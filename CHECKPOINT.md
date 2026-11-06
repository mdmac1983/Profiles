# CHECKPOINT 550 — read me first, then TASKS.md

**Written:** 2026-11-06T23:35:05Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-019b8e69-16dn91` · **builds on:** `c2cef72` (this checkpoint is the commit after it)

## Just done
TASKS: 21 boxes ticked with proof (tools/tick_v081.py), AZ FINAL + AZ4 written; ps alone PASS (run 263); pa alone dispatched (run 264)

## Do this next
when pa alone (run 264) is green: bash ship.sh 'v0.8.1 ...' ; android.yml full_build=true ; get_release_by_tag ; tools/record-release.sh ; reply to Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  2808072 ckpt 549: final gate: #260 {x1,e1,b6,o,bs,lq,ao,rb} PASS, #262 pt PASS, #259 {base,y2,t,
  0919477 ckpt 548: final delta gate dispatched on ckpt 547 (sha 04e6cd1, 23:11 UTC): #259 id 4231
  04e6cd1 ckpt 547: FINAL CODE for v0.8.1 (versionCode 26): v+ps run #258 22 PASS / 1 FAIL (PS C s
  92016c2 ckpt 546: dispatched on ckpt 545 (23:01 UTC): ps #254 id 42308896343, ae #255 id 4230890
  1d45987 ckpt 545: gate round 1 (ckpt 540 code) read: pt #249 9 PASS; ps B + pa PA2 + ae AE3 fail
  3cb5281 ckpt 544: PatchFadeTest adjusted for LEAD_MS 10 (unit suite + lint green locally)
  b4391f8 ckpt 543: pt run 249: 9 PASS: fade open_max 3-6 % vs jump 26 %; found the mid-open glitc
  964beab ckpt 542: review of v0.8.1 (agent report) fixed: stored crop invalidated when it no long
  dc94174 ckpt 541: dispatched on ckpt 540 (sha 97e801d, 22:34 UTC): pt #249 (run id 42305657809),
  97e801d ckpt 540: emulator.yml: ffmpeg install step has retries + 4 min timeout (run 248 hung 6 
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
