# CHECKPOINT 636 — read me first, then TASKS.md

**Written:** 2026-11-07T23:26:00Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-fafa2908-h7bsez` · **builds on:** `089d8ed` (this checkpoint is the commit after it)

## Just done
v0.8.5 RELEASED 23:16 UTC (https://github.com/tjshea90/Launcher/releases/tag/v0.8.5, android.yml run 32 id 42424362269 on 239af0c, recorded in BUILDLOG 23:24); TASKS BE1-BE3/BD1/BD2 ticked with proof, BE4 (tablet check) open; HANDOFF 0h + CLAUDE lessons written

## Do this next
NOTHING is waiting on Claude except new requests. Waiting on Tj: install v0.8.5 and do research/TABLET_CHECKLIST_v0.8.5.md (BE4): plain PIN after Quickstep (no Solitaire, empty Recents, how long the black screen), the same with the adb helper running (no cover), hidden apps never in Recents, patch flicker / Screenshot flash, and the tablet report right after a slow unlock (bash tools/tablet-intake.sh). If the no-helper cover is still too long: drop the Enter-tap cover in a clean start; persistent helper needs the cable. Open from earlier: BB1 / BB3c / BC4 / AU5.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M research/TABLET_CHECKLIST_v0.8.5.md

## Last ten checkpoints
```
  3507251 ckpt 635: RELEASE v0.8.5 triggered: android.yml run 32 id 42424362269 on 239af0c; all ga
  239af0c ckpt 634: gated v0.8.5 (code 30) and pushed it: the handoff self-test, the full unit sui
  016f5e9 ckpt 633: final gate re-run on c4456a9 (app code = 2999c35): #322 id 42422768769 {base,e
  8f46940 ckpt 632: final gate read: G3 green; G1 4 FAIL all in pt (0.75/0.80 scrim state + a late
  1028360 ckpt 631: FINAL GATE dispatched 22:00 UTC on 2999c35: #318 id 42418473647 {x1,y2,b4,pt,u
  2999c35 ckpt 630: version 0.8.5 / code 30 (the release candidate); gate groups chosen from tools
  deaa82c ckpt 629: recentsEmpty: deadline in every branch + card press cap (the #313/#314 hang), 
  aee9fd9 ckpt 628: emulator read #1: ae,ps + pt green; cs found the no-helper Clear-all hole (fix
  4e85854 ckpt 627: BE1-BE3 built; emulator runs #307 id 42412739934 {cs,rg,uf}, #308 id 424127423
  713f3b2 ckpt 626: BE1 patch (ACTION_OUTSIDE hide, fast-open fix, precreate at lock, diag), BE2/B
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
