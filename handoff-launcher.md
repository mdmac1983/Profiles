> **SUPERSEDED (2026-11-07 06:45 UTC): this file is the old active-memory handoff #3 and is OUT OF DATE (it says v0.8.2 is unreleased; it was released 02:44 UTC, and v0.8.3 is in its last emulator runs). Resume from `CHECKPOINT.md` and `HANDOFF.md` section 0g instead.**

# active-memory handoff: Launcher (Android 10 Go launcher: v0.8.1 released, v0.8.2 coded, reviewed and in its final gate)

**Handoff #3** · 2026-11-07 · Lineage: #1 (2026-11-05): resumed the cut-off Launcher work, shipped v0.7.1, first OTG cable session (helper unreachable, first-open crash found); #2 (2026-11-06): finished the cable session (permissions, accessibility fix, lock number and gesture proven on the real tablet), coded v0.7.2 (released by the other Claude account), took over from that account's cut-off session (takeover protocol written), built v0.7.4 on a separate branch (AD7a, AD5, AD4, AD8, PG3), brought the v0.7.3 gate to complete; between #2 and #3 (2026-11-06, other sessions): v0.7.3 to v0.8.1 were shipped (v0.8.0 = invisible patch cut from one real screenshot, v0.8.1 = battery-saver root cause fixed, patch fade, strict lock screen, automatic calibration; details in `HANDOFF.md` 0b to 0f); #3 (2026-11-07): after Tj tried v0.8.1 ("The power menu is 99% perfect. The timing is perfect, but the final wallpaper patch is very slightly too dark") built BA1 (the tablet measures the stock menu's real dimming itself) and BA2 (battery saver off on the lock screen, Quickstep's state restored only when safe), proved both on the emulator, had an independent code review (3 major bugs fixed), bumped to v0.8.2 (code 27) and started its final gate; the release is NOT cut yet

## 0. Instructions for Claude (read first)

You are continuing work from a previous chat. That chat is gone; this file is the complete context and the source of truth.

1. Read this whole file before replying.
2. Follow sections 3 (Style), 4 (Hard rules) and 5 (Corrections) in every reply, for the rest of this chat. They override your defaults.
3. Use the values in section 8 exactly. Never round, re-estimate, or "correct" them.
4. Do not suggest anything listed in section 7 (Changed / rejected) again unless the user brings it up.
5. Code word: None active. If the user types /amcodeword, start every reply with the phrase they choose (default "Yes Boss!").
6. Your first reply: at most 5 lines covering the goal, the current state, and the next step (section 11). Mention any files from section 13 that were not attached. Ask the questions in section 12 if there are any. End with "Ready to continue with <next step>?" Then wait for the user's go.

