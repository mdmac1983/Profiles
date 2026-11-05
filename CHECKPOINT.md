# CHECKPOINT 340 — read me first, then TASKS.md

**Written:** 2026-11-05T18:14:38Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `430fc04` (this checkpoint is the commit after it)

## Just done
Tj's 3rd screenshots: the helper starts fine (unix up for uid 10243, tcp 47631) but DIES within minutes (launcher log 13:08: unix Connection refused); the launcher cannot create TCP sockets here (EPERM on all ports); told Tj to restart it, keep the cable plugged, run buttons 1 then 2; facts + HLP1/HLP2 saved in TASKS.md

## Do this next
WAIT for Tj's tablet-report (or the 2b summary text) and the end-of-session ps check (does the helper still run?); then bash tools/tablet-intake.sh <file>, read research/tablet/LATEST.md; then HLP1 (why the helper dies: tj.log, logcat -b crash, report) and HLP2 (INTERNET granted?), PG5/PG6, RS1a-c as v0.7.2

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  bf2b0c8 ckpt 339: Tj's 2nd screenshot (1:02): helper start attempt 1 left no process, attempt 2 
  336c9e7 ckpt 338: Tj is in the CABLE SESSION (screenshot 12:59 phone clock): shell uid 2000 OK, 
  63a17ba ckpt 337: v0.7.1 RELEASED (android.yml run 20 green, signed): https://github.com/tjshea9
  e06b833 ckpt 336: v0.7.0 experiment (run 129) = identical legacy-alone failure on 2a547f6: the n
  4686fda ckpt 335: ship.sh gates passed again for v0.7.1 (code 18; handoff, unit, lint green; pus
  ee16860 ckpt 334: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
  b1225d0 ckpt 333: dispatched on main at 41d5c9b: run 127 = legacy (id 41991766360), run 128 = ba
  41d5c9b ckpt 332: run 125 (legacy): 19 PASS 1 FAIL (reset2 notification not cleared in time; W5/
  2b571e9 ckpt 331: workflow step limit 2100 -> 2700 s and docs: the release gate is the two runs 
  bb99c07 ckpt 330: FULL gate run 124 (3398b67): 167 PASS 3 FAIL, cut by the 2100 s timeout in rs4
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
