# Launcher — working agreement

Tj's **Android 10 (API 29) Go-edition home-screen launcher**, `com.tj.launcher`, Kotlin, sideloaded (not on
the Play Store). GitHub Actions builds the APK; Claude writes the code. Worked on across many Claude Code
sessions — a new one may start on another account or device, or right after a usage cap killed the last one
mid-step. **Everything below exists to make that handoff lossless.** What is decided about the platform and what
is still open: `BRIEF.md`. The job list: `TASKS.md`. Where the last session stopped: `CHECKPOINT.md`.
**A session with NO prior context (new account/device): read `HANDOFF.md` first** — one page: what this is, first 10 minutes, release routine, test system, architecture map, decisions already made, what waits on Tj.

Sibling repos in this container (`novig`, `Mirage`) are the origin of this system; they are **read only** for
Launcher's work. Never commit, push or run their scripts from here.

## The stock launcher photos are the ground truth for the look (Tj, 2026-11-02, permanent)

**Standing instruction (verbatim):** "Remember these photos for reference at all times, even in a new Claude session." The custom launcher must look and function IDENTICALLY to the
stock Quickstep v10. Photos + measured facts: **`research/reference/stock/INDEX.md`** (`stock-home.png`, `stock-drawer.png`, power menu, device info). Before saying any look is matched,
open `stock-home.png` / `stock-drawer.png` next to the newest emulator or tablet screenshot and measure (grid, row pitch 113 px, scrim 50% white, no dock, no caret, 5 columns). Emulator runs:
`emulator.yml` (manual) at 600x1024, 170 dpi, font scale 1.2. Never rely on memory of the photos.

## FIRST ACTION OF EVERY SESSION — install the hooks, don't assume they exist

Claude Code on the web does **not** load hooks from a repo's own `.claude/settings.json` when the repo sits under a
shared session root (`CLAUDE_PROJECT_DIR` is unset). So, before anything else:
```bash
bash tools/install-hooks.sh          # idempotent; MERGES, never overwrites other repos' hooks
bash tools/install-hooks.sh --check && echo installed || echo MISSING
```
`tools/ckpt.sh` and `tools/resume.sh` repair the hooks too, so a session that forgets still gets the net back at its
first checkpoint. Every hook is an absolute path into **this repo's** `tools/` only (marker
`launcher-checkpoint-hooks`); nothing here touches a sibling repo.
Verify over a few tool calls, not one: after 2-3 real edits, `git log --oneline -3` should show
`auto-checkpoint:` commits. If none appear, the hooks are not firing — run `bash tools/ckpt.sh "did" "next"` by hand
after every step for the rest of the session, and say so.

## Starting a session

The `SessionStart` hook ran `tools/resume.sh`, which fetched GitHub and printed: sync state, any
**INTERRUPTED MID-CHANGE** / **UNCOMMITTED WORK** warning, any *other branch with work this one lacks*,
`CHECKPOINT.md`, the open `TASKS.md` items, the `INBOX.md` tail and the rules (kept under Claude Code's 10,000-char
hook cap; `tools/test_handoff.sh` checks it). **Do not re-plan or re-read finished work.** Continue from
**Do this next**, or the first `[ ]` in `TASKS.md`. If you did not see the briefing: `bash tools/resume.sh`, and tell Tj
it did not fire.
- *INTERRUPTED MID-CHANGE* — the tree is clean only because a hook committed a half-written change, and
  `CHECKPOINT.md` is stale. Run the `git diff` it names, finish that change, `ckpt` — then new work.
- *Another branch has N commits this one lacks* — a cut-off session's work. Look (`git log --oneline HEAD..origin/X`),
  then `git merge origin/X`. If it contradicts this branch, tell Tj before merging blind.
- Check the `INBOX.md` tail against `TASKS.md`: anything in INBOX that TASKS lacks is a request nobody wrote down.
- **Taking over from a cut-off session or another Claude account** (Tj, 2026-11-06: "be very careful, do not lose progress or break anything"): follow `HANDOFF.md` section 0d (find the newest work, judge whether the other session is alive from its newest commit, read its "Do this next", check CI runs and Releases BEFORE dispatching anything, align your branch, ship one version at a time).

## The save ladder (assume you can be cut off at ANY moment)

