# CHECKPOINT 761 — read me first, then TASKS.md

**Written:** 2026-11-09T03:35:28Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/kind-pasteur-ccqmmc` · **builds on:** `933513b` (this checkpoint is the commit after it)

## Just done
gate round 2: run17 only CU5 (known first-frame flake), run18 GE2 (lock action: now logged) + O1 (o/uf wrote prefs into a RUNNING app: force-stop added = the UF order failure too); solo #15 UF green

## Do this next
dispatch base,ge,o,ac,uf on head; read run 16 (n,t,x,ac,se,lq,an,hp) id 42554389081

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  1cbd88b ckpt 760: gate v1.1 round 1 (32ed08f): run10 CS-A3/A4 (known flake), run11 UF1/UF2 (plai
  49a4fd9 ckpt 759: v1.4 gestures built: GestureRules(+test), TjHooks gestureTouch/runGesture, per
  47f68da ckpt 758: v1.3 layout built (ProfileLayout, LauncherProvider switch, PR1b); per-profile 
  272a245 ckpt 757: v1.2 work (Rental restrictions, BT1c) built: RentalRules(+test), drawer hiding
  1f90cf8 ckpt 756: v1.1 = profiles + the 8 new handshakes (BT1b folded in): UnlockMethods RHYTHM/
  df36d74 ckpt 755: v1.1 built: ProfileRules(+test)/Profiles, per-profile apps/tiles/status bar/ra
  c870ebf ckpt 754: BT1 written: build v1.1..v1.5
  aa7af8f ckpt 753: v1.0 RELEASED + RECORDED (android.yml #2 id 42548773723, 9ebe111; https://gith
  e1ac721 ckpt 752: ship.sh v1.0 green (code 37) pushed 9ebe111; android.yml full_build=true dispa
  9ebe111 ckpt 751: gated v1.0 (code 37) and pushed it: the handoff self-test, the full unit suite
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
