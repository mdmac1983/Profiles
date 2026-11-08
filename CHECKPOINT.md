# CHECKPOINT 717 — read me first, then TASKS.md

**Written:** 2026-11-08T19:44:20Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-2ax1r4` · **builds on:** `9963f0b` (this checkpoint is the commit after it)

## Just done
GATE READ (head c4f47b5), all 3 runs ended RED at the scenario step: #398 id 42514273811 job 128833774064 (o,ac,se,cs,fp,rb,last): 82 PASS 1 FAIL = CS-A3 (lock sweep with helper: Calendar task still in Recents after 'SWEEP lock: removed 1 of 1', calendar=2); FP ok (coldA first fade 8 frames worst 79 ms, PATCH warm-up logged); CS-B UNLOCKSUM 866 ms. #399 id 42514276324 job 128833782277 (e1,b6,n,v,ps,hp,hf): FAIL HP4 (core_mean +7.17, scale 0.750 fitted: patch drawn lighter than the stock scrim; NATIVE CRASH line before HP A); BK1b custom mode cover SHOWN 78 ms, quickstep mode NO cover in this run (b6 check not in the FAIL list). #397 id 42514271314 job 128833767456 (base,x1,x,ad,pt,uf): FAIL lines not read yet (tail cut)

## Do this next
read the FAIL lines of #397 (get_job_logs job 128833767456, tail_lines ~200); triage each FAIL against tag v0.8.10 (emulator.yml ref v0.8.10, same sections solo) before touching code; do NOT ship until every FAIL is explained; BK1b quickstep-mode lock-screen cover absent in #399 b6 although ckpt 714 says verified: re-check

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md
     M OUTBOX.md

## Last ten checkpoints
```
  9963f0b ckpt 716: v0.8.11 release prep (no product change): versionCode 36 / 0.8.11 in app/build
  d35a776 ckpt 715: takeover (old session died 'Prompt is too long'); branch == main == c4f47b5; p
  c4f47b5 ckpt 714: BK1 v0.8.11 candidate code complete: warm-up draw (per look kind), pencil via 
  894d06e ckpt 713: BK1c: Recents loop (no helper) fetches the window roots ONCE per look instead 
  203a5b1 ckpt 712: BK1: review fixes in (UNLOCKSUM emitted when the cover goes + Launcher drawn/r
  d9732b2 ckpt 711: BK1: run #392 (b6,fp on 1ffdfa2): FP warm-up line present, warm lock cycles fi
  1ffdfa2 ckpt 710: BK1: runs #390 (b6,fp on e415cd0) and #391 (cs baseline) read. FP: warm-up wor
  0770182 ckpt 709: BK1c: single-figure unlock summary built (UNLOCKSUM lock-log line + report key
  e415cd0 ckpt 708: BK1a/BK1b fixes built: (a) warm-up draw of the OPEN look once per patch window
  4c2e2e2 ckpt 707: BK1: measurement built, no product change yet: NEW section fp (films the FIRST
```
