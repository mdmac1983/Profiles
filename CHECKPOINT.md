# CHECKPOINT 565 — read me first, then TASKS.md

**Written:** 2026-11-07T01:25:54Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/power-menu-wallpaper-brightness-g7588s` · **builds on:** `ec70043` (this checkpoint is the commit after it)

## Just done
gate #271: ps phase B 4 FAIL (bright layer over the slot, after x1,e1,bl); diagnostics added to ps; #270 all green

## Do this next
dispatch ps alone and bl,ps; read #273 base,v,pa,uf,last and the review agent; if ps passes alone the failure is order-dependent (AZ4 class): find what bl leaves on the lock screen

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  3056094 ckpt 564: BA1 proven: pt #272 15 PASS (MEASURED 0.7997/0.7999/0.7999 off 0.03/0.0035/0.0
  27728f8 ckpt 563: BA4 written: Tj will send a photo of the patch later (his words in TASKS)
  102771e ckpt 562: waiting: pt #272 job 128248073003, gates 128247261992 + 128247271172, code rev
  747eebd ckpt 561: pt phase C fixed (no extra lock); gate jobs: 128247261992 {n,o,t,x,ac,bs,lq}, 
  0d6e1fe ckpt 560: gate runs dispatched on 5e89113: #270 id 42321616646 = {n,o,t,x,ac,bs,lq}, #27
  5e89113 ckpt 559: gate prep: decode retry, Control switch also stops using a stored measurement,
  35e87c1 ckpt 558: HANDOFF 0g + CLAUDE.md lessons written; scrim decision uses the stored measure
  11b1655 ckpt 557: BA2 boxes ticked with run #265 proof; BA1 status written; guard hold bookkeepi
  13c6146 ckpt 556: BA1a-c coded: ScrimFit (robust per-channel fit, 17 tests), ScrimMeasure (polic
  102c1f9 ckpt 555: BA2a-c coded: BatterySaverPolicy BA2 rules (47 JVM tests green), BatterySaverG
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
