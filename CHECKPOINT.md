# CHECKPOINT 338 — read me first, then TASKS.md

**Written:** 2026-11-05T18:01:08Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `3dfb1b1` (this checkpoint is the commit after it)

## Just done
Tj is in the CABLE SESSION (screenshot 12:59 phone clock): shell uid 2000 OK, launcher installed, helper start line accepted ([1] 10761; the scrambled echo is the ADB OTG app); facts saved in TASKS.md (picture not committed); the handoff files + CLAUDE/AGENTS learnings block are committed, the 'interrupted mid-change' warning for them is a false alarm

## Do this next
Tj runs the check card 'Is the helper running?' (ps -A -o ARGS | grep -c '[S]hellServer'): 1 = unplug, PART 3 on the tablet (Launcher control > Tablet setup & report > 1, 2, optional 3, 2b) then send tablet-report.txt: bash tools/tablet-intake.sh <file>, read research/tablet/LATEST.md first; 0 = the second form of the start card, then the two-step card. After that: RS1a-c, PG5 (with a v0.7.2)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  63a17ba ckpt 337: v0.7.1 RELEASED (android.yml run 20 green, signed): https://github.com/tjshea9
  e06b833 ckpt 336: v0.7.0 experiment (run 129) = identical legacy-alone failure on 2a547f6: the n
  4686fda ckpt 335: ship.sh gates passed again for v0.7.1 (code 18; handoff, unit, lint green; pus
  ee16860 ckpt 334: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
  b1225d0 ckpt 333: dispatched on main at 41d5c9b: run 127 = legacy (id 41991766360), run 128 = ba
  41d5c9b ckpt 332: run 125 (legacy): 19 PASS 1 FAIL (reset2 notification not cleared in time; W5/
  2b571e9 ckpt 331: workflow step limit 2100 -> 2700 s and docs: the release gate is the two runs 
  bb99c07 ckpt 330: FULL gate run 124 (3398b67): 167 PASS 3 FAIL, cut by the 2100 s timeout in rs4
  4b02f31 ckpt 329: run 123 (x on 3398b67) green: 31 PASS 0 FAIL (setup screen finds all 5 steps, 
  1657930 ckpt 328: dispatched on main at 3398b67: run 123 = x (id 41986024102), run 124 = FULL ga
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
