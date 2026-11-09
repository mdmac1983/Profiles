# CHECKPOINT 757 — read me first, then TASKS.md

**Written:** 2026-11-09T02:39:01Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/kind-pasteur-ccqmmc` · **builds on:** `2d5f0ba` (this checkpoint is the commit after it)

## Just done
v1.2 work (Rental restrictions, BT1c) built: RentalRules(+test), drawer hiding, guardRental (Home/Back), 3 Rental switches; emulator section rr; unit+lint green

## Do this next
v1.1 gate runs (dispatched ~02:38 on c.70ab, sections e1,pr,y2,t,cs,pf,rb / x1,x,ac,lq,uf,cs2,nq / base,legacy,n,o,an,im,last): read at ~03:01; then rr+pr solo on head

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M .github/workflows/emulator.yml
     M CHECKPOINT.md
     M tools/emulator_scenario.sh
     M tools/gate-map.txt

## Last ten checkpoints
```
  1f90cf8 ckpt 756: v1.1 = profiles + the 8 new handshakes (BT1b folded in): UnlockMethods RHYTHM/
  df36d74 ckpt 755: v1.1 built: ProfileRules(+test)/Profiles, per-profile apps/tiles/status bar/ra
  c870ebf ckpt 754: BT1 written: build v1.1..v1.5
  aa7af8f ckpt 753: v1.0 RELEASED + RECORDED (android.yml #2 id 42548773723, 9ebe111; https://gith
  e1ac721 ckpt 752: ship.sh v1.0 green (code 37) pushed 9ebe111; android.yml full_build=true dispa
  9ebe111 ckpt 751: gated v1.0 (code 37) and pushed it: the handoff self-test, the full unit suite
  892ede6 ckpt 750: gate done: all sections green except legacy flake (W5/reset2/W3b alternate on 
  50decf5 ckpt 749: gate run #3 (legacy,x1,ad,pa,uf,nq,fp): 2 FAIL in legacy (reset2 shade clear, 
  ef09827 ckpt 748: gate: runs #1 #6 green; #5 (base,e1,t,bs,ao,hp) 4 FAIL HP5/6/8/11 = scenario b
  f87f74e ckpt 747: 6 emulator groups dispatched ~01:13 UTC on main 3af5ec5 (full gate); README fo
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
