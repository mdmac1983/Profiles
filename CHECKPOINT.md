# CHECKPOINT 436 — read me first, then TASKS.md

**Written:** 2026-11-06T04:38:14Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-22ba09da-s9vh1t` · **builds on:** `364e8c0` (this checkpoint is the commit after it)

## Just done
FINAL gates on 364e8c0 dispatched 04:37Z: v,ad=run 179 (id 42163620016), gate A=run 180 (id 42163622907, PENDING behind cancelled run 174 in the same concurrency group), B'=run 181 (id 42163625623)

## Do this next
read 179 at ~04:46 (AD5 must pass), 181 at ~05:03, 180 when it has run; if run 174 never ends, A stays pending: re-dispatch A once it frees; then ship

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  364e8c0 ckpt 435: AD5 root cause = profile_preclean closes apps at lock when section v leaves th
  1dec65c ckpt 434: AD5 bisect dispatched on e610616: v,ad=run 176 (id 42162299197), ka,ad=run 177
  e610616 ckpt 433: AD5 diagnostics: lock-log 'stock home, no resume: <why>' + ResumeTarget.whyNot
  2262058 ckpt 432: TK3 gate 2: B' only AD5 x2 fails (consistent in v,ka,ac,ad; passes in ac,ad,se
  2167b58 ckpt 431: AE3/AD7b build plan AE3-1..6 written (docs only); gates A=174 B'=175 running
  165fbd1 ckpt 430: TK3 gate 2 dispatched on b5c0efe: A=run 174 (id 42159304859), B'(no g)=run 175
  b5c0efe ckpt 429: TK3: review fixes RV1-RV5 coded + unit 351/0 + lint ok; dispatching gate A + B
  933914d ckpt 428: TK3 review: 7 confirmed (all low), 6 refuted; RV1-RV5 written to TASKS
  9b152bb ckpt 427: TK3: b6 alone run 171 = SUCCESS on f869db3 (gate A b6 fails = flake pending co
  ce94c91 ckpt 426: AF1: Tj says stop on the phone guide: g rerun cancelled, g dropped from v0.7.4
```
