# CHECKPOINT 501 — read me first, then TASKS.md

**Written:** 2026-11-06T09:23:21Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `ba32855` (this checkpoint is the commit after it)

## Just done
AM1 PROVEN on the emulator (run 208, section ps: calibration from the service's own screenshot works, resting patch vs bare lock screen MAE 0, open stock menu vs dimmed lock screen MAE 0.002, a tap on the hidden spot takes no screenshot and closes the menu; the one FAIL was my log read before the settle time, fixed); AR1 first pass: Launcher control regrouped into 10 collapsible groups + fixed Restore footer, contradictions fixed (lock patch only with a cover mode, return-reset sub-switches only when its master is on, tile choosers only when per-launcher tiles are on, Solitaire setting moved to Everyday, cross-group texts fixed, patch status chip); new scenario sections uf (unlock flow from the event log) and the base check that the Control groups are on the page; 454 unit tests green

## Do this next
1) read run 209 (ae,v,x1,last on d3b49fb, job id via list_workflow_jobs resource_id 42193714641); 2) dispatch the gate for the new HEAD: ps (fixed), uf (new, never run), base (Control grouping check), plus whatever bash tools/gate.sh 3e7fbb5 lists; 3) AR1 inventory document research/SETTINGS_AUDIT.md (every switch: key, default, what it does, proof) + HANDOFF notes of what moved; 4) AQ1 report research/PERF_AUDIT.md from the uf numbers + AD8 lines; 5) ship v0.8.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  f66d336 ckpt 500: v0.7.7 SHIPPED and recorded; AM1 merged into the working branch (d3b49fb); emu
  97f6123 ckpt 499: v0.7.7 (code 24) gated (all gate runs 204-207 green on 75748c5: rg, o/x/lq, n/
  4f54346 ckpt 498: gated v0.7.7 (code 24) and pushed it: the handoff self-test, the full unit sui
  8e4bc91 ckpt 497: v0.7.7 FULL DELTA GATE dispatched at 09:00Z on 75748c5: rg alone (earlier, dia
  75748c5 ckpt 496: gate on e69d979: ao PASSES in full (own picker opens: com.tj.launcher/com.andr
  8ba2f93 ckpt 495: Tj's final polish ask (speed / no flicker / restart survival test, the invisib
  e69d979 ckpt 494: AP1 CODED (446 unit tests green): RecentsFilter (new class; I first overwrote 
  a90b089 ckpt 493: AO1b CODED (builds, unit tests green): the AOSP legacy WallpaperPicker (androi
  ceb26c4 ckpt 492: gate reruns: e1 alone (run 198), base,x1,e1 on TAG v0.7.6 (run 199) and rb alo
  ea06d51 ckpt 491: Tj's newest message (battery tile only, Recents whitelist = drawer apps, wallp
```

(8 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
