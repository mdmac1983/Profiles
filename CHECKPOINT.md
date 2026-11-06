# CHECKPOINT 499 — read me first, then TASKS.md

**Written:** 2026-11-06T09:12:21Z · **tests:** all 3 fast checks green (gradle suite: see ship.sh)
**Branch:** `claude/new-session-7a9vpi` · **builds on:** `4f54346` (this checkpoint is the commit after it)

## Just done
v0.7.7 (code 24) gated (all gate runs 204-207 green on 75748c5: rg, o/x/lq, n/ac/rb/last, base/x1/e1/ao; earlier b6/t/lq/legacy runs green), ship.sh passed, android.yml full_build dispatched on the branch at head 4f54346

## Do this next
1) wait ~3 min, then mcp__github__get_release_by_tag v0.7.7; if published: bash tools/record-release.sh v0.7.7 <the ship note: battery saver tile no longer fought + lighter helper in the shade; Wallpapers opens the launcher's own copy of the stock picker without My photos; Recents closes cards of apps not in the drawer>; 2) tell Tj with the Release link as plain text + what to try (battery tile, picker, Recents) + what to send back; 3) THEN: git merge tmp-merge-test (AM1 + current), dispatch emulator.yml sections ps first, then the AM1 gate; 4) AQ1 / AR1 / AS1 (polish release v0.8.0)

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" comes before "Starting a session" — do that one first, or autosave stays off all session.)*

## Uncommitted right now
     M CHECKPOINT.md

## Last ten checkpoints
```
  4f54346 ckpt 498: gated v0.7.7 (code 24) and pushed it: the handoff self-test, the full unit sui
  8e4bc91 ckpt 497: v0.7.7 FULL DELTA GATE dispatched at 09:00Z on 75748c5: rg alone (earlier, dia
  75748c5 ckpt 496: gate on e69d979: ao PASSES in full (own picker opens: com.tj.launcher/com.andr
  8ba2f93 ckpt 495: Tj's final polish ask (speed / no flicker / restart survival test, the invisib
  e69d979 ckpt 494: AP1 CODED (446 unit tests green): RecentsFilter (new class; I first overwrote 
  a90b089 ckpt 493: AO1b CODED (builds, unit tests green): the AOSP legacy WallpaperPicker (androi
  ceb26c4 ckpt 492: gate reruns: e1 alone (run 198), base,x1,e1 on TAG v0.7.6 (run 199) and rb alo
  ea06d51 ckpt 491: Tj's newest message (battery tile only, Recents whitelist = drawer apps, wallp
  b7a531c ckpt 490: gate on 8fc7662: run 197 (b6,o,t,lq) GREEN; run 196 (base,x1,e1,ao,last) 40 PA
  ef34117 ckpt 489: v0.7.7 (AN1+AO1) in the emulator gate; AM1 merge resolved on local branch tmp-
```
