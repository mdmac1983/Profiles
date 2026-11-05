# CHECKPOINT 339 — read me first, then TASKS.md

**Written:** 2026-11-05T18:05:58Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `9e03f75` (this checkpoint is the commit after it)

## Just done
Tj's 2nd screenshot (1:02): helper start attempt 1 left no process, attempt 2 shows 1 process, but the tablet says NOT running; diagnostics sent (stale screen? logcat TjReset reason, /proc/net/tcp listen check, restart with log); facts + PG6 saved in TASKS.md

## Do this next
WAIT for Tj's screenshot of: logcat -d -s TjReset:I | grep -i 'not reachable' | tail -3 and grep -E 'BA(0F|1[0-3])' /proc/net/tcp (0A = listening); if nothing listens: the restart-with-log line (cat /data/local/tmp/tj.log). Decide from the reason: ECONNREFUSED on all = helper dies at start (read the log), EACCES/Permission denied = SELinux on his ROM (needs a different transport), listening but not reachable = slow answer vs the 700 ms limit. Then PG5/PG6 + RS1a-c as v0.7.2

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  336c9e7 ckpt 338: Tj is in the CABLE SESSION (screenshot 12:59 phone clock): shell uid 2000 OK, 
  63a17ba ckpt 337: v0.7.1 RELEASED (android.yml run 20 green, signed): https://github.com/tjshea9
  e06b833 ckpt 336: v0.7.0 experiment (run 129) = identical legacy-alone failure on 2a547f6: the n
  4686fda ckpt 335: ship.sh gates passed again for v0.7.1 (code 18; handoff, unit, lint green; pus
  ee16860 ckpt 334: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
  b1225d0 ckpt 333: dispatched on main at 41d5c9b: run 127 = legacy (id 41991766360), run 128 = ba
  41d5c9b ckpt 332: run 125 (legacy): 19 PASS 1 FAIL (reset2 notification not cleared in time; W5/
  2b571e9 ckpt 331: workflow step limit 2100 -> 2700 s and docs: the release gate is the two runs 
  bb99c07 ckpt 330: FULL gate run 124 (3398b67): 167 PASS 3 FAIL, cut by the 2100 s timeout in rs4
  4b02f31 ckpt 329: run 123 (x on 3398b67) green: 31 PASS 0 FAIL (setup screen finds all 5 steps, 
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
