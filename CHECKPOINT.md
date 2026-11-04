# CHECKPOINT 231 — read me first, then TASKS.md

**Written:** 2026-11-04T08:17:21Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `bee2b86` (this checkpoint is the commit after it)

## Just done
gated v0.4.0 (code 14) and pushed it: the handoff self-test, the full unit suite + lint
and the versionCode check all passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow
android.yml, ref main, inputs {"full_build": "true"}. When that run is green,
confirm the Release is published (get_release_by_tag is enough) and then run:
  bash tools/record-release.sh v0.4.0 "v0.4.0: lock-screen handshakes (6 methods, no gesture into Quickstep), two-profile lock-in (Solitaire + custom home pre-armed behind the lock screen, plain unlock ends a Quickstep session, restart ends it), 7 ways back to custom, master failsafe (Konami key code, Solitaire hotspot, helper-off triple Home), invisible adb clean-up helper, wallpaper/number-reset buttons removed"

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  bee2b86 ckpt 230: Run 47: 76 PASS / 0 FAIL (INVISIBLE force-stop 3/3 in 166 ms, no cover, Recent
  9521144 ckpt 229: Wrote N9 (faster emulator workflow, after v0.4.0) into TASKS from Tj's yes
  f6e7aae ckpt 228: Run 46 read: 74 PASS, INVISIBLE WORKS on the emulator ('force-stopped 3 of 3 a
  e1834c0 ckpt 227: Run 45 read: emulator survived (run 44 death was a flake); only 3 FAIL, all N6
  c762a6d ckpt 226: Run 44 read: tap-erase/prearm/plain-unlock/icon PASS; EMULATOR DIED (qemu gone
  74302d6 ckpt 225: Run 43 read (artifact): restart check now PASS; tap-erase, prearm, plain-unloc
  6d001dc ckpt 224: Run 42 read from the artifact: PASS tap-erase handshake, prearm behind keyguar
  6c619c5 ckpt 223: N6 shell helper (ShellProtocol/ShellServer/ShellHelper, invisible path in Retu
  23ee234 ckpt 222: Run 41: 5 FAIL all in scenario section N (not product): tap-erase was off by d
  d1bebea ckpt 221: Wrote N6/N7/N8 (invisible adb clean-up, restart survival, real-profile plausib
```
