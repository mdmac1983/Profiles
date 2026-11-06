# CHECKPOINT 503 — read me first, then TASKS.md

**Written:** 2026-11-06T09:36:08Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `0c30e13` (this checkpoint is the commit after it)

## Just done
runs 210/211/212 read: uf WORKS on the emulator (plain PIN in custom: one resume of our launcher, no stock; after a Quickstep session: 3 resumes of ours over 3.3 s, never the stock launcher or Contacts; Quickstep handshake: custom then stock in 260 ms, custom never after the stock one); ps: calibration + MAE 0 + open MAE 0.002 + tap swallowed again, the 'PATCH rest on' log was still missing because a patch window created already resting logs nothing: now logged at creation; base: Control page renders groups and summaries, only the header check failed (a fast fling skipped groups: now slow swipes + the missing headers are printed); v alone is green (V5 in run 209 was a flake)

## Do this next
rerun ps and base on the new HEAD, read them (list newest 2 emulator.yml dispatches; list_workflow_jobs + get_job_logs tail 62); then PERF_AUDIT.md (uf numbers), HANDOFF refresh, full gate and v0.8.0

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  795b51b ckpt 502: AM1 proven on the emulator (ps), AR1 regrouped + audit doc, AQ1 section uf wri
  675a10a ckpt 501: AM1 PROVEN on the emulator (run 208, section ps: calibration from the service'
  f66d336 ckpt 500: v0.7.7 SHIPPED and recorded; AM1 merged into the working branch (d3b49fb); emu
  97f6123 ckpt 499: v0.7.7 (code 24) gated (all gate runs 204-207 green on 75748c5: rg, o/x/lq, n/
  4f54346 ckpt 498: gated v0.7.7 (code 24) and pushed it: the handoff self-test, the full unit sui
  8e4bc91 ckpt 497: v0.7.7 FULL DELTA GATE dispatched at 09:00Z on 75748c5: rg alone (earlier, dia
  75748c5 ckpt 496: gate on e69d979: ao PASSES in full (own picker opens: com.tj.launcher/com.andr
  8ba2f93 ckpt 495: Tj's final polish ask (speed / no flicker / restart survival test, the invisib
  e69d979 ckpt 494: AP1 CODED (446 unit tests green): RecentsFilter (new class; I first overwrote 
  a90b089 ckpt 493: AO1b CODED (builds, unit tests green): the AOSP legacy WallpaperPicker (androi
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
