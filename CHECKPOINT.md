# CHECKPOINT 668 — read me first, then TASKS.md

**Written:** 2026-11-08T03:51:19Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-yhyifc` · **builds on:** `a4b596d` (this checkpoint is the commit after it)

## Just done
v0.8.8 RELEASED 03:49 UTC (android.yml run 35 id 42444134135, https://github.com/tjshea90/Launcher/releases/tag/v0.8.8) and RECORDED in BUILDLOG; HANDOFF 0k + TASKS BH1b ticked; Tj's 02:29 notification message answered (BH1/BH2)

## Do this next
WAIT FOR TJ (nothing is half-done): BH1c = where the notification was seen (banner / status-bar icon / shade / lock screen) + which app + the tablet report. When the report comes: bash tools/tablet-intake.sh <file>, read research/tablet/LATEST.md first, then the report lines 'notifications seen (BH1', 'pop-up banners (BH1' and 'lock screen quiet:'. By his answer: banner = confirm, done; status-bar icon = offer the existing 'hide the status bar in custom' switch (no code) or a helper janitor; shade = janitor or leave; lock screen = check lock_screen_show_notifications in the report. Still open: BG2b (3 answers), BE4/BF1i/v0.8.7/v0.8.8 tablet checklists, BH3, CBR2, RS1, AF2, AU5, BB1/BB3c/BC4

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  d99d60f ckpt 667: v0.8.8 RELEASE build triggered 03:47 UTC: android.yml run 35 id 42444134135 on
  b1399c9 ckpt 666: gated v0.8.8 (code 33) and pushed it: the handoff self-test, the full unit sui
  a3bc971 ckpt 665: v0.8.8 GATE read 03:41 UTC (head 73e65ff, product code final): #342 id 4244234
  0037ea5 ckpt 664: v0.8.8 GATE dispatched 03:21 UTC on 73e65ff (product code final, nq alone #341
  e09cc2b ckpt 663: BH1: v0.8.8 candidate (versionCode 33): version bump, research/NOTIFICATIONS_Q
  f7b6ee0 ckpt 662: BH1 review fixes (8 findings: helper fact in Quickstep, hold state in a Settin
  0095fec ckpt 661: BH1b step 3: emulator section nq (10 checks NQ0-NQ10) + testnotifier high-impo
  75f3927 ckpt 660: BH1b step 2: Launcher control switch + report lines + NotifSightings (10 JVM t
  afef3c6 ckpt 659: BH1b step 1: NotifQuietPolicy + NotifQuietHold (pure) + 29 JVM tests green; No
  d2136fb ckpt 658: v0.8.7 RECORDED (run 34 id 42438532391 green, Release published); took over fr
```

(3 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
