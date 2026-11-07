# AGENTS.md

<!-- handoff:learnings:begin -->
## Session Learnings (auto-updated by handoff)

### Implicit Rules
- Stack: Java (not Kotlin), Android 10 API 29, Go-edition launcher; Tj's Android 16 preference does not apply here.
- First action each session: bash tools/install-hooks.sh. Checkpoint after every step: bash tools/ckpt.sh "did" "next" with short text.
- New Tj request goes into TASKS.md in his words before any code; a Tj message is answered, never dropped.
- Tj's photos never enter the repo (public repo, autosave git add -A). Describe photos in words without name, ID or number.
- Never commit secrets or keystore (app/sideload.jks gitignored; tools/keystore.fingerprint committed).
- GitHub MCP tools only (no gh CLI); hooks block curl to api.github.com; never work around.
- CI only via emulator.yml (sections input) and android.yml (full_build); no push triggers on workflows (minutes are scarce).
- Local Gradle only via bash tools/gradle.sh, one at a time; never ./gradlew directly.
- Live cable session with Tj: one short step per message, command in last message of turn.
- Refer to Tj by name; no gendered pronouns.
- Release order: bash ship.sh note, then android.yml full_build=true, get_release_by_tag, record-release.sh only after green, then Release link as plain text on own line.
- New Tj-facing behaviour switches default ON in release and OFF in debug (ScrimMeasure.defaultOn, recents_sweep, clean_start).
- Every polling loop checks its deadline first.
- Scenario must am force-stop the app before writing shared_prefs/tj_features.xml.
- CI RESULTS block lists only FAIL and INFO lines (PASS not listed); read with small tail_lines.
- ckpt text doubles as run log: put run ids in it.
- Never switch tools/MIRROR_MAIN off; after push check origin main equals branch tip.
- Fix flaky tests instead of rerunning the gate.
- Tablet/emulator: CI emulator is not Tj's tablet; stock menu differs (4th Emergency entry, scrim 0.75 or 0.80 by state); cmd uimode night does nothing on image; dark path proven by JVM tests.

### Key Decisions
- Decision: Recents guarantee by drawer MEMBERSHIP (new RecentsSweep, HiddenApps.isPackageShown), not origin tracking — Reason: origin heuristics missed window events and counted leftover tasks as custom (Telegram and Claude leak seen on tablet).
- Decision: clean start = no Solitaire stand-in; switch clean_start, release default ON, debug default OFF (same pattern as ScrimMeasure.defaultOn and recents_sweep) — Reason: debug keeps older emulator sections' Solitaire expectations; Tj delegated the choice ('if it is better'), so Claude decided; Tj can flip it in Launcher control.
- Decision: complete Recents clear only when custom session STARTS (Quickstep session ended, first unlock after boot, boot flag expires after 24 h), not every unlock — Reason: keeps apps Tj is actively using; HIDDEN_ONLY sweep gives the any-time guarantee.
- Decision: with adb shell helper, invisible sweeps by task id (same as swiping a card, no force-stop) at lock time, after plain unlock, when Recents opens, 0.4 s after a hidden app leaves screen. Without helper: on-screen recentsEmpty (Clear all, else Close every card via accessibility) behind black cover; settleHome removes cover as soon as custom home is in front — Reason: old cover waited for Solitaire cold start (seconds).
- Decision: patch hides at finger DOWN via FLAG_WATCH_OUTSIDE_TOUCH / ACTION_OUTSIDE; fast open path no longer refuses a merely 'away' patch; patch window pre-created while screen dark (lockPrepare / layerRefresh) — Reason: keyguard 'PIN pad coming' event arrives late when busy (emulator: hidden 64-73 ms after finger down); refusal of 'away' patch caused the Screenshot flash.
- Decision: run /code-review high on diff BEFORE gate, then gate once; tiered gate via tools/gate.sh and tools/gate-map.txt — Reason: review found 10 real bugs in ~700 lock-safety lines; tiered gate keeps Actions minutes low.

<!-- handoff:learnings:end -->
