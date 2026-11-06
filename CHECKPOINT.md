# CHECKPOINT 434 — read me first, then TASKS.md

**Written:** 2026-11-06T04:24:50Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-22ba09da-s9vh1t` · **builds on:** `e610616` (this checkpoint is the commit after it)

## Just done
AD5 bisect dispatched on e610616: v,ad=run 176 (id 42162299197), ka,ad=run 177 (id 42162301941), v,ka,ac,ad=run 178 (id 42162304907); gate A run 174 still running

## Do this next
read 176/177 at ~+12 min, 178 at ~+20; INFO 'AD5 r1 evidence' + lock-log line 'stock home, no resume' show why; gate A job 127700889299

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  e610616 ckpt 433: AD5 diagnostics: lock-log 'stock home, no resume: <why>' + ResumeTarget.whyNot
  2262058 ckpt 432: TK3 gate 2: B' only AD5 x2 fails (consistent in v,ka,ac,ad; passes in ac,ad,se
  2167b58 ckpt 431: AE3/AD7b build plan AE3-1..6 written (docs only); gates A=174 B'=175 running
  165fbd1 ckpt 430: TK3 gate 2 dispatched on b5c0efe: A=run 174 (id 42159304859), B'(no g)=run 175
  b5c0efe ckpt 429: TK3: review fixes RV1-RV5 coded + unit 351/0 + lint ok; dispatching gate A + B
  933914d ckpt 428: TK3 review: 7 confirmed (all low), 6 refuted; RV1-RV5 written to TASKS
  9b152bb ckpt 427: TK3: b6 alone run 171 = SUCCESS on f869db3 (gate A b6 fails = flake pending co
  ce94c91 ckpt 426: AF1: Tj says stop on the phone guide: g rerun cancelled, g dropped from v0.7.4
  d8271fe ckpt 425: TK3 reruns: b6=run 171 (id 42157821447), g=run 172 (id 42157824026), ac+ad+se=
  f869db3 ckpt 424: TK3 gate 1 NOT green (A: b6 x2; B: g x8, ad AD5 x2); reruns b6, g, ac+ad+se di
```
