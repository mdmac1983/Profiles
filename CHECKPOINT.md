# CHECKPOINT 547 — read me first, then TASKS.md

**Written:** 2026-11-06T23:11:06Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-019b8e69-16dn91` · **builds on:** `d3c55fa` (this checkpoint is the commit after it)

## Just done
FINAL CODE for v0.8.1 (versionCode 26): v+ps run #258 22 PASS / 1 FAIL (PS C scrim reference 0.7497: strip moved away from the panel shadow); ps, ae, pa, lq alone all PASS (runs 254-257); reviewer fixes + hold confirm + LEAD 10 in

## Do this next
dispatch the final delta gate: 4 runs {base,y2,t,v,ps,uf} {x1,e1,b6,o,bs,lq,ao,rb} {n,x,ac,ad,ae,pa,rg,last} {pt}; if all green: tick TASKS, CHECKPOINT/HANDOFF, bash ship.sh 'v0.8.1 ...', android.yml full_build=true on the branch, get_release_by_tag, tools/record-release.sh

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  92016c2 ckpt 546: dispatched on ckpt 545 (23:01 UTC): ps #254 id 42308896343, ae #255 id 4230890
  1d45987 ckpt 545: gate round 1 (ckpt 540 code) read: pt #249 9 PASS; ps B + pa PA2 + ae AE3 fail
  3cb5281 ckpt 544: PatchFadeTest adjusted for LEAD_MS 10 (unit suite + lint green locally)
  b4391f8 ckpt 543: pt run 249: 9 PASS: fade open_max 3-6 % vs jump 26 %; found the mid-open glitc
  964beab ckpt 542: review of v0.8.1 (agent report) fixed: stored crop invalidated when it no long
  dc94174 ckpt 541: dispatched on ckpt 540 (sha 97e801d, 22:34 UTC): pt #249 (run id 42305657809),
  97e801d ckpt 540: emulator.yml: ffmpeg install step has retries + 4 min timeout (run 248 hung 6 
  39c5ade ckpt 539: pt run 247: dialog event now handled in 2-17 ms (was 80-280) but the first dim
  a52dfe7 ckpt 538: quietWork cancelled in cancelPatchWork; TASKS AZ1 FLICKER FINDINGS written (ru
  ce7e0db ckpt 537: pt run 245 analysed: main-thread stall (PIN-pad scan + power-menu scan at the 
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
