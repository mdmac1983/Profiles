# CHECKPOINT 220 — read me first, then TASKS.md

**Written:** 2026-11-04T05:56:06Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `31036f4` (this checkpoint is the commit after it)

## Just done
Lint + debug build green locally; PROFILES doc has N4/N5 ladder; emulator run 41 (https://github.com/tjshea90/Launcher/actions/runs/41782076557, sha 39eb540) in progress

## Do this next
read run 41 result: actions_get download artifact or job logs; results.txt N-section (N2/N3/N4/N5 checks) must be 0 FAIL; fix any FAIL; then tick N1-N5 in TASKS with the proving test names, bump version (ship.sh v0.4.0), android.yml full_build, record-release; answer Tj

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  d563818 ckpt 219: Dispatched emulator.yml on claude/admiring-gates-1ut5m3 (run not yet read)
  39eb540 ckpt 218: N1-N5 coded: buttons removed, UnlockMethods x6, gesture removed, ProfileSessio
  1b534f7 ckpt 217: Built N1 (buttons removed), N2 (UnlockMethods x6 + tests), N3a (gesture remove
  d29d8c4 ckpt 216: Wrote N1-N3 (Tj's 2026-11-04 3-part request) into TASKS.md; read RouterActivit
  87e5f36 ckpt 215: Handoff written for a new session: research/HANDOFF_2026-11-04.md (v0.3.7 rele
  47b476a ckpt 212: RELEASED v0.3.7 (Build APK green, Release published, signed, 9.19 MB; emulator
  fe43e6c ckpt 210: v0.3.7 gated (code 13) and pushed (3451263 on main); android.yml full_build di
  3451263 ckpt 209: gated v0.3.7 (code 13) and pushed it: the handoff self-test, the full unit sui
  3ecc89a ckpt 208: pre-ship: v0.3.7: choose the launcher at unlock (Delete twice before your PIN 
  494614c ckpt 207: Run 39 (1eca5f1): 59 PASS / 1 FAIL. E1 WORKS on the emulator: the helper sees 
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
