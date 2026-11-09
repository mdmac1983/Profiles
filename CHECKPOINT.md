# CHECKPOINT 746 — read me first, then TASKS.md

**Written:** 2026-11-09T01:12:44Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/kind-pasteur-ccqmmc` · **builds on:** `743990e` (this checkpoint is the commit after it)

## Just done
v1.0 review (base bf0615b = v0.8.11 state; 206b3a8 absent in fork): 10 findings, 8 fixed (UpdateStatusReceiver non-exported, socket profiles_shell_helper, stale offer forget, pendingConfirm, update_auto switch, curtainWallFailed, atomic Cached, fresh())

## Do this next
tests+lint ut4.log; then dispatch 6 emulator groups (gate.sh --full --groups 6) on mdmac1983/Profiles ref main

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  46ad59b ckpt 745: v1.0 part 2: in-app updater (UpdateRules+test, Updater via DownloadManager, Up
  f2e37ce ckpt 744: v1.0 part 1: stampDue wired (HomeCurtain.restampIfDue); applicationId app.orio
  9169d1a ckpt 743: BS1 written: all 15 answers + in-app updates, versions 1.0 +0.1, guide/feature
  dc521bc ckpt 742: BR1e answers: Owner=Quickstep, settings Owner-only, all customizations; consol
  e59c14e ckpt 741: BR1 written: profiles Owner / FBOP (plain PIN) / Rental, handshake->profile + 
  06ebb31 ckpt 740: BQ1 written: 8 pad/key handshakes chosen; handshake->profile map asked; nothin
  a0eec15 ckpt 739: BP1 answered: handshakes already toggles; new handshake ideas listed, none bui
  4d9ef4b ckpt 738: BO1 written (package rename, gestures, multi-profile PINs, app name, suggestio
  7f3f7d7 ckpt 737: BN1 written: Tj wants tweaks/customizations, none named yet; asked him which
  f6a978f ckpt 736: BM1 done: README.md rewritten (features, architecture, structure, code map, te
```

(2 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
