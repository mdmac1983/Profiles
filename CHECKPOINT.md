# CHECKPOINT 343 — read me first, then TASKS.md

**Written:** 2026-11-05T18:33:33Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-888039cc-xwzdeh` · **builds on:** `c053ac2` (this checkpoint is the commit after it)

## Just done
Tj: the tablet says Shell helper NOT running right after a fresh restart (helper printed unix socket up); step 3 of 4 sent: the launcher's logged reason; HLP3 saved in TASKS.md

## Do this next
WAIT for Tj's screenshot of: logcat -d -s TjReset:I | grep -i 'not reachable' | tail -1 | cut -c1-180. Permission denied = SELinux blocks the unix socket (design a file-mailbox transport, TASKS HLP3); Connection refused = namespace or dead helper (check ps + tj.log); then decide the next short step for Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  97cb41f ckpt 342: Tj's step 1 screenshot (1:31 phone clock): helper restarted OK ([1] 12440, uni
  000d69b ckpt 341: Tj says the launcher crashed when first opened on the tablet and now seems to 
  c87f389 ckpt 340: Tj's 3rd screenshots: the helper starts fine (unix up for uid 10243, tcp 47631
  bf2b0c8 ckpt 339: Tj's 2nd screenshot (1:02): helper start attempt 1 left no process, attempt 2 
  336c9e7 ckpt 338: Tj is in the CABLE SESSION (screenshot 12:59 phone clock): shell uid 2000 OK, 
  63a17ba ckpt 337: v0.7.1 RELEASED (android.yml run 20 green, signed): https://github.com/tjshea9
  e06b833 ckpt 336: v0.7.0 experiment (run 129) = identical legacy-alone failure on 2a547f6: the n
  4686fda ckpt 335: ship.sh gates passed again for v0.7.1 (code 18; handoff, unit, lint green; pus
  ee16860 ckpt 334: gated v0.7.1 (code 18) and pushed it: the handoff self-test, the full unit sui
  b1225d0 ckpt 333: dispatched on main at 41d5c9b: run 127 = legacy (id 41991766360), run 128 = ba
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
