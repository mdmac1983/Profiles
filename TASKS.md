# TASKS — the job list. Tick `[x]` only when written, tested, committed (name the test).

Tj's request, 2026-11-02 (verbatim): "In the launcher repo, I will be building an Android 10 go launcher apk. I want
it to use github actions for as much as it can contribute, and save progress and checkpoints to github. Set up the
foundation for the launcher repo. I want there to be a comprehensive checkpoint, ladder, and save system so that if
Claude is interrupted by usage, another session can pick up the work without loss of progress. Look at the other
repos and their checkpoint systems for this purpose and adopt the best system but modify it for the launcher repo.
Install and use any skills and plug-ins needed to help this project. Install or adopt the tools needed to build apks
with github actions, just like the other repos do."

## Foundation (2026-11-02)
- [x] Checkpoint ladder: capture_inbox (rung 0), autosave (1), ckpt (2), ship (3), Actions (4) — `tools/test_handoff.sh`
- [x] Cold-resume briefing with interrupted-session and stranded-branch detection — `tools/test_handoff.sh`
- [x] Hook installer scoped to this repo, merge-safe next to novig/Mirage hooks — `tools/test_handoff.sh`
- [x] Secret scan on commit; key redaction in INBOX — `tools/test_handoff.sh`
- [x] Android project skeleton: HOME activity, `AppList` logic — `AppListTest` (`bash tools/gradle.sh testDebugUnitTest`)
- [x] Local debug build + lint green (2.5 MB debug APK)
- [x] Workflows: `android.yml` (gates / preview / signed release), `ci.yml` (PR tests + lint), Dependabot
- [x] Project skill `android-go-launcher`; kotlin-concurrency-and-flow copied
- [ ] **Run `android.yml` once on GitHub** (dispatch, `full_build=false` then `true`) and confirm the preview APK artifact
      appears — the workflow has never run on a real runner
- [ ] Tj decides the OPEN items in BRIEF.md (UI toolkit, features, device, keystore)
- [ ] Create the keystore (`bash tools/init-keystore.sh`), back it up, Tj adds `SIGNING_KEYSTORE_BASE64`
- [x] Tj approved `tools/MIRROR_MAIN` = on (2026-11-02): every push also fast-forwards `main`
- [ ] Install the preview APK on the Android 10 Go device; set it as default home app; report what happens

## Features
(none yet — Tj has not described them. Write each request here in his words before coding.)
