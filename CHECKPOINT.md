# CHECKPOINT 232 — read me first, then TASKS.md

**Written:** 2026-11-04T08:17:37Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `2f54373` (this checkpoint is the commit after it)

## Just done
v0.4.0 (code 14) gated + pushed; android.yml full_build=true dispatched on main (not yet read)

## Do this next
poll actions_list android.yml runs (branch main) until green; get_release_by_tag v0.4.0; bash tools/record-release.sh v0.4.0 '<note from ship.sh>' ONLY after green; give Tj the Release link as plain text on its own line; THEN N9

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  2f54373 ckpt 231: gated v0.4.0 (code 14) and pushed it: the handoff self-test, the full unit sui
  bee2b86 ckpt 230: Run 47: 76 PASS / 0 FAIL (INVISIBLE force-stop 3/3 in 166 ms, no cover, Recent
  9521144 ckpt 229: Wrote N9 (faster emulator workflow, after v0.4.0) into TASKS from Tj's yes
  f6e7aae ckpt 228: Run 46 read: 74 PASS, INVISIBLE WORKS on the emulator ('force-stopped 3 of 3 a
  e1834c0 ckpt 227: Run 45 read: emulator survived (run 44 death was a flake); only 3 FAIL, all N6
  c762a6d ckpt 226: Run 44 read: tap-erase/prearm/plain-unlock/icon PASS; EMULATOR DIED (qemu gone
  74302d6 ckpt 225: Run 43 read (artifact): restart check now PASS; tap-erase, prearm, plain-unloc
  6d001dc ckpt 224: Run 42 read from the artifact: PASS tap-erase handshake, prearm behind keyguar
  6c619c5 ckpt 223: N6 shell helper (ShellProtocol/ShellServer/ShellHelper, invisible path in Retu
  23ee234 ckpt 222: Run 41: 5 FAIL all in scenario section N (not product): tap-erase was off by d
```