| Rung | What | Who runs it | Saves |
| :- | :- | :- | :- |
| 0 | `tools/capture_inbox.sh` (UserPromptSubmit) | hook | every message Tj sends, verbatim (keys redacted), committed + pushed before any work |
| 0b | `tools/capture_outbox.sh` (called by autosave on Stop AND after tool calls) | hook | what Claude told Tj: the last reply of every turn and any substantial reply text (200+ chars), quoted, key-redacted, into `OUTBOX.md`; `resume.sh` prints its tail (2026-11-06: findings that lived only in a chat died with the chat) |
| 1 | `tools/autosave.sh` (PostToolUse Edit/Write/Bash, Stop) | hook | `git add -A`, secret scan, commit, push — after every tool call; no tests, no gate |
| 2 | `bash tools/ckpt.sh "did" "next"` | **you, after every completed step** | rewrites `CHECKPOINT.md` (did / next / state), fast checks, commit, push |
| 3 | `bash ship.sh "note"` | you, at real versions | full gate (handoff test, unit tests, lint, versionCode), push, queue the GitHub build |
| 4 | GitHub Actions | GitHub | builds + signs the APK, publishes the Release; `BUILDLOG.md` is the durable record |

Rules that make the ladder work:
- **"next" is the one thing no diff can reconstruct.** Make it specific: file, function, command. Run `ckpt` after
  every step, not at the end of the session. Never hold findings or intent only in the conversation or `/tmp`.
- **New request from Tj:** write it into `TASKS.md` in his own words as `[ ]` boxes and `ckpt` **before** any code.
  Tick a box only when written, tested and committed; name the test that proves it.
- **Low on usage?** Spend what is left on `ckpt` with an honest "next", not on one more edit.
- **Long conversation** (many compactions, a big task just finished): `ckpt`, then tell Tj *"Handoff ready: start a
  new session, it resumes from CHECKPOINT.md"*. `tools/toobig.sh` (PreCompact) saves and says this too.
- **Push failure is never silent.** Autosave warns when commits exist only in this container (they are lost when
  the session ends). Fix the connection and `git push origin HEAD` at once.

## Messages from Tj (standing rule, 2026-11-03, permanent)

If Tj sends several messages while a task is running: **keep doing what you are already doing**, add each new message as the next step (write it into `TASKS.md` in his words, `ckpt`), **never drop or forget one**, and **answer every one of them**, including ones from earlier in the conversation that are still open (re-list them at the end of a reply until they are done). Before ending a turn, scan `INBOX.md`/`TASKS.md` for any message of his that has no answer yet.

**Live cable session (Tj, 2026-11-05, permanent):** when Tj is at the tablet with the OTG cable and time is short, send ONE short step per message and wait for the reply: no findings, no options, no explanations in the step message. Put every command in the LAST message of the turn: text written before a tool call can be missed on Tj's screen (twice Tj asked for a line that sat earlier in the same turn), so do the bookkeeping tool calls first and the step text last, with no tool call after it.

## Files from the tablet (Tj, 2026-11-05, permanent)

Tj: "make sure claude can save and use all necessary information from the tablet when connected." When he sends `tablet-report.txt` (or the `launcher-probe` folder, or a zip, or pastes the report text):
`bash tools/tablet-intake.sh <file|folder|zip>` (`-` reads standard input) → **read `research/tablet/LATEST.md` first** (red flags, then the answers) → `git add research/tablet && bash tools/ckpt.sh "tablet intake" "what to do about the red flags"` → write what the answers decide into `TASKS.md`.
Never commit a raw report or a tablet screenshot by hand: the intake redacts serial/IMEI/MAC/IP/e-mail/Wi-Fi names and refuses credentials (`research/tablet/README.md`; pinned by `tools/test_tablet_intake.sh`). A session that is cut off right after Tj sends the file loses nothing as long as the intake and a `ckpt` ran.

