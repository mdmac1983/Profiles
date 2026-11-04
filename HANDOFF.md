# HANDOFF — read this first if you are a Claude session with NO prior context

Written 2026-11-04 (end of the v0.5.x → v0.6.0 stretch). It is the single entry point for a new Claude account. Everything it says is
checked against the repo at that date; where it can go stale it says where to look instead. After this, read in order:
`CLAUDE.md` (working agreement, auto-loaded) → `CHECKPOINT.md` (where the last session stopped, "Do this next") → open `[ ]` in `TASKS.md`.

## 0. In one minute
- **What:** `com.tj.launcher`, a sideloaded **Android 10 (API 29) Go-edition home-screen launcher** for ONE tablet owned by Tj
  (Alps 711 `tb8168p1_bsp`, MediaTek MT8168B, 32-bit, ~1.3 GB RAM free, 600×1024, locked bootloader, stock launcher = Quickstep v10).
  It is a port of AOSP Launcher3 (Java, not Kotlin in practice) plus a large layer of Tj-specific behaviour.
- **The product idea (Tj's standing goal):** the *custom launcher* and the *stock Quickstep launcher* must behave like **two separate
  user profiles that do not know each other** — own apps/Recents/notifications/quick-settings tiles/power menu/keyboard.
  Custom = locked down and plain (opens Solitaire; only the auto-rotate tile; power menu without Screenshot; stock keyboard).
  Quickstep = normal, reached only by a **secret handshake on the lock-screen PIN pad** (never from the custom launcher).
- **Who Tj is / how to work with him:** non-developer, runs everything from a phone/tablet and a Claude chat. Wants plain-language answers,
  a clear "what I did / what you must do". Sends `?` when he thinks you went quiet — answer at once with real status. Every message he sends
  must be written into `TASKS.md` in his own words as `[ ]` boxes BEFORE any code (`CLAUDE.md` rule), and each must get an answer.
  He prefers longer sessions and more usage if it gives a better product, but wants usage spent efficiently.
- **State at hand-off:** see `BUILDLOG.md` (last line = last real release) and `CHECKPOINT.md`. Releases are built and signed by GitHub
  Actions; this container never builds a release.
- **Repo:** https://github.com/tjshea90/Launcher (default branch `main`, mirrors the session branch on every push).

## 1. First 10 minutes (do these, in this order)
1. `bash tools/install-hooks.sh && bash tools/install-hooks.sh --check` — web sessions do NOT load repo hooks by themselves. Without them nothing is
   auto-saved. After 2-3 edits `git log --oneline -3` must show `auto-checkpoint:` commits; if not, run `bash tools/ckpt.sh "did" "next"` by hand.
2. The SessionStart hook prints a briefing (`tools/resume.sh`): sync state, INTERRUPTED/UNCOMMITTED warnings, `CHECKPOINT.md`, open tasks, INBOX tail.
   If you did not see it: `bash tools/resume.sh`.
3. Look for unfinished work: `git status`, `git log --oneline -5`, and "another branch has N commits this one lacks" in the briefing.
4. Compare the `INBOX.md` tail with `TASKS.md`: any message of Tj's that has no `[ ]`/`[x]` entry and no answer is unanswered — handle it first.
5. Tools you need (names as seen in this environment): GitHub MCP (`mcp__github__*`: `actions_run_trigger`, `actions_get`, `actions_list`,
   `get_release_by_tag`, …), `Bash`, `Read/Edit/Write`. There is **no `gh` CLI**. Session scope is the single repo `tjshea90/Launcher`.
6. Secrets: never paste or commit one (`tools/secretscan.sh` blocks it). The keystore is committed on purpose as base64 (`signing/README.md`);
   `app/sideload.jks` itself is gitignored and absent in this container — expected. Never regenerate the keystore, never change `applicationId`.

## 2. How a change travels (the save ladder + release routine)
- **Rung 1 (automatic):** hooks commit + push after every tool call (`tools/autosave.sh`). Every Tj message is captured verbatim to `INBOX.md`.
- **Rung 2 (you):** `bash tools/ckpt.sh "what I just did" "the specific next step (file/function/command)"` after EVERY step. "next" is the one thing
  no diff can reconstruct. Autosave commits everything, so **do not leave half-written code lying around if a release is about to be cut**.
- **Rung 3:** `bash ship.sh "note"` — gate (handoff self-test, full unit suite, lint, versionCode must exceed every previous ship), pushes.
- **Rung 4:** trigger **`android.yml` on `main` with `full_build=true`** (`mcp__github__actions_run_trigger`, method `run_workflow`, inputs `{"full_build":"true"}`).
  GitHub builds, signs with the pinned key, verifies the certificate, creates the tag and Release. Then confirm with `get_release_by_tag`, and only then
  `bash tools/record-release.sh vX.Y.Z "note"` (writes `BUILDLOG.md`; it is guarded — run it BEFORE bumping the version again).
  Give Tj the Release URL as **plain text on its own line** (never in a code block): `https://github.com/tjshea90/Launcher/releases/tag/vX.Y.Z`.
- Never add a `push:` trigger to any workflow (Actions minutes are scarce; `test_handoff.sh` fails if you do). Dispatch/tag only.
- Version lives in `app/build.gradle.kts` (`versionCode`, `versionName`). Last shipped: see `BUILDLOG.md`.

## 3. The test system (this is where most time goes — read before testing)
- **Unit tests (local, fast):** `bash tools/gradle.sh testDebugUnitTest` (~1-2 min warm). Pure-Java rule classes (`UnlockMethods`, `ProfileSession`,
  `PowerMenuRule`, `ShellProtocol`, …) are covered here; most logic is deliberately written as small testable rule classes.
  Build traps: `BRIEF.md` (never blank `JAVA_TOOL_OPTIONS`, one Gradle at a time, long builds in the background).
- **Emulator check (the integration test):** `.github/workflows/emulator.yml`, manual dispatch only, runs `tools/emulator_scenario.sh` on an Android 10 x86
  emulator at 600×1024/170 dpi. It writes `results.txt` (`PASS/FAIL/INFO` lines, ends with `failed checks: N`) + screenshots + logs into an artifact.
  - **Run only the sections you need:** input `sections` (comma list). Names: `base legacy x1 e1 y2 b6 b4 n n6 o t u s p x v last`; empty = everything
    (the release gate, **~25 min**, scenario timeout 2100 s). A single feature section is 3-8 min. Two different section strings may run side by side.
  - **Read results:** `actions_get get_workflow_run <id>` → when `completed`: `actions_list list_workflow_run_artifacts <id>` → `actions_get download_workflow_run_artifact <artifact id>`
    → `curl` the signed URL into the scratchpad → `unzip` → `grep -n '^FAIL\|NATIVE CRASH' results.txt`. Look at the PNGs and the per-check log files.
  - **`list_workflow_runs` returns HUGE output** (it ignores `per_page`). Use `workflow_runs_filter` (`{"branch":"...","created":">2026-11-04T21:00:00Z"}`) and, once you know
    the run id, only `get_workflow_run`. A dispatch right after a push can run on the OLD commit — check `head_sha` of the run.
  - A run can show **failure with 0 FAILs** if the scenario hit its `timeout` (that happened at 1000 s; now 2100 s). Check the tail of `results.txt`.
  - **Known emulator flakes (don't chase, rerun the section):** qemu dying; a rare native SIGSEGV of our process inside Android's libz/libhwui (one per ~full run;
    `heal()` in the scenario detects it, logs `INFO NATIVE CRASH`, restores the default home — FAILs within ~15 s after that line are caused by it).
  - **Test-writing lessons:** `adb shell input keyevent` is injected and bypasses the accessibility key filter, so hardware-key behaviour is NOT provable on the emulator
    (only the JVM `KeySeq` tests prove it). Section-only runs have no default home → every section starts with `ensurehome`. Fixed sleeps are fragile; poll.
    `adb logcat -c` also ends the streaming logcat. Quote carefully: use the existing `setfeat`/`vfeat` pattern for writing prefs through `run-as`.
  - **What CANNOT be proven without Tj's tablet:** real hardware keys (volume/side), rotation/shake/knock sensors, a real restart, MediaTek-specific behaviour,
    demo-mode/INTERNET/power-menu quirks on his ROM. Say so honestly; ask Tj to test and report.

## 4. Architecture map (`app/src/main/java/com/tj/launcher/` unless noted; Launcher3 itself is under `launcher3/` and `app/src/main` ports)
| Area | Classes | What / why |
| :- | :- | :- |
| Home + modes | `RouterActivity` (the HOME app), `Mode`, `StockLauncher`, `CrashGuard`, `StrayHomeGuard` | The router decides custom vs stock on every Home press. Custom launcher is started as a HOME-type task (never in Recents; cost: a process crash makes Android clear the default-home setting → "Choose a Home app"). Crash breaker falls back to stock. Stray-stock-home guard presses Home if the stock home appears in custom mode. |
| Lock-screen handshake | `UnlockMethods` (16 methods, multi-select, ≥1 always on), `UnlockPick`, `MotionDetector`, `KeySeq`, `ProfileSession` | Secret sequences on the PIN pad / keys / charger / rotation / shake / knock pick Quickstep at unlock; a plain PIN = custom. Lost-Enter race fixed: a pad sequence dismissed ≤3 s before unlock still counts (`finishPendingPad`). `ProfileSession` holds the pure rules (pre-arm behind keyguard, pre-clean at lock, boot ends a Quickstep session). |
| Ways back to custom | `ReturnMethods`, `BackToCustomActivity`, `ReturnTile`, `FailsafeKeys` | Double-Home, icon, QS tile, key codes, side-key flurry, shake/knock, plus master failsafes (Konami keys, hidden Solitaire hotspot) to always reach Quickstep. |
| Return reset | `ReturnReset`, `ResetPolicy`, `RecentsGuard`, `RecentsPolicy`, `RecentsDirty`, `AnimationSpeedup` | Closing Quickstep's apps/notifications/radios when going back to custom (black cover, animations to 0). |
| Accessibility helper | `TjAccessibilityService` (big; ~1600 lines) | Power-menu cover + lock-screen wallpaper patch, pencil cover, lock-screen serial number (`LockNumber`), key filter, charger/rotation/sensor feeds, lock-time prepare, tile guard, radio guard, unlock stopwatch. |
| Power menu | `PowerMenuRule`, parts of the service | Cover modes: `off`, `hide_screenshot`, `wallpaper` (home), `block`. **Lock screen (v0.6.0):** wallpaper patch of the LOCK wallpaper + a card of stock item height (Power off + Restart), patch invisible until the menu opens (`lock_patch` key, Control toggle). Menu rect + item size are learned once per rotation (`power_card_rect_rN`, `power_item_rN`). |
| Per-launcher settings | `TileProfile` (QS tiles), `DemoModeProfile` (hides pencil+gear natively; opt-in), `KeyboardSwitch/Policy`, `RestoreStore` ("Restore everything") | Need `WRITE_SECURE_SETTINGS` (adb grant). Every setting change is snapshotted first and restorable. |
| Invisible clean-up helper | `ShellServer` (shell-uid `app_process` over loopback TCP 127.0.0.1:47631-47635, peer uid checked in /proc/net/tcp), `ShellProtocol` (allow-listed jobs only), `ShellHelper` (client) | Started by ONE adb line, dies at reboot. Force-stops apps + removes Recents tasks with no UI. Jobs include permission grants, `COMPILE_SELF`, `SHOTS`, `APKS`, `PROBE`. |
| Setup/probe | `SetupActivity`, `TabletReport`, `AdbGuideActivity`, `tools/adb-all.sh`/`.bat`, `app/src/main/resources/probe-commands.txt`, `tools/adb-probe.sh`, `research/ADB_*` | The "probe": one adb session grants everything, starts the helper, reads tablet facts, copies the tablet's own Settings/SystemUI/Launcher3/framework APKs, screenshots stock Settings. Phone-adb route: `research/ADB_PHONE.txt`. |
| UI | `ControlActivity` (Launcher control; Quickstep-only), `HiddenAppsActivity`, `Features` (prefs `tj_features`), `TjLook` | Control screen is hidden in custom mode by design. |

## 5. Decisions already made — do NOT redo or re-ask
- **No gesture into Quickstep from the custom launcher** (removed); entry is only the lock-screen handshake or the master failsafe.
- **P1 (each launcher keeps its OWN apps open and hidden from the other): decided NOT possible seamlessly** — evidence in `research/PROFILES_2026-11-04.md` and `IDEAS_2026-11-04.md`
  (one Android user = one Recents/shade; `pm suspend` kills the Recents card; `pm hide` refused to shell; a real 2nd user ends on its own lock screen).
- Power-menu: no earlier signal than the menu window exists; the side key never reaches an accessibility service; RRO overlay can't change the list; `MONITOR_INPUT` refused to shell
  (`research/NOTES_2026-11-04.md`, `IDEAS`). Native fix possible only if the ROM has `power_menu_actions` (probe prints it).
- Pencil: `device_demo_mode=1` hides pencil + gear natively on the emulator (opt-in switch; try the adb line first). The cover remains the default.
- AVD cache/snapshot for faster emulator runs: tried and dropped. Full runs are long; use sections.
- Wallpaper work (home) was once dropped by Tj ("forget the wallpaper"), later he asked for the LOCK-screen patch (done in v0.6.0) — don't polish the home one.
- Do not decide `BRIEF.md` OPEN items for Tj. Do not start **SET1 (stock-looking Settings clone)** or multi-user/device-owner work without his go-ahead and his inputs.

## 6. Waiting on Tj (as of this hand-off)
1. Tablet testing of v0.5.0/v0.6.0: install over the old one, turn the accessibility helper off and on once, start the shell helper (adb line or `Launcher control > Tablet setup & report`),
   try the new lock-screen methods, the lock-screen power menu (nudges in Control if the patch is off), and report the unlock timing shown in Control ("Last unlock: …").
2. **The probe output** (`probe-out/` from `bash tools/adb-all.sh`, or `Downloads/launcher-probe` from the phone route): answers max users / second user, ROM identity, whether the helper can start before
   first unlock, `power_menu_actions`, compile state, memory/process facts, and delivers the tablet's own Settings APK for SET1.
3. Screenshots + keep/remove list for the Settings clone (SET1/C4) when he is ready.
4. Optional housekeeping: delete the old branch `ccr-4ff66321-chzhhg`; Dependabot PRs re-open against `main`.

## 7. Backlog beyond what is shipped (see `TASKS.md` for exact wording)
SET1 (curated Settings clone; plan in `research/SETTINGS_CLONE_2026-11-02.md` + `IDEAS_2026-11-04.md §4`), C4 gear→clone, L5 (tile strip partly covered by `TileProfile`),
L10 (standing safety rule: before ANY release be highly confident nothing breaks; failsafes must always work), self-adb idea (launcher starts its own helper after restart; needs the probe's
`service.adb.tcp.port` answer), per-launcher setting bundles (DND/animation/brightness).

## 8. Document index (`research/` unless noted)
`ADB_TODO.md` (every adb command, fast path on top) · `ADB_PHONE.txt` (adb from a phone) · `ADB_COMMANDS.txt` · `PROFILES_2026-11-04.md` (two-profiles design + limits) · `IDEAS_2026-11-04.md`
(separation / power menu / pencil / Settings plan / accelerometer) · `NOTES_2026-11-04.md` (answered research) · `DESIGN_2026-11-02.md` (foundation design, safety rules §6) ·
`SETTINGS_CLONE_2026-11-02.md` · `FIRST_INSTALL.md` · `HANDOFF_2026-11-03.md`, `HANDOFF_2026-11-04.md` (older, history) · `reference/stock/INDEX.md` (**the stock-launcher photos are the ground
truth for the look**: open `stock-home.png`/`stock-drawer.png` and measure before saying a look is matched) · root: `BRIEF.md`, `BUILDLOG.md`, `TASKS.md`, `CHECKPOINT.md`, `INBOX.md`.

## 9. Pitfalls that cost real time (each happened)
- Editing scripts with inline python/heredocs and unusual quoting silently mangled a file once (a `setmode` line was overwritten because a "first match" search hit the wrong line). After a scripted edit,
  `bash -n` the script AND diff the touched lines against `git show <good-commit>:file`.
- Don't tick a `TASKS.md` box before a named test proves it; name the test in the line.
- A full emulator run takes 25 min; plan the order so most failures are found by 5-minute section runs first. Don't ship on "should work": a lost-Enter race only showed up in a long run.
- Reading `list_workflow_runs` unfiltered floods the context. Don't.
- When Tj says "?" he means "report status now" — answer with the newest run/release state, then continue.
- Tj's tablet may run an OLDER version than the repo; always state which version a statement refers to.
