# CHECKPOINT 750 — read me first, then TASKS.md

**Written:** 2026-11-09T02:00:14Z · **tests:** all 5 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/kind-pasteur-ccqmmc` · **builds on:** `9dd31be` (this checkpoint is the commit after it)

## Just done
gate done: all sections green except legacy flake (W5/reset2/W3b alternate on identical product code; TASKS BS1h); probing secret with android.yml full_build=false

## Do this next
read android.yml gates run steps: keystore step ran = secret set -> bash ship.sh 'v1.0' then android.yml full_build=true; else ask user to add SIGNING_KEYSTORE_BASE64

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M TASKS.md

## Last ten checkpoints
```
  50decf5 ckpt 749: gate run #3 (legacy,x1,ad,pa,uf,nq,fp): 2 FAIL in legacy (reset2 shade clear, 
  ef09827 ckpt 748: gate: runs #1 #6 green; #5 (base,e1,t,bs,ao,hp) 4 FAIL HP5/6/8/11 = scenario b
  f87f74e ckpt 747: 6 emulator groups dispatched ~01:13 UTC on main 3af5ec5 (full gate); README fo
  3af5ec5 ckpt 746: v1.0 review (base bf0615b = v0.8.11 state; 206b3a8 absent in fork): 10 finding
  46ad59b ckpt 745: v1.0 part 2: in-app updater (UpdateRules+test, Updater via DownloadManager, Up
  f2e37ce ckpt 744: v1.0 part 1: stampDue wired (HomeCurtain.restampIfDue); applicationId app.orio
  9169d1a ckpt 743: BS1 written: all 15 answers + in-app updates, versions 1.0 +0.1, guide/feature
  dc521bc ckpt 742: BR1e answers: Owner=Quickstep, settings Owner-only, all customizations; consol
  e59c14e ckpt 741: BR1 written: profiles Owner / FBOP (plain PIN) / Rental, handshake->profile + 
  06ebb31 ckpt 740: BQ1 written: 8 pad/key handshakes chosen; handshake->profile map asked; nothin
```

(1 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
