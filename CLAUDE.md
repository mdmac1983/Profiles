# Launcher — working agreement

Tj's **Android 10 (API 29) Go-edition home-screen launcher**, `com.tj.launcher`, Kotlin, sideloaded (not on
the Play Store). GitHub Actions builds the APK; Claude writes the code. Worked on across many Claude Code
sessions — a new one may start on another account or device, or right after a usage cap killed the last one
mid-step. **Everything below exists to make that handoff lossless.** What is decided about the platform and what
is still open: `BRIEF.md`. The job list: `TASKS.md`. Where the last session stopped: `CHECKPOINT.md`.

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

## The save ladder (assume you can be cut off at ANY moment)

| Rung | What | Who runs it | Saves |
| :- | :- | :- | :- |
| 0 | `tools/capture_inbox.sh` (UserPromptSubmit) | hook | every message Tj sends, verbatim (keys redacted), committed + pushed before any work |
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

## Branches

Claude Code gives every session its own branch, so work can strand on a branch nobody opens. Rules:
- Develop and push on **the branch the session was assigned**; `push.sh` pushes it after every checkpoint.
- `tools/MIRROR_MAIN` (`on`/`off`, committed) makes every push also fast-forward `origin/main` (plain push, never
  forced). **On** since 2026-11-02 (Tj approved), so a fresh session that just opens the repo sees the real state;
  GitHub can only dispatch workflows that exist on the default branch, which is now `main` itself.
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
  do other work, read the result when it lands.
- **The Emulator check is fast when you ask for a section** (N9, 2026-11-04): `emulator.yml` has a `sections` input (comma list: `base,legacy,x1,e1,y2,b6,b4,n,n6,o,t,last`; empty = everything, ~15 min, the release gate).
  A new feature runs only its own section (e.g. `{"sections":"n6"}`, measured 3.5-3.6 min end to end; an AVD cache + snapshot was tried and DROPPED: no measurable gain, and a snapshot boot changed the drawer order). Two section runs may go side by side (the sections are part of the concurrency group).
  Only the release run (empty) is the full gate. Read results via `download_workflow_run_artifact` -> curl the signed URL into the scratchpad.
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
