# CHECKPOINT 427 — read me first, then TASKS.md

**Written:** 2026-11-06T03:46:32Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-22ba09da-s9vh1t` · **builds on:** `ce94c91` (this checkpoint is the commit after it)

## Just done
TK3: b6 alone run 171 = SUCCESS on f869db3 (gate A b6 fails = flake pending composition); g dropped (AF1); waiting run 173 ac+ad+se

## Do this next
~03:53 read run 173 (id 42157826815, job 127696086652): AD5 pass => B composition ok except the g drop; AD5 fail => debug the ac->ad state interaction (ResumeTarget / resume_qs_app)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  ce94c91 ckpt 426: AF1: Tj says stop on the phone guide: g rerun cancelled, g dropped from v0.7.4
  d8271fe ckpt 425: TK3 reruns: b6=run 171 (id 42157821447), g=run 172 (id 42157824026), ac+ad+se=
  f869db3 ckpt 424: TK3 gate 1 NOT green (A: b6 x2; B: g x8, ad AD5 x2); reruns b6, g, ac+ad+se di
  c20c330 ckpt 423: AE findings written (v2 not buildable now; AD7b design; MBX1/BATCH1 explained)
  5b9f077 ckpt 422: AE1-AE6 (Tj's answers) in TASKS.md; AE1/AE2/AE5 answered (switches already exi
  548833b ckpt 421: TK3: code-review workflow wf_8e100f49-bbd (5 lenses + verify) running in sessi
  26789ed ckpt 420: TK3: gates dispatched on 76fad6a: A=run 169 (id 42154508863), B=run 170 (id 42
  76fad6a ckpt 419: TK3: merged main (v0.7.3 record), bumped to 0.7.4 / code 21
  1b68379 ckpt 418: TK2 DONE: v0.7.3 released + recorded (main 6e06c39); push.sh/-e .git worktree 
  3d6e2c1 ckpt 417: v0.7.3 PUBLISHED (run 22 id 42153666541 success, both APKs on the Release); NO
```
