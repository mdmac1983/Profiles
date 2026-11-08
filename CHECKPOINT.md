# CHECKPOINT 725 — read me first, then TASKS.md

**Written:** 2026-11-08T21:04:30Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `1b3af08` (this checkpoint is the commit after it)

## Just done
v0.8.11 RELEASED + RECORDED (android.yml #38 id 42522618674, 206b3a8; https://github.com/tjshea90/Launcher/releases/tag/v0.8.11). TASKS BK1a/BK1b ticked (emulator proof), BK1g/BK1f written. Gate story: 4 red checks, none a product bug (UF2 tag-identical, CS-A3/HP4 flakes, BK1b check measured the wrong thing); tests fixed + re-proven solo. Reply to Tj written next

## Do this next
WAITING ON TJ: BK1d tablet check (research/TABLET_CHECKLIST_v0.8.11.md), BK1e: which unlock is slow / what is on screen / seconds + tablet report; DECISION home-looking curtain yes/no. Claude-owned and low urgency: BH3 (animation scales vs Clear data). Do NOT start the curtain before Tj answers

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  cad4ed9 ckpt 724: RELEASE BUILD dispatched ~20:56 UTC: android.yml main full_build=true on 206b3
  38f3671 ckpt 723: gated v0.8.11 (code 36) and pushed it: the handoff self-test, the full unit su
  7ae1ed7 ckpt 722: b6 samples #408 id 42519172669 / #409 id 42519175423 (head 1986107): quickstep
  f945d11 ckpt 721: b6 #407 id 42518553791: BK1b lock phases cover shown at 3 openings in BOTH sto
  f8e6d37 ckpt 720: scenario-only edits on main 540e4e5: cover_resumes helper (UF2 + CS-B5 cover-a
  6e87f60 ckpt 719: TRIAGE DONE. Solo runs on main: uf #400 (UF2 FAIL, same order as TAG v0.8.10 #
  6e0299f ckpt 718: GATE #397 read: 99 PASS 1 FAIL = UF2 (stock HOME .Launcher resumed after the E
  3d61f3e ckpt 717: GATE READ (head c4f47b5), all 3 runs ended RED at the scenario step: #398 id 4
  9963f0b ckpt 716: v0.8.11 release prep (no product change): versionCode 36 / 0.8.11 in app/build
  d35a776 ckpt 715: takeover (old session died 'Prompt is too long'); branch == main == c4f47b5; p
```

(5 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
