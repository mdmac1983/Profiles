# CHECKPOINT 569 — read me first, then TASKS.md

**Written:** 2026-11-07T01:45:08Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/power-menu-wallpaper-brightness-g7588s` · **builds on:** `caf5648` (this checkpoint is the commit after it)

## Just done
AZ4 root cause found (stale tj_restore.xml snapshot restores lock-screen notifications in ps/pa/pt after a section ends with setfeat): scenario fix; app code unchanged since dced3e9

## Do this next
dispatch bl,ps,pa to confirm; read final gate #276 id 42324558796, #277 id 42324561598, #278 id 42324581655 (#279 ps id 42324585039 already green); then ship (git diff dced3e9 -- app must be empty)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  2719c14 ckpt 568: FINAL GATE on dced3e9: #276 A{n,o,t,x,ac,bs,lq} id 42324558796, #277 B{base,v,
  dced3e9 ckpt 567: version 0.8.2 / code 27 (final gate runs on this commit)
  3815d6b ckpt 566: review of BA1/BA2 done: 3 major fixed (memory wipe on abandoned handshake, res
  9281c6d ckpt 565: gate #271: ps phase B 4 FAIL (bright layer over the slot, after x1,e1,bl); dia
  3056094 ckpt 564: BA1 proven: pt #272 15 PASS (MEASURED 0.7997/0.7999/0.7999 off 0.03/0.0035/0.0
  27728f8 ckpt 563: BA4 written: Tj will send a photo of the patch later (his words in TASKS)
  102771e ckpt 562: waiting: pt #272 job 128248073003, gates 128247261992 + 128247271172, code rev
  747eebd ckpt 561: pt phase C fixed (no extra lock); gate jobs: 128247261992 {n,o,t,x,ac,bs,lq}, 
  0d6e1fe ckpt 560: gate runs dispatched on 5e89113: #270 id 42321616646 = {n,o,t,x,ac,bs,lq}, #27
  5e89113 ckpt 559: gate prep: decode retry, Control switch also stops using a stored measurement,
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