**Tj's PHOTOS stay OUT of the repo (Tj, 2026-11-06, permanent).** The repo is PUBLIC and autosave runs `git add -A` after every tool call, so anything copied into the repo folder is on GitHub within seconds, for good (history keeps it). Photos of the tablet show his name, an ID and the lock-screen number (he blurs some with a blue scribble, not all). Rules: view a photo where it was delivered (`/root/.claude/uploads/...`) or in the scratchpad, never `cp`/`mv` it into the repo; describe what it shows in TASKS.md in words with NO name, ID or number; the new photos he sends are "only Claude may see them". **To MEASURE a phone photo of the tablet** (card / number / patch sizes): `python3 -I tools/photo_rectify.py <photo> <scratchpad-dir> [--rotate 90]` rectifies it to a true 600 x 1024 screen (it refuses to write inside the repo; method, pitfalls and the 2026-11-06 results: its header, HANDOFF.md 0d and TASKS.md AH3 / AL1; best input: stock tablet and ours TOGETHER in one photo). Four old lock-screen screenshots from 2026-11-03 are already tracked in `research/uploads/2026-11-03/` (see TASKS.md AF2: his decision).

## Branches

Claude Code gives every session its own branch, so work can strand on a branch nobody opens. Rules:
- Develop and push on **the branch the session was assigned**; `push.sh` pushes it after every checkpoint.
- `tools/MIRROR_MAIN` (`on`/`off`, committed) makes every push also fast-forward `origin/main` (plain push, never
  forced). **On** since 2026-11-02 (Tj approved), so a fresh session that just opens the repo sees the real state;
  GitHub can only dispatch workflows that exist on the default branch, which is now `main` itself. **2026-11-06 (Tj: "Why are my prompts and Claude findings not being saved? This is important to fix"):** the mirror had been `off` since the v0.7.3/0.7.4 gating and was never switched back, so `main` froze at v0.7.4 for three releases while every new session started FROM it: prompts (INBOX), findings (TASKS) and code looked lost (they sat on side branches). It is `on` again and `main` follows every push. NEVER switch it off again without writing WHY in TASKS.md and switching it back on in the same session; after a push check `git ls-remote origin refs/heads/main` equals your tip.
- `resume.sh` names every other remote branch with unmerged commits; merge deliberately, not blindly.

**What each branch is (audited 2026-11-03):**
| Branch | Purpose | Status |
| :- | :- | :- |
| `main` | the mirror (`MIRROR_MAIN`): fast-forwarded by every push; what a fresh session and the release workflow see | the real, current state |
| `claude/...` / `ccr-...` | one per Claude session; the session's working branch | `ccr-4ff66321-chzhhg` is an OLD session branch, fully contained in `main` (frozen at ckpt 136); it was GitHub's default branch until Tj switched the default to `main` (2026-11-03, confirmed `HEAD branch: main`); now just a leftover, safe to delete |
| `dependabot/*` | bot branches, one per update PR | were rooted on a pre-2026-11-02 history that shares no ancestor with `main`/`ccr` (the repo was re-rooted that day), so their PRs could never merge; PRs #1,#2,#3,#5,#6 closed 2026-11-03; do NOT merge such a branch; let Dependabot recreate it |
`resume.sh` no longer raises a false alarm for `dependabot/*` or for a branch with no common history.

## GitHub Actions does the building (Tj's rule on all repos)

