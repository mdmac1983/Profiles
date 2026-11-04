# CHECKPOINT 216 — read me first, then TASKS.md

**Written:** 2026-11-04T05:35:51Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/admiring-gates-1ut5m3` · **builds on:** `73bbd2e` (this checkpoint is the commit after it)

## Just done
Wrote N1-N3 (Tj's 2026-11-04 3-part request) into TASKS.md; read RouterActivity/UnlockPick/ControlActivity/TjHooks/ReturnReset

## Do this next
Read TjAccessibilityService lines 230-330 + 560-700 (unlock/keyguard/mode hooks), then do N1 (remove 2 buttons in ControlActivity), N2 (UnlockToken), N3a (remove gesture), research doc N3b

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  87e5f36 ckpt 215: Handoff written for a new session: research/HANDOFF_2026-11-04.md (v0.3.7 rele
  47b476a ckpt 212: RELEASED v0.3.7 (Build APK green, Release published, signed, 9.19 MB; emulator
  fe43e6c ckpt 210: v0.3.7 gated (code 13) and pushed (3451263 on main); android.yml full_build di
  3451263 ckpt 209: gated v0.3.7 (code 13) and pushed it: the handoff self-test, the full unit sui
  3ecc89a ckpt 208: pre-ship: v0.3.7: choose the launcher at unlock (Delete twice before your PIN 
  494614c ckpt 207: Run 39 (1eca5f1): 59 PASS / 1 FAIL. E1 WORKS on the emulator: the helper sees 
  feb3927 ckpt 206: Answered E2 (does the PIN matter): no, any numeric PIN works
  1eca5f1 ckpt 205: E1 coded (v0.3.7/code 13): UnlockPick token (Delete x2 on the empty PIN pad = 
  1bab462 ckpt 204: Recorded E1 (choose the launcher at unlock: two PINs / extra digit / gesture t
  4b3344b ckpt 203: RELEASED v0.3.6 (Build APK green, Release published, signed, 9.19 MB; emulator
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
