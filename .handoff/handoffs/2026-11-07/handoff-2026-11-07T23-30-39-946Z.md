---
date: 2026-11-07T23:30:39.946Z
project: Launcher
session: 14e1bbae
schema_version: 1
next_steps_count: 5
has_blockers: true
keywords: launcher handoff, recents sweep, clean start, lock screen patch, v0.8.5, tablet check, release routine, gate tiers
headline: Goal: custom Android 10 Go-edition home launcher (app.orionmd.profiles, Java, sideloaded port of AOSP Launcher3 plus TjAccess
---

# Session Handoff Snapshot
> **Generated:** 11/7/2026, 11:30:39 PM

## 🎯 High-Level Objective
* **Goal:** Goal: custom Android 10 Go-edition home launcher (app.orionmd.profiles, Java, sideloaded port of AOSP Launcher3 plus TjAccessibilityService) must feel stock-instant and trustworthy on low-RAM Go tablet. Intent: hidden apps stay hidden everywhere incl. Recents; lock-screen transitions flicker-free; custom home reachable from lock screen immediately; Quickstep (stock) may be slower. Claude writes code, GitHub Actions builds signed APK, many sessions over time, handoff must be lossless.

## 📌 Current State & Next Steps
* **Status:** DONE: v0.8.5 released, BE1-BE3, BD1, BD2 ticked in TASKS.md with proof. Final reply with Release link, limits, tablet-check request already sent to Tj. CHECKPOINT.md latest ckpt pushed; branch and main in sync. OPEN: BE4 = Tj tablet check, checklist at research/TABLET_CHECKLIST_v0.8.5.md. Not verified on real hardware. Why: release gate needs emulator plus JVM; tablet-only behaviour (PIN pad timing, Screenshot flash, no-helper cover time) only Tj can confirm.
* **Blocker:** none for Claude. Waiting on Tj: BE4 tablet check. Limits: instant unlock needs adb helper re-run after every tablet restart (helper dies at reboot; persistent helper needs adb over TCP plus cable). Without helper cover still about 1.5 s on emulator. NotificationListener cannot be enabled on this low-RAM device.
* **Next Action:** Nothing waits on Claude except new Tj requests. New Tj message: write into TASKS.md in his words, ckpt, then code.

### Remaining Queue
- [ ] When Tj returns BE4 results: bash tools/tablet-intake.sh <file>, then read research/tablet/LATEST.md, then read lock-log lines RESET, HOME custom home in front +N ms, CLEANLOCK, SWEEP, LOCKPREP no shell helper, PATCH order.
- [ ] If no-helper cover still too long: drop the Enter-tap cover in a clean start when pre-armed home is the only thing under keyguard. Persistent helper needs adb over TCP plus cable.
- [ ] If a hidden app still shows in custom Recents: check whether helper ran (SWEEP lines), then noteHiddenForSweep and RecentsFilter fallback.
- [ ] Older open items in TASKS.md: BB1, BB3c, BC4 (lock screen takes a screenshot by itself, not diagnosed), AU5, RS1a-c, CBR2, AF2 (Tj decides on 4 old lock-screen screenshots in public repo).

## 🛠️ Modified Files Delta
* app/src/main/java/com/tj/launcher/RecentsSweep.java: NEW membership sweep, policies HIDDEN_ONLY and ALL, systemLike guard, pending-request slot
* app/src/main/java/com/tj/launcher/ReturnReset.java: clean start, boot-clean flag, cleanAtLock, settleHome, mayHaveWork
* app/src/main/java/com/tj/launcher/TaskOrigin.java: finish complete mode
* app/src/main/java/com/tj/launcher/AppOrigin.java: usage cache
* app/src/main/java/com/tj/launcher/Mode.java, ProfileSession.java, TjHooks.java: small hooks
* app/src/main/java/com/tj/launcher/ShellHelper.java: removeTasks in chunks of 40
* app/src/main/java/com/tj/launcher/LockLog.java: addAsync
* app/src/main/java/com/tj/launcher/RecentsFilter.java: one-pass close, sweep trigger
* app/src/main/java/com/tj/launcher/TjAccessibilityService.java: ACTION_OUTSIDE hide, away guards, fastDecline fix, lockPrepare pre-create and sweep, recentsEmpty, boot-clean branch
* app/src/main/java/com/tj/launcher/ControlActivity.java: 2 new toggles
* app/src/main/java/com/tj/launcher/ReportState.java: allow-list keys
* app/src/test: RecentsSweepTest.java NEW, CleanStartDefaultTest.java NEW
* tools/emulator_scenario.sh: sections cs, cs2, pt phase H added
* tools/gate-map.txt: tiered gate map
* .github/workflows/emulator.yml: sections input
* research/RECENTS_AND_UNLOCK_AUDIT_2026-11-07.md: NEW audit doc
* research/TABLET_CHECKLIST_v0.8.5.md: NEW tablet check list for Tj
* HANDOFF.md: section 0h added
* CLAUDE.md: lessons sections for v0.8.5
* TASKS.md: BE0-BE4 entries
* BUILDLOG.md: v0.8.5 record
* app/build.gradle.kts: versionCode 30, versionName 0.8.5

## 🚫 Failed Approaches (DO NOT RETRY)
* Approach: no-helper complete clear relied on shade Clear all within 1.8 s -> Result: did nothing on emulator (runs 307 and 314 class of failures) -> Lesson: added recentsEmpty fallback closing every card via accessibility Close.
* Approach: recentsEmpty v1 returned after pressing Close without reaching its deadline -> Result: endless loop, reset hung with no result line -> Lesson: every polling loop checks deadline FIRST; max 3 presses per card; only cards with descriptions.
* Approach: scenario injected boot flag without timestamp -> Result: invalid under new 24 h expiry -> Lesson: scenario writes clean_pending_boot_at from device clock.
* Approach: scenario CS-A premise (restart process, expect old behaviour) -> Result: broken by correct new trigger -> Lesson: open Calendar then Contacts instead to test the trigger path.
* Approach: hard-coded 0.80 scrim and strict fade-vs-step comparator in section pt -> Result: failed on CI emulator (state-dependent 0.75 or 0.80, noisy film) -> Lesson: use fitted scrim value and relaxed comparator.
* Approach: big gate groups -> Result: emulator adb stall (~9 min) and GL checksum crash, one run cancelled at 50 min -> Lesson: split into smaller groups; infra flake, not product.
* Approach: pulling large pt log tails into main session -> Result: heavy token use -> Lesson: read CI logs with small tail_lines or a read-only subagent.

## 🔑 Crucial Context & Implicit Rules
* Stack: Java (not Kotlin), Android 10 API 29, Go-edition launcher; Tj's Android 16 preference does not apply here.
* First action each session: bash tools/install-hooks.sh. Checkpoint after every step: bash tools/ckpt.sh "did" "next" with short text.
* New Tj request goes into TASKS.md in his words before any code; a Tj message is answered, never dropped.
* Tj's photos never enter the repo (public repo, autosave git add -A). Describe photos in words without name, ID or number.
* Never commit secrets or keystore (app/sideload.jks gitignored; tools/keystore.fingerprint committed).
* GitHub MCP tools only (no gh CLI); hooks block curl to api.github.com; never work around.
* CI only via emulator.yml (sections input) and android.yml (full_build); no push triggers on workflows (minutes are scarce).
* Local Gradle only via bash tools/gradle.sh, one at a time; never ./gradlew directly.
* Live cable session with Tj: one short step per message, command in last message of turn.
* Refer to Tj by name; no gendered pronouns.
* Release order: bash ship.sh note, then android.yml full_build=true, get_release_by_tag, record-release.sh only after green, then Release link as plain text on own line.
* New Tj-facing behaviour switches default ON in release and OFF in debug (ScrimMeasure.defaultOn, recents_sweep, clean_start).
* Every polling loop checks its deadline first.
* Scenario must am force-stop the app before writing shared_prefs/tj_features.xml.
* CI RESULTS block lists only FAIL and INFO lines (PASS not listed); read with small tail_lines.
* ckpt text doubles as run log: put run ids in it.
* Never switch tools/MIRROR_MAIN off; after push check origin main equals branch tip.
* Fix flaky tests instead of rerunning the gate.
* Tablet/emulator: CI emulator is not Tj's tablet; stock menu differs (4th Emergency entry, scrim 0.75 or 0.80 by state); cmd uimode night does nothing on image; dark path proven by JVM tests.

## Key Decisions
- Decision: Recents guarantee by drawer MEMBERSHIP (new RecentsSweep, HiddenApps.isPackageShown), not origin tracking — Reason: origin heuristics missed window events and counted leftover tasks as custom (Telegram and Claude leak seen on tablet).
- Decision: clean start = no Solitaire stand-in; switch clean_start, release default ON, debug default OFF (same pattern as ScrimMeasure.defaultOn and recents_sweep) — Reason: debug keeps older emulator sections' Solitaire expectations; Tj delegated the choice ('if it is better'), so Claude decided; Tj can flip it in Launcher control.
- Decision: complete Recents clear only when custom session STARTS (Quickstep session ended, first unlock after boot, boot flag expires after 24 h), not every unlock — Reason: keeps apps Tj is actively using; HIDDEN_ONLY sweep gives the any-time guarantee.
- Decision: with adb shell helper, invisible sweeps by task id (same as swiping a card, no force-stop) at lock time, after plain unlock, when Recents opens, 0.4 s after a hidden app leaves screen. Without helper: on-screen recentsEmpty (Clear all, else Close every card via accessibility) behind black cover; settleHome removes cover as soon as custom home is in front — Reason: old cover waited for Solitaire cold start (seconds).
- Decision: patch hides at finger DOWN via FLAG_WATCH_OUTSIDE_TOUCH / ACTION_OUTSIDE; fast open path no longer refuses a merely 'away' patch; patch window pre-created while screen dark (lockPrepare / layerRefresh) — Reason: keyguard 'PIN pad coming' event arrives late when busy (emulator: hidden 64-73 ms after finger down); refusal of 'away' patch caused the Screenshot flash.
- Decision: run /code-review high on diff BEFORE gate, then gate once; tiered gate via tools/gate.sh and tools/gate-map.txt — Reason: review found 10 real bugs in ~700 lock-safety lines; tiered gate keeps Actions minutes low.

## 🙋 User Contribution (human-authored)
Tj used v0.8.4 on the real tablet and reported both symptoms (brief patch flicker at PIN pull-up; one Screenshot-button flash). Tj wrote the Recents requirement twice (BD1/BD2 and the 20:17 audit request). Tj asked for immediate custom unlock from lock screen. Tj asked for status mid-session.

## 🙋 User Decisions (made by the human)
- **Hidden-from-drawer apps must never appear in custom Recents**
  - Reason: Telegram and Claude were seen in custom Recents
  - Rejected: none stated
- **Custom launcher start must be immediate; Quickstep may be slower**
  - Reason: Custom launcher is the daily driver
  - Rejected: none stated

## Summary
Launcher v0.8.5 (versionCode 30) done and released. Tj 2026-11-07 ~20:17 UTC report after v0.8.4: BE1 lock-screen power-menu patch flicker + brief stock Screenshot flash; BE2 custom Recents must never show hidden-from-drawer apps; BE3 custom launcher must start from lock screen instantly, prep invisibly. All three shipped: RecentsSweep membership guard, clean start (no Solitaire, empty Recents at custom start), immediate hide at finger down, fast open path fix, pre-created patch window. Build: android.yml full_build run passed, cert verified, Release published, recorded in BUILDLOG.md. Gate: emulator runs plus core sections green; /code-review high run before gate found 10 real lock-safety bugs, all fixed. Only BE4 (Tj tablet check) open. Not measured on real tablet: only CI emulator and JVM tests.

---
*A short hint surfaces on session start; full context loads only if your next prompt matches a keyword above, or via manual `/handoff-resume`. The same record is available as a sibling `.json` file for external tools.*