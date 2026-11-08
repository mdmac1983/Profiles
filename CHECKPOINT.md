# CHECKPOINT 667 — read me first, then TASKS.md

**Written:** 2026-11-08T03:47:24Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-yhyifc` · **builds on:** `b1399c9` (this checkpoint is the commit after it)

## Just done
v0.8.8 RELEASE build triggered 03:47 UTC: android.yml run 35 id 42444134135 on b1399c9 (full_build=true); gate complete: #341 nq 23 PASS, #342 (106 PASS, N2+N3 = native-crash flake) -> n alone #345 SUCCESS, #343 SUCCESS, #344 SUCCESS; Tier 0 681 tests + lint + handoff 38; ship.sh OK

## Do this next
when run 35 is green: get_release_by_tag v0.8.8; bash tools/record-release.sh v0.8.8 '<the ship.sh note in the ckpt 666 commit message>'; HANDOFF 0j (state line + v0.8.8 contents) + TASKS BH1b tick + BH1 stays open (BH1c waits on Tj); then reply to Tj: Release link on its own line, what changed, what cannot be done, the BH1c question, still-open list

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  b1399c9 ckpt 666: gated v0.8.8 (code 33) and pushed it: the handoff self-test, the full unit sui
  a3bc971 ckpt 665: v0.8.8 GATE read 03:41 UTC (head 73e65ff, product code final): #342 id 4244234
  0037ea5 ckpt 664: v0.8.8 GATE dispatched 03:21 UTC on 73e65ff (product code final, nq alone #341
  e09cc2b ckpt 663: BH1: v0.8.8 candidate (versionCode 33): version bump, research/NOTIFICATIONS_Q
  f7b6ee0 ckpt 662: BH1 review fixes (8 findings: helper fact in Quickstep, hold state in a Settin
  0095fec ckpt 661: BH1b step 3: emulator section nq (10 checks NQ0-NQ10) + testnotifier high-impo
  75f3927 ckpt 660: BH1b step 2: Launcher control switch + report lines + NotifSightings (10 JVM t
  afef3c6 ckpt 659: BH1b step 1: NotifQuietPolicy + NotifQuietHold (pure) + 29 JVM tests green; No
  d2136fb ckpt 658: v0.8.7 RECORDED (run 34 id 42438532391 green, Release published); took over fr
  6d26926 ckpt 657: v0.8.7 RELEASE build triggered 02:26 UTC: android.yml run 34 id 42438532391 on
```