`.github/workflows/android.yml` builds the APK; `ci.yml` runs tests + lint on pull requests; Dependabot proposes
updates monthly. **Minutes are the scarce resource** — never add a `push:` branch trigger to any workflow
(autosave pushes after every tool call; `test_handoff.sh` fails if you do). Tags and manual dispatch only.
- **A brand-new workflow whose triggers are only tags + dispatch is invisible to the API (dispatch → 404) until one
  event touches it.** Register it once: add a temporary `push: branches: [<branch>, main]` + `paths: [that file]`,
  push, then remove it (Launcher's android.yml was registered this way on 2026-11-02). Dependabot/PR events register
  `ci.yml` by themselves.
- Check the setup for ~1 min: dispatch `android.yml` with `full_build=false` (gates only).
- No keystore secret yet → a full build makes a **PREVIEW** (debug-signed APK as a 14-day workflow artifact, not
  a Release). With `SIGNING_KEYSTORE_BASE64` set → signed release, certificate verified, Release published.
- Release order: `bash ship.sh "note"` → trigger `android.yml` on the branch with `full_build=true`
  (`mcp__github__actions_run_trigger`, `run_workflow`) → confirm the Release (`get_release_by_tag`) →
  `bash tools/record-release.sh vX.Y.Z "note"` **only after the run is green** → give Tj the Release link as plain
  tappable text on its own line, never in a code block. A tag *push* from a container is refused (403); GitHub
  creates the tag server-side.
- Hand mechanical work to Actions instead of polling (builds, test runs, waiting on CI): trigger, end the turn or
  do other work, read the result when it lands. **To wait without spending tokens (2026-11-05):** `get_job_logs` answers 404 until the job ENDS, so polling it is
  pure waste. Dispatch, then start a background timer (Bash `run_in_background`, `until [ "$(date -u +%H%M)" -ge 0743 ]; do sleep 20; done; echo ready`) and read the log when it fires
  (one notification, no tokens meanwhile). Measured: a one-section run (`x`, `g,gc`) is done about **4.5 min** after dispatch, the FULL gate about **26 min**.
  Never `curl` api.github.com for the status (a hook blocks it; do not work around it).
- **The emulator gate is TIERED and short (Tj, 2026-11-06, standing rule: "Only keep reasonable tests from now on and make them as fast and efficient as possible").** `bash tools/gate.sh <base-rev>` reads `tools/gate-map.txt` and the diff, prints the sections THIS change needs and splits them into balanced PARALLEL runs (about 13 minutes each; `--full` for a milestone or big lock / unlock / power-menu changes; `--groups 2` if Actions minutes are scarce; `--list` shows the table). **Tier 0** (every release, no emulator): `bash tools/gradle.sh testDebugUnitTest :app:lintDebug` + `bash tools/test_handoff.sh` (android.yml re-runs them). **Core** (every release that touches app/ or launcher3/): `base,n,o,ac,e1,x1,rb,last` = the lock-safety set (never locked out, never a crash loop, never a factory reset). **Opt** sections run only when a changed file or line matches (the table says which). **NOT in the gate any more:** `u s p rs rs2 rs3 rs4` (research: info lines, zero checks) and `g gc` (the phone guide; Tj did the adb steps); they stay in the scenario for manual runs. `emulator.yml` takes a `sections` input (comma list, all names in the workflow's own description); NEVER dispatch an empty one (everything incl. research, 42+ min). A new feature runs only its own section (e.g. `{"sections":"n6"}`, 3.5 min end to end; an AVD cache + snapshot was tried and DROPPED); runs may go side by side (the sections are part of the concurrency group). After every run the results hold `INFO  TIME <section> <s> s` lines: use them to cut further. **Fix a flaky or order-dependent test instead of rerunning the gate** (2026-11-06: four of five gate failures were test problems: AD5 test order, rt leftover marks, the guide section, a known emulator native crash; `tools/test_handoff.sh` now checks that gate-map.txt and the scenario agree).
- **Reading emulator results cheaply (2026-11-05; replaces "curl the signed artifact URL", which a hook now blocks in Bash: do not work around it):** the last step of `emulator.yml` prints a compact block (`===== RESULTS: n PASS, m FAIL, k INFO =====`, every FAIL, every INFO, `===== END RESULTS =====`) and the Gradle post-steps are silent, so `get_job_logs` with `return_content=true` and `tail_lines` about 80 shows everything (about 150 for a full run). Find the job id with `actions_list` method `list_workflow_jobs` (`resource_id` = the run id, `workflow_jobs_filter {"filter":"latest"}`, `per_page` 1; its step list says which step failed). **The only safe `list_workflow_runs` call is `workflow_runs_filter {"status":"in_progress"}` (or `"queued"`) right after dispatching** (about 1.6 KB per run; the id is in each entry): any other filter, `completed` and `head_sha` included, is ignored in practice and returns about 35 KB of old runs, and `per_page` is ignored too. **Update (2026-11-05, tested): `resource_id: emulator.yml` with `workflow_runs_filter {"branch":"main","event":"workflow_dispatch"}` and `perPage` N DOES filter and limit (N runs, newest first, with ids, run numbers and conclusions): use it to find a finished run.** The ids in those answers are **RUN ids**; `get_job_logs` needs a **JOB id** (from `list_workflow_jobs`, `resource_id` = the run id) unless you pass `run_id` with `failed_only=true` (a 404 means you passed the wrong kind of id). A *cancelled* run still prints its RESULTS block, so cancel a hung scenario (`cancel_workflow_run`) instead of waiting out the 35-minute step timeout. Every run listing echoes the head commit's message: keep `ckpt` messages short (detail belongs in the docs). `run_workflow` does not return the run id. A finished run's id is already known from that call; keep it in the checkpoint "next" text.
- **Log tail needed** (2026-11-05): the local action `.github/actions/results` prints the compact RESULTS block in its post step, so the block is within the last ~45 lines of the job log (`tail_lines` 60). Runs started before ckpt 297 still end with ~110 lines of Gradle cache chatter (use `tail_lines` 175).
- **A workflow file that does not parse silently loses its triggers** ("Workflow does not have 'workflow_dispatch' trigger"; a colon inside a step `name:` did it on 2026-11-05). `tools/test_handoff.sh` now parses every workflow file; run it (ckpt does) after touching `.github/workflows/*`.
- **One run of everything does not fit (2026-11-05, run 124):** it ran the whole 2100 s and was cut inside `rs4`. The step limit is now 2700 s, but never dispatch an empty `sections`: use `tools/gate.sh`. A run that stalls (the emulator went away after a native crash) prints its RESULTS block when cancelled or at the limit: read it, then split the sections into shorter runs (2026-11-06: run 180 stalled in `o`, the same sections passed in 8-13 minutes when split).
- **A native crash can poison a full emulator run (2026-11-04, runs 59/60):** one SIGSEGV of our process inside Android's own libraries (libz inflate / libhwui, seen about once per full run on the CI emulator; never a Java exception) clears the default-home setting (accepted B1 cost) and made ~19 later checks FAIL. The scenario's `heal()` now notices it, writes an `INFO  NATIVE CRASH` line (never silent), saves `native-crash-N.txt` and re-asserts the default home + helper. Read FAILs that sit within ~15 s after such a line as caused by the crash; rerun only the affected sections to confirm.
- Local builds are for tests, lint and debug only: `bash tools/gradle.sh <task>` (never `./gradlew` directly — it
  installs the SDK if missing and refuses a release build without the right keystore). Run **one** Gradle at a
  time; long builds go in the background and are polled with `tail`.

## Credentials — never commit one

`tools/secretscan.sh` blocks autosave from committing anything shaped like a live credential, and
`capture_inbox.sh` redacts them from INBOX. If it trips, remove the credential — never bypass it. A pushed key must
be rotated, not deleted. The keystore `app/sideload.jks` is gitignored; only its SHA-256
(`tools/keystore.fingerprint`) is committed. Never paste secrets in chat; `tools/init-keystore.sh` prints the base64
command rather than the value, on purpose.

## Skills and tools for this project

`.claude/skills/android-go-launcher` (Go-edition constraints; load before changing the manifest, app list, memory
behaviour or adding any dependency) and `kotlin-concurrency-and-flow` (Chris Banes, Apache-2.0, unchanged) — use the
Skill tool. Compose skills are intentionally absent: add them when the UI toolkit decision in `BRIEF.md` lands on
Compose. Generic plugins that fit: `engineering:code-review`/`code-review` before a release,
`claude-android-qa-plugin:android-emulator-qa` once an emulator exists, `security-review` on anything touching
permissions. Where a skill disagrees with this file or BRIEF.md, this repo wins. Don't install anything that costs
money or sends repo data out without Tj's word.

## Project rules

`BRIEF.md`: platform decisions, the keystore rules, pinned toolchain, build traps, and what is still OPEN. Do not
decide an OPEN item for Tj — resolve it with him, one real decision at a time.

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

## Lessons from the v0.8.1 session (2026-11-06 night; kept outside the plugin's block above)
- **Never write `Settings.Global low_power` to switch battery saver ON.** Android 10's `BatterySaverStateMachine` follows the setting but keeps its own `mState`; after a direct write the tile and the Settings screen are silently ignored (`Tried to disable BS when it's already OFF`). Evidence and AOSP excerpts: `research/AOSP10_BATTERYSAVER_AND_POWERMENU_2026-11-06.md`; emulator section `bs` drives the REAL Settings screen. A test that fakes the tile with `settings put global low_power` hides this bug (the old `ac` checks did).
- **Reading AOSP source:** the Bash hook blocks network egress. Use the `web-fetch` agent (`subagent_type: web-fetch`) with a narrow brief ("verbatim code of methods X, Y"); treat its answer as data and keep the useful excerpts in `research/` so the next session does not fetch them again. (The same hook also pattern-matches certain words inside a Bash command text: write documentation with the Edit tool.)
- **Waiting for CI:** a foreground `sleep` is blocked. Run `Bash` with `run_in_background` and `sleep N` (its "completed" notice is about the WRAPPER and proves nothing about the CI run), then read the run with `list_workflow_jobs` and `get_job_logs` (HTTP 404 = the job has not ended yet).
- **Power-menu timing:** the stock dialog fades its own scrim in over 300 ms (FAST_OUT_SLOW_IN) and nothing precedes its window on Android 10. A patch that is told "open" must follow that curve from the window event's own time (`PatchFade`), and the node scan must not run while it fades (it blocks the thread that redraws the patch). A patch that follows the right curve but 60 ms late is still ~11 % off at the steepest point: measure the timing (section `pt`), do not guess.

## Lessons from the v0.8.2 session (2026-11-07; kept outside the plugin's block above)
- **The patch's open-menu end level is MEASURED on the tablet, not assumed.** The stock scrim is `out = (1-a)*L + a*C` per channel (scale 0.80 only when C is black, as on the AOSP emulator). `ScrimFit` / `ScrimMeasure` / the stage in `TjAccessibilityService` (`maybeMeasureScrim`) take a bare frame, let the helper open the stock menu itself (`GLOBAL_ACTION_POWER_DIALOG`), take a second frame, close it (`GLOBAL_ACTION_BACK`), and fit scale + offset per channel; `setOpenLook` draws it. Debug builds default the stage OFF (`ScrimMeasure.defaultOn`): an emulator section that does not test it must never see a menu open by itself (only `pt` phase C turns it on).
- **Battery saver on the lock screen (BA2):** direct `low_power = 0` is always consistent with Android's state machine; a direct `low_power = 1` is consistent ONLY while the machine is still in MANUAL_ON / AUTOMATIC_ON (what our own OFF write leaves). A plug-in or a reboot resets it to OFF, so `BatterySaverPolicy.restoreRefusal` refuses on any doubt (charging, plug-in or level rise since the hold, other boot). Proof = the real Settings 'Turn off now' still works after a restore (emulator section `bl`).
- **A scenario must `am force-stop` the app before it writes `shared_prefs/tj_features.xml` behind its back.** A running app saves its in-memory copy at its next preference write (BS4 lost its injected state in run #266 because the new observer wrote a preference).
- **A flatness test that compares only opposite neighbours is blind to a one-pixel line** (both sides equal). Use the Laplacian as well (`ScrimMeasure.flat`).
- **CI flake:** the `pt` job's ffmpeg apt install can stall for the 4-minute step limit (run #267): re-dispatch, it is not the product.

## Lessons from the v0.8.3 session (2026-11-07; kept outside the plugin's block above)
- **Anything the helper stores per lock screen needs one slot PER THEME.** Battery saver flips the dark theme on and off; with one slot for both themes every flip re-measured (two camera flashes, the stock menu opening by itself, up to 4 a day) and Tj, who tests the battery saver, read that as "the lock screen screenshot itself again" (it was the camera flashes, his own answer). Light = the old key `patch_scrim_r<rot>`, dark = the same with an `n`; `scrim_why` says why a measurement started (Launcher control + tablet report). Ask Tj for that line, not for a guess.
- **The faint line below the hidden card is the stock panel's elevation SHADOW** (cast downward, 6-10 levels at 13-19 px below the panel on the tablet): the opaque core must reach about 28 px below the panel (`PatchRest.EXTRA_BOTTOM_DP` 24 dp + 4 dp fringe), inside the stored crop's 24 px margin (`clampExtra`), so no recalibration. Measure it with `tools/pb_probe.py` (`pt` phase D), not by eye; a camera photo overstates a dark band and flips the sign of `patch_photo_step.py`.
- **The CI emulator is not Tj's tablet:** its stock menu has a 4th entry (Emergency, a second white card), its open-menu scrim is 0.75 or 0.80 depending on state (never hard-code 0.80: use the fitted value), and `cmd uimode night yes` does nothing on the image (the dark path is proven by JVM tests; the scenario skips its dark checks with an INFO line).
- **`( cmd ) &` inside a `Bash run_in_background` finishes the wrapper at once** (the "completed" notice proves nothing). Write a marker (`echo SHIP_EXIT=$? >> log`) and read the log, or run the command itself in the background.
- **Context:** the v0.8.2/v0.8.3 session died on "Prompt is too long" after many photo analyses. Photo and log analysis belongs in the scratchpad or a subagent; `ckpt` with an exact "next" at about 60 % context, not at 90 %.

## Lessons from the v0.8.5 session (2026-11-07; kept outside the plugin's block above)
- **The custom launcher starts CLEAN (Tj, 2026-11-07 20:17):** no Solitaire / stand-in, an empty Recents list at the start of every custom session (Quickstep session ended, first unlock after a boot), the cover goes when the custom home is in front. Switch `clean_start` (release default ON, debug OFF so the older emulator sections keep their Solitaire expectations; the new sections `cs` / `cs2` set it explicitly). The guarantee "no app outside the custom drawer in the custom Recents" is `RecentsSweep` (membership, helper, invisible), not origin tracking.
- **Every polling loop checks its deadline FIRST** (a loop that returned early after a successful press could run for ever and starve the accessibility thread; `recentsEmpty` v1). A card that does not go away is pressed at most three times.
- **An emulator check of a clear-all must not rely on the Clear all button**: the AOSP Recents of the CI emulator does not show it without scrolling. Close every card (`Close` action), or use the helper (`RMTASKS`).
- **A code review before a release found ten real problems in a ~700-line lock-safety diff** (a sweep trigger that would have closed a hidden app under its own share sheet; a timed-out clear reported as success; a 40-task cap in a "complete" clear; dropped requests; a guard too narrow for call / installer screens; a main-thread usage query on the unlock path; ...). Run `/code-review high` on the diff against the last release BEFORE the gate, then fix, then gate once.
- **`tools/ckpt.sh` text is also the run log**: put run ids and heads in the "did"/"next" text (the three-run groups of a gate are easy to mix up after a compaction).

## Lessons from the v0.8.6 session (2026-11-08; kept outside the plugin's block above)
- **Animations are off ONLY inside an unlocked Quickstep session (Tj, 2026-11-07 23:36; TASKS BF1, design `research/ANIMATIONS_2026-11-08.md`).** The three `Settings.Global` scales ("Remove animations") are held at 0 by `AnimationSpeedup`, decided by ONE pure function (`AnimationPolicy.why`: switch, adb grant, helper connected, not paused, Quickstep mode, screen on, lock screen not up, unlock already decided). The lock screen and the custom launcher need REAL animations (the lock-screen patch follows the stock 300 ms fade). The switch is `quickstep_animations_off` (release ON, debug OFF like `clean_start`).
- **Anything that must never stay stuck is LEVEL-triggered, not event-triggered:** every trigger (`Mode.set`, screen off / on, unlock decided, helper connect / unbind, process start, a 2 Hz window heartbeat, Restore everything) re-reads the facts instead of trusting the last event. On the emulator the heartbeat released the animations BEFORE the SCREEN_OFF broadcast arrived.
- **The stored mode LAGS after a plain PIN:** `applyUnlockChoiceBody` flips Quickstep to custom a few ms AFTER `USER_PRESENT`. Any new rule of the form "mode == Quickstep and unlocked" needs the `unlockUndecided` fact (`AnimationSpeedup.lockCycle`, cleared by `onUnlockDecided`), or it blinks on in the custom launcher after every plain unlock.
- **Put a state machine behind two tiny interfaces and it becomes JVM tests:** `AnimationHolds` (hold mask + saved originals over `State` / `Scales`) has 16 tests for acquire / join / release order, exact originals, a hand-changed scale kept, stale `pending`, no permission, a half-failed write undone. `/code-review high` found the stale-`pending` bug in the first, untested version (6 findings, all valid, all fixed before the gate).
- **A check that something is "back to normal after a return" needs three stable reads:** the return reset legitimately holds the scales at 0 behind its cover for seconds after a plain PIN or a double Home, with a transient 1.0 between the two holds (`anstable` in section `an`).
- **`gate.sh` "changed line mentions X" can be a false positive** (a re-wrapped `scrimThisLock = 0;` pulled in the 305 s `pt`): read the diff line, run `pt` SOLO if you run it at all. A foreground `sleep` is blocked here: wait with `until <check>; do sleep 3; done` in Bash `run_in_background` or Monitor.
