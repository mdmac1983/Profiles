# CHECKPOINT 566 — read me first, then TASKS.md

**Written:** 2026-11-07T01:31:04Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/power-menu-wallpaper-brightness-g7588s` · **builds on:** `c85092c` (this checkpoint is the commit after it)

## Just done
review of BA1/BA2 done: 3 major fixed (memory wipe on abandoned handshake, restore after a manual off, stray BACK), minors + nits fixed; unit+lint green

## Do this next
wait #274 (ps alone id 42324039079) and #275 (bl,ps id 42324041525): do they show what leaves the bright layer before ps? Then dispatch the FINAL gate on the final commit (tools/gate.sh 7d85b50 groups) and ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  9281c6d ckpt 565: gate #271: ps phase B 4 FAIL (bright layer over the slot, after x1,e1,bl); dia
  3056094 ckpt 564: BA1 proven: pt #272 15 PASS (MEASURED 0.7997/0.7999/0.7999 off 0.03/0.0035/0.0
  27728f8 ckpt 563: BA4 written: Tj will send a photo of the patch later (his words in TASKS)
  102771e ckpt 562: waiting: pt #272 job 128248073003, gates 128247261992 + 128247271172, code rev
  747eebd ckpt 561: pt phase C fixed (no extra lock); gate jobs: 128247261992 {n,o,t,x,ac,bs,lq}, 
  0d6e1fe ckpt 560: gate runs dispatched on 5e89113: #270 id 42321616646 = {n,o,t,x,ac,bs,lq}, #27
  5e89113 ckpt 559: gate prep: decode retry, Control switch also stops using a stored measurement,
  35e87c1 ckpt 558: HANDOFF 0g + CLAUDE.md lessons written; scrim decision uses the stored measure
  11b1655 ckpt 557: BA2 boxes ticked with run #265 proof; BA1 status written; guard hold bookkeepi
  13c6146 ckpt 556: BA1a-c coded: ScrimFit (robust per-channel fit, 17 tests), ScrimMeasure (polic
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
