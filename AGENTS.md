# AGENTS.md

<!-- handoff:learnings:begin -->
## Session Learnings (auto-updated by handoff)

### Implicit Rules
- Stack: Java (not Kotlin) Android 10 API 29 Go-edition launcher built on in-repo Launcher3 source (launcher3/PATCHES.md lists deviations); applicationId com.tj.launcher; Tj's Android 16 preference does not apply here
- First action every session: bash tools/install-hooks.sh; checkpoint after every step with bash tools/ckpt.sh did next; new Tj request goes into TASKS.md in his words before code
- Local builds only for tests and lint: bash tools/gradle.sh testDebugUnitTest :app:lintDebug; GitHub Actions builds APKs; release order ship.sh then android.yml full_build on main then get_release_by_tag then tools/record-release.sh
- Emulator checks: emulator.yml sections input; concurrency group includes ref so branch runs do not cancel main runs; gate A and B lists in CLAUDE.md; read RESULTS block in job log tail
- Hooks block variable rm -rf and curl to api.github.com; do not work around
- Never commit keys or keystore; tools/MIRROR_MAIN decides whether pushes also fast-forward main; a branch that is not an ancestor of main fails the mirror silently
- During a live cable session send ONE short step per message, command in last message, then wait; tablet clock is EST; ADB OTG app echoes long lines scrambled but they run
- Tablet facts: Enforcing SELinux user build, shell helper unreachable, logcat buffer about 3 minutes, DuraSpeed on, accessibility service must be re-bound by tablet restart after a crash loop
- Write handoff and learnings in English; Tj uses no gendered pronouns in text, refer to Tj by name

### Key Decisions
- Decision: ACC1 root cause is stale Binding state in accessibility manager from the 12:48 crash loop; fix is a tablet restart — Reason: AMS had no ServiceRecord while Binding set stayed; after restart service bound and logs showed unlock handled; DuraSpeed off changed nothing
- Decision: notification access is not a missing permission — Reason: low-RAM Go devices refuse managed services for non-whitelisted packages; allow_listener silently leaves enabled_notification_listeners null; ReturnReset already said so; guide must not offer it
- Decision: keep-alive default OFF, PNG transparent small icon, crash fuse takes first RemoteServiceException Bad notification crash (switches only keep-alive off, sets keep_alive_blocked, not counted toward 3-crash breaker), onCreate refuses to repost when rules say no, test knob keep_alive_test_bad_icon only in debuggable builds — Reason: system crashed app 3x in 7s on MediaTek SystemUI, tripped breaker, wiped switches and stranded accessibility
- Decision: CBR1 clears only the tripped flag on a newer versionCode and leaves Mode untouched; unreadable version stored as Integer.MAX_VALUE — Reason: new version gets fair try but Tj chooses when to return to custom
- Decision: updates need no adb (grants, app-ops, battery whitelist, accessibility enabled, Home choice survive in-place update; only optional speed compile is lost) — Reason: same signing key; documented in research/ADB_COMMANDS.txt
- Decision: this session does not touch v0.7.3 gate or release and never pushes to main; branch ccr-9ab65fa5-vp812y is main plus a commit setting tools/MIRROR_MAIN off — Reason: other session on another account is mid-gate; android.yml builds main HEAD; concurrent pushes would contaminate release or reject fast-forwards; at integration merge main, set MIRROR_MAIN on, run gates A and B
- Decision: lock-screen power-menu cover must work on any lock screen (cover world = custom mode OR keyguard locked) — Reason: Tj's tablet lock from Quickstep has no cover because mode flips to custom only after helper clean-up
- Decision: gesture unlock should resume the last Quickstep app (launch intent without RESET_TASK_IF_NEEDED) instead of stock home, fallback stock home — Reason: Tj asked if minimizing apps is necessary; it is not
- Decision: new secret entrances supersede earlier N3a removal because Tj explicitly asked for several toggleable hidden options; no hardware keys, no accelerometer; ProfileSession.goToQuickstep needs no helper — Reason: Solitaire hotspot unreliable (default off, needs Solitaire configured and in front)

<!-- handoff:learnings:end -->