## 1. Mission
- **Goal:** Tj's custom home-screen launcher `com.tj.launcher` for ONE Android 10 Go tablet (MediaTek board `tb8168p1_bsp`). Java, sideloaded, built and signed by GitHub Actions, written by Claude over many sessions and several accounts. Repo: github.com/tjshea90/Launcher (PUBLIC). `main` mirrors every push (`tools/MIRROR_MAIN` = on). This session's branch: `claude/power-menu-wallpaper-brightness-g7588s`.
- **Done looks like:** (Tj's standing words) it must "look and function IDENTICALLY to the stock Quickstep v10", never crash, never lock Tj out, never need a factory reset. For this release: v0.8.2 published and recorded, then Tj checks on the tablet (a) that the one-time on-tablet measurement runs (two camera flashes, the stock power menu opens by itself for about two seconds and closes) and that the patch over the Screenshot spot is invisible at the END of the menu opening, (b) battery saver on the lock screen in the four cases of `research/TABLET_CHECKLIST_v0.8.2.md`.
- **Why it matters / context:** Claude can be cut off by usage caps at any moment. Everything is saved in the repo: `CHECKPOINT.md` (where we stopped, "Do this next"), `TASKS.md` (job list + evidence, Tj's words verbatim), `HANDOFF.md` (one-page entry, 0d = TAKEOVER, 0g = this release), `INBOX.md` / `OUTBOX.md` (Tj's raw messages / Claude's replies), `CLAUDE.md` (working agreement + lessons), `BUILDLOG.md` (releases). The SessionStart hook prints the briefing; `bash tools/install-hooks.sh` FIRST.
- **Right now (2026-11-07 ~01:55 UTC):** v0.8.1 is the last release. v0.8.2 is code-complete on commit `dced3e9` (versionCode 27, versionName 0.8.2); later commits change only tests, the emulator scenario and docs. Its final emulator gate is running (section 8). Nobody has run `ship.sh` or `android.yml` for v0.8.2. Tj said he will send a photo of the wallpaper patch later (TASKS BA4).

## 2. About the user (as relevant to this work)
- Tj (GitHub `tjshea90`), owner of the project and of the tablet. Not a developer: works from a phone/tablet and a Claude chat; wants plain-language answers, a clear "what I did / what you must do"; notices releases himself; sends phone screenshots and photos instead of text.
- Looks at the real tablet very closely (sees a patch that is "very slightly too dark ... but barely"): an exact, invisible result is the bar ("100% identical").
- Uses several Claude accounts/devices; usage caps interrupt sessions; prefers longer, thorough sessions over cheap shortcuts.
- Tj's other apps target Android 16; THIS project targets Android 10 (API 29): the project's `CLAUDE.md` wins.
- Hardware (Tj, 2026-11-06): "Mt8168 / Android 10 go / 2gb ram / Armv7".
- Pronouns for Tj are not stated: use "Tj" or "you", no gendered pronouns.

## 3. Style & communication
- **Language:** English.
- **Tone:** plain, direct, short; say plainly what was and was not released or proven; no jargon toward Tj (explain a technical term in a few words).
- **Reply length:** concise; during a LIVE cable session ONE short step per message, then WAIT. Answer every open message of Tj's and re-list the open ones until done. When the harness says the user has not heard from you, give a one- or two-line status and keep working.
- **Formatting:** the Release link as plain tappable text on its own line, never in a code block; each command alone in a code block; in a cable session every command goes in the LAST message of the turn with no tool call after it.
- **Working style:** write facts to the repo first (`TASKS.md`, then `bash tools/ckpt.sh "did" "next"` with SHORT messages: run listings echo the head commit's message); a new request from Tj goes into `TASKS.md` in his words BEFORE any code; hand waiting to Actions and a background timer; read CI results cheaply (job-log tail); get an independent fresh-eyes review before a release.
- **Avoid:** long explanations or options inside a cable step, gendered pronouns for Tj, deciding an OPEN item for Tj, polling CI in a loop, copying Tj's photos into the repo.

## 4. Hard rules (word for word)
1. "Remember these photos for reference at all times, even in a new Claude session." (Tj, 2026-11-02. The stock photos in `research/reference/stock/INDEX.md` are the ground truth for the look: 5 columns, 8 rows, no dock, no caret, row pitch 113 px, scrim 50% white; measure before saying a look is matched.)
2. "make sure claude can save and use all necessary information from the tablet when connected." (Tj, 2026-11-05)
3. "This is way too much information for my short time with the cable. Give me short step by step tasks and wait for me" (Tj, 2026-11-05; live cable session: ONE short step per message, every command in the LAST message of the turn)
4. "SAFETY (overrides everything): before ANY release be highly confident it breaks nothing; failsafe that removes every modification and restores Android to its pre-launcher state. A friend's launcher glitched the lock screen (could not log in) and brought back all quick-settings tiles: that must never happen. Must be revertible if anything goes wrong." (TASKS.md L10, Tj's words; code that runs on the lock screen fails OPEN and never blocks an unlock)
5. "It is important that this project doesn't break anything or require me to factory reset my tablet." (Tj, 2026-11-05)
6. "GitHub actions should do all the app compiling and apk making using their usage when possible. Claude codes the app, GitHub makes the apk." (Tj's rule on all repos). Never add a `push:` branch trigger to a workflow; local Gradle only via `bash tools/gradle.sh <task>`, one at a time, never `./gradlew`.
7. "If Tj sends several messages while a task is running: keep doing what you are already doing, add each new message as the next step (write it into TASKS.md in Tj's words, ckpt), never drop or forget one, and answer every one of them, including ones from earlier in the conversation that are still open (re-list them at the end of a reply until they are done)." (CLAUDE.md, 2026-11-03)
8. "Do not decide an OPEN item for Tj" (BRIEF.md): resolve it with Tj, one real decision at a time.
9. "Tj's PHOTOS stay OUT of the repo (Tj, 2026-11-06, permanent)." The repo is public and autosave runs `git add -A` after every tool call: view a photo where it was delivered (`/root/.claude/uploads/...`) or in the scratchpad, never copy it into the repo; describe it in words with NO name, ID or number.
10. Never commit a key, the keystore `app/sideload.jks` or `local.properties`; never regenerate the keystore; never `curl` api.github.com (a hook blocks it: do not work around hooks); GitHub MCP tools only, no `gh` CLI.
11. "FIRST ACTION OF EVERY SESSION — install the hooks, don't assume they exist": `bash tools/install-hooks.sh`, then `bash tools/install-hooks.sh --check`. Checkpoint after every step. A new request from Tj goes into `TASKS.md` in Tj's words first; tick a box only when written, tested and committed, and name the test.
12. Files from the tablet: `bash tools/tablet-intake.sh <file|folder|zip>`, read `research/tablet/LATEST.md` first; never commit a raw report or a tablet screenshot by hand.
13. Release routine: `bash ship.sh "note"` → trigger `android.yml` with `full_build=true` on the branch → confirm the Release (`get_release_by_tag`) → `bash tools/record-release.sh vX.Y.Z "note"` only after the run is green and BEFORE bumping the next version → give Tj the Release link. Ship one version at a time, only from a commit whose product code (`app/`, `launcher3/`, `phoneguide/`) is what the gate ran on.
14. "Only keep reasonable tests from now on and make them as fast and efficient as possible" (Tj, 2026-11-06): the emulator gate is TIERED (`bash tools/gate.sh <base-rev>`); never dispatch an empty `sections`; fix a flaky or order-dependent test instead of rerunning the gate.
15. `tools/MIRROR_MAIN` stays `on`: "NEVER switch it off again without writing WHY in TASKS.md and switching it back on in the same session; after a push check `git ls-remote origin refs/heads/main` equals your tip."
16. "Never write `Settings.Global low_power` to switch battery saver ON" except through the guarded restore of v0.8.2 (`BatterySaverPolicy.restoreRefusal`): a direct 1 is consistent with Android 10's state machine only while it is still in MANUAL_ON / AUTOMATIC_ON.
17. Tj, 2026-11-07 00:07 UTC (verbatim): "make the lock screen always have battery saver turned off permanently when the lock screen is up, no matter which launcher was on before the screen lock. If I go to the custom launcher, have the battery saver remain turned off, but if I unlock to quickstep, keep the same battery saver state as the last time I was in quickstep. Only implement these battery saver features if you are confident they can work and not break anything"
18. Tj, 2026-11-07 00:07 UTC (verbatim): "The power menu is 99% perfect. The timing is perfect, but the final wallpaper patch is very slightly too dark and I can see the square patch, but barely. Try to make the brightness of the square patch match the darkened lock screen after the side key press 100% identical." (The fade TIMING is perfect: do not touch `PatchFade`.)
19. User preference: "Review all available plugins, skills, and connectors at your disposal. Use any plugins, skills, and connectors freely if they can improve app building, app UI and theming, efficiency and accuracy of code, efficiency of Claude usage, or anything else to improve Claude code for building android apps. Ignore skills, plugins, and connectors if they contradict one another; choose the best one. Most of my apps are for Android 16." (this project: Android 10)
20. User preference: "All projects should have ladder systems that auto save all data frequently to GitHub, and a new Claude code session can cold resume the project with no data or progress being lost. ... Assume Claude can be interrupted at any time, and plan for a resumption protocol."
21. User preference: "I prefer longer sessions and more usage if it makes a better product and fixes bugs, but efficiency and saving Claude usage where possible."
22. Takeover rule (HANDOFF.md 0d): ONE actor only runs `android.yml` and `tools/record-release.sh`; a sibling session's newest commit is its heartbeat; check CI runs and `get_release_by_tag` BEFORE dispatching anything; never double-build or double-record.
23. Hooks are the user's safety net: PreToolUse hooks block destructive-looking text in Bash commands (for example literal git force/reset words in a heredoc) and variable `rm -rf`; write such text with the Edit/Write tools; never work around a hook.
24. Tool habits that cost time before: run lists need `perPage` in camelCase (`per_page` is ignored); `get_job_logs` answers 404 until the job ENDS (a cheap "done yet?" probe); foreground `sleep` is blocked: use Bash `run_in_background` with `until [ "$(date -u +%H%M)" -ge HHMM ]; do sleep 5; done` (the sandbox clock and `sleep` do not run at the same speed: wait on `date`, not on a sleep length); the "completed" notice of a background wrapper proves nothing about the CI run; `tools/gate.sh` knows which sections a change needs.

## 5. Corrections log
| # | Claude did | The user wanted |
|---|---|---|
| 1 | Wrote the exact command only in text placed before a tool call, so it did not show on Tj's screen | Tj: "You didn't tell me the one line" → every command goes in the LAST message of the turn, with no tool call after it |
| 2 | Wrote "send a screenshot of the two diagnostic lines above" without repeating them | Tj: "What two diagnostics" → repeat the exact commands in the message |
| 3 | Sent long messages with findings, options and several commands while Tj was at the cable | Tj: "This is way too much information for my short time with the cable. Give me short step by step tasks and wait for me" → one step per message, then wait |
| 4 | Told Tj that notification access was a missing permission to grant | (own audit) On a low-RAM Go device Android refuses managed services for non-whitelisted packages; nothing to grant; the guide must not offer it |
| 5 | Put literal git force/reset words in a Bash heredoc | (hook rule) write doc text with Read + Edit/Write |
| 6 | Called the run-list tool with `per_page` | use `perPage` in camelCase, or the answer is about 35 KB of old runs |
| 7 | Merged `INBOX.md` in text mode and changed the CRLF line endings of one of Tj's messages | merge `INBOX.md` BYTE-EXACT in binary mode: only insertions |
| 8 | (found by tests, 2026-11-07) `pt` phase C pressed the power key again right after the helper had started measuring: "stopped: the screen went off" | no extra lock in that phase: the restarted helper measures on the plain lock screen by itself |
| 9 | (found by run #266) a scenario wrote `tj_features.xml` behind the RUNNING app; the app's next preference write (the new observer) saved its old in-memory copy over the injected state (BS4 failed) | `am force-stop` the app BEFORE writing its preference file |
| 10 | (found by `ScrimMeasureTest`) a flatness test comparing only opposite neighbours let one-pixel strokes through | also test the centre against the average of its neighbours (`ScrimMeasure.flat`) |
| 11 | (independent review, 2026-11-07) three real bugs in the new code: the Quickstep memory wiped when a handshake was armed and abandoned; a restore after the person's own "off" in Settings (the old "Turn off does nothing" bug again); a stray BACK that could close the PIN pad | all fixed: only a Quickstep session that begins UNLOCKED counts (`noteQuickstepBegins`); any non-own `low_power` change while unlocked ends the hold; BACK only to a verifiably present stock menu over a still plain lock screen |
| 12 | (found by diagnostics, 2026-11-07) blamed `ps` failures on the product | AZ4 root cause: a stale `tj_restore.xml` snapshot restores `lock_screen_show_notifications`=1 when a later section turns `lock_quiet` off: notification cards sit over the patch slot. Test hygiene only; `ps`/`pa`/`pt` now delete the snapshot first |

## 6. Decisions
User decisions and standing facts first, then Claude's design choices (marked) that Tj has not yet seen on the tablet.
| Decision | Why |
|---|---|
| Battery saver on the lock screen: always off, from any launcher; custom keeps it off; a gesture unlock into Quickstep restores Quickstep's last state; only if confident and nothing breaks (Tj, 2026-11-07) | Tj's request; the confidence condition is binding (section 4 rule 17) |
| The lock-screen patch end level must be "100% identical" (Tj, 2026-11-07) | the square is "barely" visible at 0.80 |
| A (automatic calibration of the patch by the helper itself, no step for Tj) (Tj, 2026-11-06) | AW5; the screenshot stays in the gallery (Tj accepted) |
| Photos of the tablet stay out of the public repo (Tj, 2026-11-06); four old lock-screen screenshots from 2026-11-03 stay tracked until Tj decides (TASKS AF2) | privacy |
| `tools/MIRROR_MAIN` = on, main follows every push (2026-11-06, after Tj's "Why are my prompts and Claude findings not being saved?") | a frozen `main` made new sessions start from a stale state |
| Claude's design, NOT yet confirmed by Tj: the tablet measures the stock scrim itself (bare frame, helper opens the stock menu, second frame, BACK; per-channel scale + offset), default ON in the app with a Control switch, OFF in debug builds | no guess can be 100 % identical; the emulator's own scrim is exactly black x 0.80, the tablet's evidently is not |
| Claude's design, NOT yet confirmed: restore Quickstep's saver ONLY after a gesture unlock and ONLY when Android's state machine cannot have been reset (not charging, no plug-in / level rise since the hold, same boot, not changed by the person meanwhile, rate limited); in any doubt do not restore | a direct low_power=1 on a reset state machine is the AZ2 bug ("it says it turned off but it never does") |
| Claude's design: the fade timing of v0.8.1 stays untouched | Tj: "The timing is perfect" |
| Claude's design: an app occluding the keyguard (alarm, camera) or an open shade is NOT checked before the measurement (accepted low risk, TASKS BA5 m4) | a guard on the active window's type could silently disable the whole measurement on this ROM and cannot be tested here |
| v0.8.2 ships from commit `dced3e9`'s product code; later commits are tests/scenario/docs only | rule 13 |

## 7. Changed / rejected
- `DIM_OPEN` 0.79 (v0.8.0) → 0.80 (v0.8.1) → the MEASURED per-channel transform (v0.8.2; 0.80 stays the fallback).
- AG1 "battery saver forced ON in Quickstep" (v0.7.5 to v0.8.0, retired in v0.8.1 as the AZ2 cause) → v0.8.2 restores Quickstep's own state, but only through the guarded restore.
- Handoff #2 rule "do NOT push to `main` and keep `MIRROR_MAIN` off until v0.7.3 is released" → obsolete: `MIRROR_MAIN` is on and `main` follows every push.
- Handoff #2's "v0.7.3 / v0.7.4 not yet released" → all released; v0.8.1 is the last release.
- N3a (all entrances to Quickstep removed) → AD4 hidden, switchable entrances (shipped); the hidden Solitaire box → default OFF.
- One "everything" emulator run as the gate → tiered gate (`tools/gate.sh`, balanced parallel runs of about 20 minutes).
- ❌ Hedge: raise the default `DIM_OPEN` for Tj's tablet by 1 % (rejected: a guess; the measurement is the answer).
- ❌ Ask Tj for two manual screenshots (bare + menu open) instead of an automatic measurement (rejected for the default; Tj's photo is still welcome as a check).
- ❌ A model of the wallpaper for the patch (never exact; real screenshot pixels instead, v0.8.0).
- ❌ Restoring the saver on ANY unlock or at any Quickstep entry (only the gesture unlock; non-lock entries just record Quickstep's live state).
- ❌ "Notification access is a missing permission", "TCP failed because INTERNET is missing", "DuraSpeed keeps the accessibility service from binding", "the tablet is LineageOS", RGB_565 wallpaper bitmaps, R8 on, merging `dependabot/*` and orphan branches (all from earlier handoffs, still rejected).

## 8. Data & facts (exact)
**Repository and releases**
| Item | Value |
|---|---|
| Branch | `claude/power-menu-wallpaper-brightness-g7588s`, `main` follows (verify `git ls-remote origin refs/heads/main`); CHECKPOINT number 569 at 01:50 UTC, later auto-checkpoints add commits |
| Shipping commit of v0.8.2 | `dced3e9` (`versionCode = 27`, `versionName = "0.8.2"` in `app/build.gradle.kts`); `git diff dced3e9 --stat -- app launcher3 phoneguide` was EMPTY at 01:50 UTC (tip `098b1d0`) |
| Last release | v0.8.1, code 26, published 2026-11-06 23:44 UTC, https://github.com/tjshea90/Launcher/releases/tag/v0.8.1 , `android.yml` run 28, BUILDLOG line 23:52Z |
| v0.8.2 | NOT released; `get_release_by_tag v0.8.2` must be 404 before you dispatch; gate base for `tools/gate.sh` = `7d85b50` (the gated v0.8.1 code) |
| Local checks | `bash tools/gradle.sh testDebugUnitTest :app:lintDebug` green on the tree of `dced3e9` (about 560 unit tests; new: `ScrimFitTest` 19, `ScrimMeasureTest`, `BatterySaverPolicyTest` 47+); `bash tools/test_handoff.sh` 38 passed / 0 failed |

**Emulator evidence (workflow `emulator.yml`, ref = the branch)**
| Run | Sections / commit | Result |
|---|---|---|
| #265 id 42319244391 | `bl` on 102c1f9 | 21 PASS / 0 FAIL (saver off at the lock from Quickstep and custom; kept off when something turns it on; gesture unlock restores it: state machine `mState=2`; the real Settings 'Turn off now' then works; plug-in while locked = not restored) |
| #266 | `bs,lq,ac` on 102c1f9 | 50 PASS / 2 FAIL (BS4a, BS4c: the TEST wrote prefs behind the running app; fixed) |
| #268 id 42320933665 | `bs` on 11b1655 | 13 PASS / 0 FAIL |
| #267 | `pt` | failed in the ffmpeg apt install step (mirror stall, 4 min): infrastructure |
| #269 id 42321050689 | `pt` on 0eb6250 | 12 PASS / 3 FAIL: phase C interrupted its own measurement ("stopped: the screen went off"): test sequence fixed |
| #272 id 42321871716, job 128248073003 | `pt` on b925d62 | 15 PASS / 0 FAIL: helper note `MEASURED scale 0.800 / 0.800 / 0.800, offset 0.0 / 0.0 / 0.0 (30919 samples, rms 0.5)`; stored `3|r0|600x1024#d#0.7997,0.7999,0.7999#0.0299,0.0035,0.0030#30919#0.4842`; the stock menu closed again; end level (patch / surround - 1) measured look -0.0012 vs generic 0.80 look -0.0023; fade25 worst mismatch while opening 0.0229 vs 0.1918 for the old one-step dim |
| #270 id 42321616646 (job 128247261992) | `n,o,t,x,ac,bs,lq` on 5e89113 | success |
| #271 id 42321619870 (job 128247271172) | `x1,e1,bl,ps,rb` on 5e89113 | 48 PASS / 4 FAIL, all in `ps` B/C (AZ4 mechanism, section 5 row 12); x1, e1, bl, rb passed |
| #273 id 42322925817 (job 128251471823) | `base,v,pa,uf,last` on 3056094 | success |
| #274 id 42324039079 | `ps` alone on 9281c6d | success; #275 id 42324041525 (job 128255069573) `bl,ps` FAILED and its `PS B: DIAG` lines found the AZ4 root cause |
| FINAL GATE on `dced3e9` | #276 id 42324558796 (job 128256718619) `{n,o,t,x,ac,bs,lq}`; #277 id 42324561598 `{base,v,pa,pt,uf,last}`; #278 id 42324581655 `{x1,e1,bl,rb}`; #279 id 42324585039 (job 128256802556) `ps` = SUCCESS | #276 to #278 were still running at 01:50 UTC (get_job_logs = 404 until each job ends; `list_workflow_jobs` conclusion `success` = no FAIL) |
| Confirmation of the AZ4 fix | #280 id 42325739219 `bl,ps,pa` on 2675d47 | running at 01:50 UTC |

**BA1: the on-tablet scrim measurement (v0.8.2)**
- Model: stock dialog background = flat scrim: out = (1-a)*L + a*C per channel = s*L + o; AOSP emulator: s = 0.80, o = 0 (measured 0.7997 / 0.7999 / 0.7999, offsets 0.00 to 0.03).
- Files: `ScrimFit.java` (robust fit, gates, storage `patch_scrim_r<rot>` = `sig#d|n#s,s,s#o,o,o#samples#rms#when`), `ScrimMeasure.java` (decision, limits, sampling grid, failure counting), stage in `TjAccessibilityService` (`maybeMeasureScrim`, `scrimStep`, `scrimIngest`, `scrimCloseStep`, `scrimTeardown`, `setOpenLook`), Control rows (switch, 'Measure again', status), report line `menu dimming measurement (BA1)`, research note `research/SCRIM_MEASUREMENT_2026-11-07.md`.
- Constants: grid every 4th pixel (2nd if too few), flat test FLAT_MAX 60 (opposite-neighbour sum AND |4c - sum of neighbours|); excluded: top 3.5 % and bottom 6.5 % bands, the stock panel + 18 dp, the patch window + 6 px, the lock number rect (x 190-410, y 895-938 on 600x1024); gates MIN_SAMPLES 3000, MAX_RMS 3.0, MIN_SPREAD 12, MIN_INLIER_FRACTION 0.55, 0.55 <= s <= 0.95, |o| <= 45, the three s within 0.05; timings SHOT_GAP_MS 2400, OPEN_SETTLE_MS = PatchFade.SETTLE_MS + 870 = 1200, SHOT_WAIT_MS 8000, MENU_WAIT_MS 4000, CLOSE_STEP_MS 900, CLOSE_TRIES 5, STAGE_LIMIT_MS 45000, settle after wake 3500, MIN_GAP_MS 45000, MAX_PER_LOCK 2, MAX_PER_DAY 4, MAX_FAILURES 2 per lock screen signature.
- Keys: `scrim_measure` (default ON in the app, OFF when the build is debuggable), `patch_scrim_r<rot>`, `scrim_fail_r<rot>`, `scrim_tries`, `scrim_note`. PatchFade: OPEN_MS 300, LEAD_MS 10, FRESH_MS 600, SETTLE_MS 330 (unchanged).

**BA2: battery saver on the lock screen (v0.8.2)**
- Files: `BatterySaverPolicy.java` (pure rules: `lockOffWanted`, `keepOffWanted`, `qsStateAtHold`, `qsStateLive`, `holdStillValid`, `levelRose`, `sameBoot`, `restoreRefusal`), `BatterySaverGuard.java` (`onLock`, `onLockAsync`, `onSettingChanged`, `onUnlock`, `onUnlockAsync`, `onPowerEvent`, `noteQuickstepBegins`, `lockStatus`), hooks in `Mode.set`, `TjAccessibilityService` (`lockQuietNow`, both unlock branches, the power receiver), Control switches, report line `battery saver on the lock screen (BA2)`.
- Keys: `profile_battery_saver_lock_off` and `profile_battery_saver_lock_restore` (default ON), memory `bs_qs_on`, `bs_held`, `bs_held_at`, `bs_held_boot`, `bs_last_level`, `bs_plug_seen`, `bs_qs_real_start`, `bs_restores`, `bs_lock_note`. Limits: MAX_LOCK_WRITES 6 per 60 s, RESTORE_QUIET_MS 6000, BOOT_TOLERANCE_MS 120000, LEVEL_RISE_PCT 3, MAX_RESTORES 3 per 5 min.
- Android 10 facts (AOSP, `research/AOSP10_BATTERYSAVER_AND_POWERMENU_2026-11-06.md`): `MANUAL_ON` exits only by plug-in or a manual off; a boot starts with the saver off; direct write of 0 is always consistent; restoring dark theme may redraw the Quickstep home once after the gesture unlock.

**Tj's request of 2026-11-07 00:07 UTC** is in section 4, rules 17 and 18 (verbatim); "I will send a photo of the wallpaper patch later if it helps" (~01:10 UTC) = TASKS BA4.

**Tablet** (first real report `research/tablet/2026-11-05/`; still valid)
| Item | Value |
|---|---|
| Model | `alps 711 / tb8168p1_bsp`, Android 10 (API 29), patch 2021-07-05 |
| Fingerprint | `alps/vnd_tb8168p1_bsp/tb8168p1_bsp:10/QP1A.190711.020/1719850017:user/release-keys` |
| SoC / ABI | MediaTek MT8168 (4x Cortex-A53, Mali-G52 MP1); 32-bit ARMv7 (armeabi-v7a); the app has no native code |
| Build / security | `user` build, release keys, SELinux Enforcing, locked bootloader, unencrypted |
| Memory / screen | low-RAM Go device, 2 GB RAM, memory class 128 MB; 600x1024 portrait (report: 600x973 usable) at 170 dpi, font scale 1.2; max users 1 |
| System apps | MtkSettings, MtkSystemUI, MtkLauncher3GoIconRecents (stock launcher), MtkBrowser; SwiftKey 9.13.16.5 |
| Facts proven in the cable sessions | `allow_listener` leaves `enabled_notification_listeners` null; the shell helper is unreachable (sockets blocked) and dies within minutes (HLP1): the real tablet has NO shell helper, so everything must work through the accessibility service + WRITE_SECURE_SETTINGS |
| Nothing of v0.8.2 has run on the tablet | Tj's last test was v0.8.1 (2026-11-07 00:07 UTC) |

**CI mechanics**
- `emulator.yml` input `sections`; one-section runs about 4 to 10 minutes, a tier group about 20; the `RESULTS` block is the last ~45 lines of the job log; the `pt` job installs ffmpeg (can stall 4 minutes: re-dispatch).
- `android.yml` ("Build APK"): `full_build=true` on the branch: handoff self-test, versionCode gate against BUILDLOG.md, unit tests, lint, build, sign, certificate check, publishes the Release (the tag is created server-side).
- `tools/record-release.sh vX.Y.Z "note"` reads versionCode from `app/build.gradle.kts` and refuses if the build file is already bumped.
- AD4 entrance rules (shipped): page flick 6 alternating swipes (switch `entrance_flick`, default ON); Auto-rotate tile x5 in 10 s (`entrance_rotate_tile`, default ON); charger in/out/in within 8 s (`entrance_charger`, default OFF); resume switch `resume_qs_app` default ON.

## 9. People, terms & names
- **People:** Tj = the user and owner. "The other session / account" = another Claude account's sessions (they built v0.7.2 and v0.7.3). The independent code reviewer = a fresh-eyes agent (`ship:ship-code-reviewer`).
- **Terms & nicknames:** Quickstep = the stock launcher mode; custom = this launcher's mode; "the gesture / handshake" = the secret lock-screen key pattern (`UnlockMethods`) that unlocks into Quickstep; "plain PIN" = unlock into the custom launcher; "the patch" = real screenshot pixels drawn over the Screenshot entry of the stock power menu (rests invisibly, dims with the menu); "the scrim" = the stock dialog's dark background layer; "hold" = the saver is off BECAUSE of the guard; "the helper" = `ShellServer` (shell uid, started over adb; absent on the real tablet) vs "the accessibility helper" = `TjAccessibilityService`; "the breaker" = `CrashGuard`; "gate A / B / C" = parallel emulator runs of a release gate; DuraSpeed = MediaTek's background-app manager.
- **Task IDs in TASKS.md:** BA0 to BA5 (this release: BA1 patch end level, BA2 saver, BA3 ship, BA4 Tj's photo, BA5 review), AZ1 to AZ4 (flicker, battery saver root cause, triage, AZ4 test hygiene = solved), AW1 to AW6 (strict lock screen), AX1, AY1/AY2, AM1 (real-pixel patch), AU1 to AU8 (review items), AD1 to AD8, AC1 to AC9, RS1a to c, CBR2, AF2, AE6, HLP1, MBX1, L10.
- **Emulator sections:** base legacy x1 e1 y2 b6 b4 n n6 o t x v ka ac bs bl lq ps pa pt ad se rt pf im ao rg uf rb last (new in v0.8.2: `bl`; `pt` phase C). `tools/gate-map.txt` is the data behind `tools/gate.sh`.
- **Files:** `CHECKPOINT.md`, `TASKS.md`, `HANDOFF.md` (0d TAKEOVER, 0g this release), `CLAUDE.md` (lessons of v0.8.1 and v0.8.2), `INBOX.md`, `OUTBOX.md`, `BUILDLOG.md`, `BRIEF.md`, `research/TABLET_CHECKLIST_v0.8.2.md`, `research/SCRIM_MEASUREMENT_2026-11-07.md`, `research/AOSP10_BATTERYSAVER_AND_POWERMENU_2026-11-06.md`, `research/tablet/LATEST.md`, `handoff-launcher.md` (this file).
- **Tools:** `tools/ckpt.sh`, `tools/install-hooks.sh`, `tools/resume.sh`, `tools/test_handoff.sh`, `tools/gate.sh`, `tools/gradle.sh`, `tools/record-release.sh`, `tools/tablet-intake.sh`, `tools/photo_rectify.py`, `ship.sh`, `tools/emulator_scenario.sh`.

## 10. Work state
| Item | Status | Version / location | Notes |
|---|---|---|---|
| v0.8.1 | final, released + recorded | tag `v0.8.1`, BUILDLOG.md | Tj tried it on the tablet |
| BA2 battery saver on the lock screen | coded, emulator-proven (#265, #268), review fixes applied | commit `dced3e9` | the review fixes (M1, M2) are covered by the final gate runs, not by a dedicated new emulator check |
| BA1 on-tablet scrim measurement | coded, emulator-proven (#272), review fixes applied | commit `dced3e9` | cannot be verified here on the real scrim: only Tj's tablet shows whether the tablet's scrim is not 0.80 black; if it measures ~0.80/0, the remaining mismatch is NOT the scrim (see `research/SCRIM_MEASUREMENT_2026-11-07.md` last row) |
| Final gate of v0.8.2 | RUNNING | runs #276, #277, #278 (+ #279 green, confirmation #280) | all must be success before `ship.sh` |
| AZ4 (pa PA 2 / ps B order flake) | root cause found, scenario fixed, confirmation #280 running | `tools/emulator_scenario.sh` | `ps`, `pa`, `pt` delete `tj_restore.xml` first |
| Independent review of BA1/BA2 | done: 3 major + 5 minor + nits, all fixed except m4 (accepted, documented) | TASKS BA5 | |
| Release v0.8.2 | NOT started | `ship.sh`, `android.yml`, `record-release.sh` | section 11 |
| Docs for the release | written | `HANDOFF.md` 0g, `CLAUDE.md` lessons, `research/TABLET_CHECKLIST_v0.8.2.md`, `research/SCRIM_MEASUREMENT_2026-11-07.md`, TASKS BA0 to BA5 | refresh `HANDOFF.md` 0g after the release |
| BA4 Tj's photo | waiting on Tj | TASKS | view in chat only, never in the repo |
| Open older items | open | TASKS | AY2 (confirm `git show origin/main:OUTBOX.md | tail -c 600` holds the last reply), AU5 (shade, tablet), RS1a to c, CBR2 (Tj's decision), AF2 (old photos in the public repo: Tj decides), AE6, HLP1 |

## 11. Next steps
1. **Next action: read the final gate.** `bash tools/install-hooks.sh` first; `git fetch`; then for each of run ids 42324558796, 42324561598, 42324581655 and 42325739219: `mcp__github__actions_list` `list_workflow_jobs` (`resource_id` = run id, `workflow_jobs_filter {"filter":"latest"}`, `perPage` 1): `conclusion` `success` = no FAIL; on `failure` read `get_job_logs` `return_content` true `tail_lines` about 70 (the `RESULTS` header lists every FAIL). (`ps` run 42324585039 is already green.)
2. If a gate section fails: decide product bug vs test order issue (diagnose, do not just rerun); fix; re-run only the affected sections; keep `git diff dced3e9 -- app launcher3 phoneguide` empty unless the fix is product code (then re-gate the affected sections and re-check the bump).
3. All green: `bash ship.sh "v0.8.2: <note>"` (commits, runs the handoff self-test, unit tests, lint, versionCode check, pushes), then dispatch `android.yml` (`mcp__github__actions_run_trigger`, `run_workflow`, `workflow_id` `android.yml`, `ref` the branch, `inputs {"full_build":"true"}`), wait with a background `until date` timer, require success, `get_release_by_tag v0.8.2` must show `Launcher-v0.8.2.apk`, then `bash tools/record-release.sh v0.8.2 "v0.8.2: the tablet measures the stock menu's real dimming so the patch ends level with the dimmed lock screen; battery saver is off whenever the lock screen is up and Quickstep's state comes back after a gesture unlock when Android cannot have lost track"`.
4. Tell Tj plainly (plain text, Release link on its own line): what is new; what he will SEE once (two camera flashes and the power menu opening by itself for about two seconds on the first plain lock screen after installing; switch in Launcher control > Power menu "Measure the menu's real dimming"); the battery-saver behaviour and its limits (no restore while charging or after a plug-in/restart; a one-time Quickstep redraw when the saver comes back because the dark theme switches); what to send back (the `Menu dimming measurement` line, report lines `menu dimming measurement (BA1)` and `battery saver on the lock screen (BA2)`, a straight photo of the open menu with exposure locked: viewed in chat only); the checklist `research/TABLET_CHECKLIST_v0.8.2.md`. Not proven on the real tablet.
5. Tick BA1 (parent), BA2, BA3 in `TASKS.md` with the proof, refresh `HANDOFF.md` 0g and `CHECKPOINT.md` (`bash tools/ckpt.sh "did" "next"`).
6. When Tj sends the photo or the report: `bash tools/tablet-intake.sh` for files; for a photo measure with `python3 -I tools/photo_rectify.py <photo> <scratchpad-dir>`; write what it decides into `TASKS.md` (BA4); if the tablet measured ~0.80 / 0 offset the cause is elsewhere: compare a fresh bare frame with the stored crop (lock screen changed since calibration, dark theme, notification cards).
7. Standing: answer every open message of Tj's; keep decisions for Tj visible (section 12); confirm AY2.

## 12. Open questions ⚠️
- Is Tj fine with the automatic, default-ON measurement that opens the stock power menu by itself once (two flashes, about two seconds)? (Claude chose ON with a Control switch; Tj has not seen it. Alternatives: default OFF with a 'Measure now' button, or ask Tj to take two screenshots.)
- Which version is installed on the tablet now? (Last known: v0.8.1, tried at 2026-11-07 00:07 UTC.)
- Occluding apps / open shade are not checked before the measurement (TASKS BA5 m4): accept (Claude's choice) or add a guard after Tj reports a problem?
- Standing DECISIONS waiting on Tj (TASKS.md): AF2 (old photos in the public repo: remove, make the repo private, or purge history?), CBR2 (a breaker trip must not destroy Tj's configuration), AE6 (MBX1 vs BATCH1, explain in plain words and ask yes/no), Power menu v2, SET1 go or no-go, AU5 (shade, tablet check), `profile_preclean` default (irrelevant while the tablet has no helper).
- If the final gate shows a failure that is not test order: which section and why (unknown until the runs finish).

## 13. Re-attach checklist
- [ ] Nothing to attach if the new chat is Claude Code on the repo tjshea90/Launcher: the SessionStart hook prints `CHECKPOINT.md`, `TASKS.md` and the INBOX/OUTBOX tails; this file is `handoff-launcher.md` in the repo root on branch `claude/power-menu-wallpaper-brightness-g7588s`.
- [ ] If the new chat has NO repo access: attach this file, `CHECKPOINT.md`, `TASKS.md`, `HANDOFF.md` and `research/TABLET_CHECKLIST_v0.8.2.md`.
- [ ] Tj's photo of the wallpaper patch (when he sends it): it arrives in the chat; never put it in the repo.
- [ ] This file replaces handoff #2 (it stays in git history).

---
<sub>Audit: 13/13 sections · 24 rules · 12 corrections · about 150 data points · secrets removed: none found (no key, keystore content, PIN, serial or e-mail is included; the keystore is only mentioned by file name) · generated by active-memory</sub>
