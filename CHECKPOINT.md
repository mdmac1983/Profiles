# CHECKPOINT 336 — read me first, then TASKS.md

**Written:** 2026-11-05T08:25:53Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `7ffa2e7` (this checkpoint is the commit after it)

## Just done
v0.7.0 experiment (run 129) = identical legacy-alone failure on 2a547f6: the no-access reset failure is PRE-EXISTING; triggered android.yml full_build on main: run 20 (id 41994326987) on 4686fda

## Do this next
read run 20 from ~08:34Z (list_workflow_jobs for the job id, get_job_logs tail 40; build + sign + verify + publish): green = get_release_by_tag v0.7.1 (Launcher + PhoneGuide APK), then bash tools/record-release.sh v0.7.1 "v0.7.1: PhoneGuide 1.2 short-cable guide, tablet report with launcher state + side-key test + short summary, tablet-intake tool, code-review fixes"; then tick PG1-PG4 in TASKS with proof, give Tj the Release link

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  4686fda ckpt 335: ship.sh gates passed again for v0.7.1 (code 18; handoff, unit, lint green; pus
  ee16860 ckpt 334: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
  b1225d0 ckpt 333: dispatched on main at 41d5c9b: run 127 = legacy (id 41991766360), run 128 = ba
  41d5c9b ckpt 332: run 125 (legacy): 19 PASS 1 FAIL (reset2 notification not cleared in time; W5/
  2b571e9 ckpt 331: workflow step limit 2100 -> 2700 s and docs: the release gate is the two runs 
  bb99c07 ckpt 330: FULL gate run 124 (3398b67): 167 PASS 3 FAIL, cut by the 2100 s timeout in rs4
  4b02f31 ckpt 329: run 123 (x on 3398b67) green: 31 PASS 0 FAIL (setup screen finds all 5 steps, 
  1657930 ckpt 328: dispatched on main at 3398b67: run 123 = x (id 41986024102), run 124 = FULL ga
  3398b67 ckpt 327: review fixes in: keyTestLines separate caps (many input devices cannot drop KE
  b185fe1 ckpt 326: code-review of the v0.7.0..HEAD delta (medium): 9 findings, triaged against th
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
