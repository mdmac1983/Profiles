# AC drafts (written 2026-11-05 while the v0.7.2 gate ran; NOT compiled, NOT part of the APK)

Product code was frozen until the v0.7.2 `android.yml` run had checked out `main`, so the new classes were drafted here (this folder is documentation: Gradle does not look at it).
When the freeze is over: copy `main/*.java` to `app/src/main/java/com/tj/launcher/`, `test/*.java` to `app/src/test/java/com/tj/launcher/`, paste `ac-section.sh` into `tools/emulator_scenario.sh`
(before `if want rs2` or after section `ka`; add `ac` to the section lists in the script header and `.github/workflows/emulator.yml`), then wire them in as `research/AC_DESIGN_2026-11-05.md` says.
After the copy, DELETE this folder (the real files are the source of truth). Drafts only compile once copied; `BatterySaverGuard` calls `CrashGuard.automationOff(Context)` which does not exist yet (part of the wiring).

## UPDATE (same evening): the full change set is `ac-work.patch`
The drafts above were turned into a working change set in a throwaway clone (unit tests 277 / 0 failures). `ac-work.patch` is `git diff` of that clone against the commit it was cloned from (git apply it from the repo root
AFTER the v0.7.2 release is recorded; it does NOT bump the version). It contains: BatterySaver*, LockLog, PowerMenuDump (+ tests), CrashGuard pause flag, ProfileSession/UnlockMethods/ReturnReset/Mode/TjApplication/RouterActivity/TjAccessibilityService/
ControlActivity/ReportState/RestoreStore changes, the updated unit tests, the scenario `ac` section and the workflow section list. If the patch no longer applies (the repo moved), re-create it from `research/AC_DESIGN_2026-11-05.md`.
