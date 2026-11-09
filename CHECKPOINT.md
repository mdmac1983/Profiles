# CHECKPOINT 755 — read me first, then TASKS.md

**Written:** 2026-11-09T02:23:00Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/kind-pasteur-ccqmmc` · **builds on:** `cd85b8a` (this checkpoint is the commit after it)

## Just done
v1.1 built: ProfileRules(+test)/Profiles, per-profile apps/tiles/status bar/radios/banners, handshake->profile at unlock (secretAccepted/applyUnlockChoiceBody), settings Profiles group + Opens: per handshake, guide; emulator section pr

## Do this next
dispatch emulator 'base,e1,pr,o,n' on main; fix; then versionCode 38 / 1.1, /code-review high on v1.0..HEAD, gate, ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M .github/workflows/emulator.yml
     M CHECKPOINT.md

## Last ten checkpoints
```
  c870ebf ckpt 754: BT1 written: build v1.1..v1.5
  aa7af8f ckpt 753: v1.0 RELEASED + RECORDED (android.yml #2 id 42548773723, 9ebe111; https://gith
  e1ac721 ckpt 752: ship.sh v1.0 green (code 37) pushed 9ebe111; android.yml full_build=true dispa
  9ebe111 ckpt 751: gated v1.0 (code 37) and pushed it: the handoff self-test, the full unit suite
  892ede6 ckpt 750: gate done: all sections green except legacy flake (W5/reset2/W3b alternate on 
  50decf5 ckpt 749: gate run #3 (legacy,x1,ad,pa,uf,nq,fp): 2 FAIL in legacy (reset2 shade clear, 
  ef09827 ckpt 748: gate: runs #1 #6 green; #5 (base,e1,t,bs,ao,hp) 4 FAIL HP5/6/8/11 = scenario b
  f87f74e ckpt 747: 6 emulator groups dispatched ~01:13 UTC on main 3af5ec5 (full gate); README fo
  3af5ec5 ckpt 746: v1.0 review (base bf0615b = v0.8.11 state; 206b3a8 absent in fork): 10 finding
  46ad59b ckpt 745: v1.0 part 2: in-app updater (UpdateRules+test, Updater via DownloadManager, Up
```

(10 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
