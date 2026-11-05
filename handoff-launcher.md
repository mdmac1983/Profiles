# active-memory handoff: Launcher (Android 10 Go launcher: v0.7.1 shipped, first OTG cable session in progress)

**Handoff #1** · 2026-11-05 · Lineage: #1 (2026-11-05): resumed the cut-off Launcher work, shipped v0.7.1, then ran Tj's first OTG cable session (helper unreachable on this tablet, first-open crash found, file-mailbox plan, DuraSpeed test running)

## 0. Instructions for Claude (read first)

You are continuing work from a previous chat. That chat is gone; this file is the complete context and the source of truth.

1. Read this whole file before replying.
2. Follow sections 3 (Style), 4 (Hard rules) and 5 (Corrections) in every reply, for the rest of this chat. They override your defaults.
3. Use the values in section 8 exactly. Never round, re-estimate, or "correct" them.
4. Do not suggest anything listed in section 7 (Changed / rejected) again unless the user brings it up.
5. Code word: None active. If the user types /amcodeword, start every reply with the phrase they choose (default "Yes Boss!").
6. Your first reply: at most 5 lines covering the goal, the current state, and the next step (section 11). Mention any files from section 13 that were not attached. Ask the questions in section 12 if there are any. End with "Ready to continue with <next step>?" Then wait for the user's go.

## 1. Mission
- **Goal:** Tj's custom home-screen launcher `com.tj.launcher` for a 2 GB Android 10 Go tablet (MediaTek board `tb8168p1_bsp`). Sideloaded, built and signed by GitHub Actions, written by Claude over many sessions. Repo: github.com/tjshea90/Launcher, working branch `ccr-888039cc-xwzdeh`, mirrored to `main`.
- **Done looks like:** (Tj's standing words) it must "look and function IDENTICALLY to the stock Quickstep v10" and never break the tablet. Practically now: the custom launcher runs on the real tablet without crashing, its shell-helper features work there, and Tj can set the tablet up in one short cable session.
- **Why it matters / context:** Tj only gets short windows with the OTG cable (a phone running the "ADB OTG" app plugged into the tablet). Claude can be cut off by usage caps at any moment, so everything is saved in the repo: `CHECKPOINT.md` (where we stopped), `TASKS.md` (job list + evidence), `HANDOFF.md` (one-page entry), `INBOX.md` (Tj's raw messages), `CLAUDE.md` (working agreement).
- **Right now:** a LIVE cable session. v0.7.1 is released. Cable steps 1 to 9 are done; step 10 (is the helper still alive about 30 minutes after 14:17:57 with DuraSpeed off?) is pending.

## 2. About the user (as relevant to this work)
- Tj (GitHub `tjshea90`), owner of the project. Target device is Tj's own tablet; Tj runs `adb shell` on it from an Android phone (ADB OTG app) over a USB OTG cable.
- Sideloads APKs; sends phone screenshots and photos of the tablet instead of text; wants the stock look.
- Uses several Claude accounts/devices; usage caps interrupt sessions.
- Tj's other apps target Android 16; THIS project targets Android 10 (API 29): the project's `CLAUDE.md` wins.
- Pronouns for Tj are not stated: use "Tj" or "you", no gendered pronouns.

## 3. Style & communication
- **Language:** English.
- **Tone:** plain, direct, short.
- **Reply length:** during a cable session ONE short step per message (one sentence of result, then one command or one tap), then WAIT. Outside the cable session: concise, answer every open message.
- **Formatting:** each command alone in a code block so it can be copied; the Release link as plain tappable text on its own line, never in a code block; every command goes in the LAST message of the turn.
- **Working style:** save facts to the repo before replying (TASKS.md + `bash tools/ckpt.sh "did" "next"`). Do the bookkeeping tool calls FIRST and write the step text LAST, with no tool call after it.
- **Avoid:** long explanations, findings or options inside a step message, "the lines above" instead of repeating the command, more than one step per message, gendered pronouns for Tj.

## 4. Hard rules (word for word)
1. "Remember these photos for reference at all times, even in a new Claude session." (Tj, 2026-11-02. The stock photos in `research/reference/stock/INDEX.md` are the ground truth for the look: 5 columns, 8 rows, no dock, no caret, row pitch 113 px, scrim 50% white; measure before saying a look is matched.)
2. "make sure claude can save and use all necessary information from the tablet when connected." (Tj, 2026-11-05)
3. "This is way too much information for my short time with the cable. Give me short step by step tasks and wait for me" (Tj, 2026-11-05)
4. "SAFETY (overrides everything): before ANY release be highly confident it breaks nothing; failsafe that removes every modification and restores Android to its pre-launcher state. A friend's launcher glitched the lock screen (could not log in) and brought back all quick-settings tiles: that must never happen. Must be revertible if anything goes wrong." (TASKS.md L10, Tj's words)
5. "It is important that this project doesn't break anything or require me to factory reset my tablet." (Tj, 2026-11-05)
6. "GitHub actions should do all the app compiling and apk making using their usage when possible. Claude codes the app, GitHub makes the apk." (Tj's rule on all repos)
7. "If Tj sends several messages while a task is running: keep doing what you are already doing, add each new message as the next step (write it into TASKS.md in Tj's words, ckpt), never drop or forget one, and answer every one of them, including ones from earlier in the conversation that are still open (re-list them at the end of a reply until they are done)." (CLAUDE.md, 2026-11-03)
8. Live cable session (CLAUDE.md, 2026-11-05): send ONE short step per message and wait for the reply; no findings, no options, no explanations in the step message; every command in the LAST message of the turn.
9. "Do not decide an OPEN item for Tj" (BRIEF.md): resolve it with Tj, one real decision at a time.
10. Never commit a key, the keystore `app/sideload.jks` or `local.properties`; never regenerate the keystore; never add a `push:` trigger to a workflow; never `curl` api.github.com (a hook blocks it: do not work around hooks).
11. First action of every Claude Code session: `bash tools/install-hooks.sh`. Checkpoint after every step. A new request from Tj goes into TASKS.md in Tj's words first.
12. Files from the tablet: run `bash tools/tablet-intake.sh <file>` and read `research/tablet/LATEST.md` first. Never commit a raw report or a screenshot by hand.
13. Release routine: `bash ship.sh "note"` → trigger `android.yml` with `full_build=true` → confirm the Release → `bash tools/record-release.sh vX.Y.Z "note"` only after the run is green. The release gate is TWO emulator runs, A and B (section 8).
14. User preference: "Review all available plugins, skills, and connectors at your disposal. Use any plugins, skills, and connectors freely if they can improve app building, app UI and theming, efficiency and accuracy of code, efficiency of Claude usage, or anything else to improve Claude code for building android apps. Ignore skills, plugins, and connectors if they contradict one another; choose the best one."
15. User preference: "All projects should have ladder systems that auto save all data frequently to GitHub, and a new Claude code session can cold resume the project with no data or progress being lost. ... Assume Claude can be interrupted at any time, and plan for a resumption protocol."
16. User preference: "I prefer longer sessions and more usage if it makes a better product and fixes bugs, but efficiency and saving Claude usage where possible."

## 5. Corrections log
| # | Claude did | The user wanted |
|---|---|---|
| 1 | Wrote the exact command only in text placed before a tool call, so it did not show on Tj's screen | Tj: "You didn't tell me the one line" → every command goes in the LAST message of the turn, with no tool call after it |
| 2 | Wrote "send a screenshot of the two diagnostic lines above" without repeating them | Tj: "What two diagnostics" → repeat the exact commands in the message |
| 3 | Sent long messages with findings, options and several commands while Tj was at the cable | Tj: "This is way too much information for my short time with the cable. Give me short step by step tasks and wait for me" → one step per message, then wait |

## 6. Decisions
| Decision | Why |
|---|---|
| v0.7.1 (code 18) + PhoneGuide 1.2 released and recorded in BUILDLOG.md; PG1 to PG4 ticked with proof | the phone guide for Tj's short cable session; gate evidence on identical product code |
| Release gate = two parallel emulator runs A and B (section 8) | one run of everything does not fit (about 42 min, cut at 2100 s) |
| Sockets (unix and TCP) are unusable on this tablet | SELinux Enforcing: unix connect gives `Permission denied`; TCP gives `socket failed: EPERM` |
| Claude's plan, NOT yet confirmed by Tj: a file mailbox in `/sdcard/Android/data/com.tj.launcher/files` for app and shell | proven readable and writable in both directions on the tablet |
| Claude's plan, NOT yet confirmed by Tj: fix the keep-alive notification icon and make keep-alive default OFF until proven | it crash-looped the launcher on first open; the crash breaker fell back to stock |
| The shell helper is treated as ephemeral | it died within about 15 minutes even with the cable connected |
| DuraSpeed switched OFF as a reversible test | suspect for the helper's death; Tj asked and then ran the line |
| Permissions applied by hand from the phone (the guide's FALLBACK commands as one line) | the helper was unreachable; permissions persist after unplugging |
| Screenshots are not committed; their facts go into TASKS.md | project rule and privacy |
| Tj decided earlier (INBOX, 2026-11-05): "Close g3d." and the phone guide must be simple, logical in order and short for limited cable time | Tj's call |

## 7. Changed / rejected
- One "everything" emulator run as the release gate → two runs A and B (CLAUDE.md, HANDOFF.md and the workflow description fixed; step limit 2100 → 2700 s).
- Socket/TCP channel between launcher and helper → rejected for this tablet; file mailbox planned instead.
- ❌ "TCP failed because INTERNET is missing": INTERNET is `granted=true`; the EPERM is a ROM rule.
- ❌ "The helper dies because the cable is unplugged": it died while the cable was connected.
- ❌ "DuraSpeed keeps the accessibility service from binding": `Bound services:{}` stayed empty with DuraSpeed off (step 9).
- ❌ "The tablet is LineageOS": it is a MediaTek `alps` user/release-keys build.
- ❌ Declined review finding: "Take me to my next step" skips cards of a closed group (design: FALLBACK, EXTRAS and UNDO are outside the main path).
- Long multi-part messages → one short step at a time (Tj's rule).

## 8. Data & facts (exact)
**Release**
| Item | Value |
|---|---|
| Release | https://github.com/tjshea90/Launcher/releases/tag/v0.7.1 (built by `android.yml` run 20, id 41994326987, commit `4686fda`) |
| Launcher APK | `Launcher-v0.7.1.apk`, versionCode 18, 8,888,486 bytes, sha256 c09785892e8aecb2a8a5be718cb5f782ae1008e78d8c2a40cfb9da56b860f07f |
| Phone guide APK | `PhoneGuide-v0.7.1.apk` (guide 1.2, code 3), 23,859 bytes, sha256 4a58b9278c12eddd6a07e7242093fa19951f8d58a6eb9dda7a232eeee8682f20 |
| Last checkpoint | ckpt 352 on branch `ccr-888039cc-xwzdeh` |
| Release gate A | `base,legacy,x1,e1,y2,b6,b4,n,n6,o,t,u,s,p,x` |
| Release gate B | `v,g,ka,rt,im,gc,pf,rb,last` (research sections `rs` to `rs4` are not part of it) |
| Gate evidence for v0.7.1 | runs 124, 128, 126, 123, 120 on product code identical to the release; unit 244, intake test 57, uinode test 20, handoff self-test 31 |

**Tablet** (from the first real report, saved in `research/tablet/2026-11-05/`)
| Item | Value |
|---|---|
| Model | `alps 711 / tb8168p1_bsp`, Android 10 (API 29), patch 2021-07-05 |
| Fingerprint | `alps/vnd_tb8168p1_bsp/tb8168p1_bsp:10/QP1A.190711.020/1719850017:user/release-keys` |
| Build type / SELinux | `user` build, release keys; `getenforce` = Enforcing; bootloader locked, verified boot green; unencrypted |
| Memory / screen | low-RAM Go device, memory class 128 MB; 600x973 at 170 dpi, font scale 1.2; max users 1 |
| System apps | MtkSettings, MtkSystemUI, MtkLauncher3GoIconRecents (stock launcher), MtkBrowser; SwiftKey 9.13.16.5 installed |
| Launcher uid | 10243 (`u0_a243`); shell uid 2000, groups include 1004 input, 1007 log, 1011 adb, 1015 sdcard_rw, 1028 sdcard_r |
| Clock | tablet and phone show the same time; tablet timezone EST |

**Crash (first open of the launcher)**
- `logcat -d -b crash`: three crashes at 12:48:47 (pid 9473), 12:48:50 (pid 9548), 12:48:54 (pid 9643): `RemoteServiceException: Bad notification(tag=null, id=7471) posted from package com.tj.launcher, crashing app(uid=10243 ...): Couldn't create icon: StatusBarIcon(icon=Icon(typ=RESOURCE pkg=com.tj.launcher id=0x7f07009a) visible user=0 )`.
- id 7471 = the keep-alive foreground notification (`KeepAliveService.NOTIFICATION_ID`); `0x7f07009a` = `R.drawable.tj_ic_blank`, an EMPTY vector drawable used as the invisible small icon. The tablet's vendor SystemUI cannot create the icon; Android then crashes the posting app.
- Report state afterwards: launcher 0.7.1 (18), mode `quickstep`, crash breaker tripped: yes (`CrashGuard`: 3 crashes in 2 minutes → Quickstep mode + all modifications restored), `keep_alive: false`, shell helper reachable: no, accessibility helper bound: no. So the tablet currently runs STOCK behaviour; the custom launcher is OFF until switched back in Launcher control.

**Helper and channel facts**
- Helper = `com.tj.launcher.ShellServer`, started by one adb line. Abstract unix socket `tj_shell_helper`; TCP ports 47631 to 47635; its log goes to `/data/local/tmp/tj.log` when started with the log line below.
- Alive helper: the launcher logs (tag `TjReset`) `shell helper not reachable: unix: IOException Permission denied; tcp 47631: SocketException socket failed: EPERM (Operation not permitted)`. No helper: `unix: IOException Connection refused`.
- Helper lifetime: started about 13:02 (alive at 1:02, gone before 13:08); started 13:31 (job `[1] 12440`; alive 13:32, gone by 13:47:40, `ps` count 0 at 14:13, log has no exit line = killed silently); restarted 14:17:57 EST (job `[1] 14527`).
- Mailbox evidence: `/sdcard/Android/data/com.tj.launcher/files/probe/tablet-report.txt` is `-rw-rw---- u0_a243 sdcard_rw 32461` and the shell could copy it to `/sdcard/Download/launcher-probe/`; a file the shell created in that folder (`from-shell.txt`) came out `-rw-rw---- u0_a243 sdcard_rw`: both directions work.
- By-hand grants done at about 1:37: WRITE_SECURE_SETTINGS, DUMP, CHANGE_CONFIGURATION, READ_EXTERNAL_STORAGE, INTERNET all `granted=true`; four app-ops allowed; `dumpsys deviceidle whitelist +com.tj.launcher` = Added; `enabled_accessibility_services` = `com.tj.launcher/com.tj.launcher.TjAccessibilityService` (enabled, `Bound services:{}`, `accessibility_enabled` = 1); `cmd package compile -m speed -f com.tj.launcher` = Success.
- DuraSpeed: `setting.duraspeed.enabled` was 1 in global AND system; set to 0 in both at about 14:17.

**Commands (verbatim, for the immediate next steps)**
- Step 10 check: `date; ps -A -o ARGS | grep -c '[S]hellServer'` (prints the time, then 1 = helper alive, 0 = dead)
- DuraSpeed back on: `settings put global setting.duraspeed.enabled 1; settings put system setting.duraspeed.enabled 1`
- Restart the helper with a log: `pkill -f com.tj.launcher.ShellServer; CLASSPATH=$(pm path com.tj.launcher | head -1 | cut -d: -f2) setsid nohup app_process /system/bin com.tj.launcher.ShellServer >/data/local/tmp/tj.log 2>&1 & sleep 4; cat /data/local/tmp/tj.log`
- Why the launcher cannot reach the helper: `logcat -d -s TjReset:I | grep -i 'not reachable' | tail -1 | cut -c1-180`
- Crash log: `logcat -d -b crash | tail -40`
- The ADB OTG app shows a scrambled echo of long lines (the tablet's shell redraws them sideways); that is normal, the line still runs.

## 9. People, terms & names
- **People:** Tj = the user and owner. Claude = the assistant.
- **Terms & nicknames:** OTG cable = USB OTG cable between phone and tablet; "the helper" = `ShellServer`, a shell-uid process started over adb; "the breaker" = `CrashGuard`; Quickstep = the stock launcher mode; custom = this launcher's own mode; DuraSpeed = MediaTek's background-app manager; "Step N" = the numbered cable steps in this chat.
- **Names in use:** task IDs in TASKS.md: KA1 (keep-alive icon crash fix), MBX1 (file mailbox), UX1 (report publish message + share button), ACC1 (accessibility not bound), CBR1 (breaker stays tripped after an update), HLP1 (helper dies), HLP2 (no TCP), HLP3 (helper unreachable), CR1 (first-open crash), RS1 (return reset without notification access), PG5 and PG6 (phone guide notes), L10 (safety), SET1 (Settings clone).
- **Files:** `CHECKPOINT.md`, `TASKS.md`, `HANDOFF.md`, `CLAUDE.md`, `INBOX.md`, `BUILDLOG.md`, `research/tablet/2026-11-05/` (`tablet-report.txt`, `FACTS.md`, `INTAKE.txt`), `research/tablet/LATEST.md`.
- **Tools:** `tools/ckpt.sh`, `tools/tablet-intake.sh`, `ship.sh`, `tools/record-release.sh`, `tools/install-hooks.sh`; workflows `android.yml` (release build) and `emulator.yml` (sections input).

## 10. Work state
| Item | Status | Version / location | Notes |
|---|---|---|---|
| Release v0.7.1 | final, published | tag `v0.7.1`, BUILDLOG.md | not installed fresh-working on the tablet: custom mode is off (breaker) |
| First tablet report | final, saved | `research/tablet/2026-11-05/` | 3 red flags: helper not running, accessibility not bound, breaker tripped |
| KA1 keep-alive icon crash | NOT started | TASKS.md | plan: transparent PNG icon, default OFF, JVM test; Tj to decide default |
| MBX1 file mailbox | NOT started | TASKS.md | both directions proven on the tablet; client + server + tests + emulator proof needed |
| UX1, ACC1, CBR1, PG5, PG6, HLP1, RS1 | open | TASKS.md | see TASKS.md for evidence and next steps |
| DuraSpeed test | RUNNING | tablet | DuraSpeed OFF, helper restarted 14:17:57; waiting for step 10 |
| Permissions / app-ops / compile | done by hand | tablet | persist across unplugging |
| Launcher mode on the tablet | stock (Quickstep) | breaker tripped | Tj switches back to custom in Launcher control after v0.7.2 |

## 11. Next steps
1. **Next action:** get Tj's step 10 result (about 30 minutes after 14:17:57 on the tablet clock, cable plugged in): `date; ps -A -o ARGS | grep -c '[S]hellServer'`. Read it: 1 = the helper survived with DuraSpeed off; 0 = DuraSpeed is not the killer. Then tell Tj one thing: whether to turn DuraSpeed back on (give the undo line). One step per message.
2. Tell Tj plainly that the launcher is in stock mode because the crash breaker tripped, and that custom mode returns after v0.7.2 via the switch in Launcher control.
3. Build v0.7.2 in this order: KA1 (new icon, default OFF, test), MBX1 (file mailbox, helper treated as ephemeral), UX1, ACC1 diagnostics, CBR1, PG5/PG6. Run the two-run gate A and B, then `bash ship.sh`, `android.yml`, `tools/record-release.sh`. Verify on the tablet in a short cable session.
4. Keep the DECISIONS list for Tj (section 12) visible; never decide those for Tj.
5. Start of any Claude Code session: `bash tools/install-hooks.sh`, read `CHECKPOINT.md`, `TASKS.md`, `HANDOFF.md`; checkpoint after every step.

## 12. Open questions ⚠️
- Keep-alive default OFF until it is proven on this tablet? (Claude recommends OFF; Tj decides; the wish behind it is a faster unlock.)
- Build the file mailbox (MBX1) as the way around the blocked socket? (Claude's plan; Tj has not been asked directly.)
- Should DuraSpeed go back on after the test? (Depends on step 10; Tj's call.)
- Why is the accessibility service enabled but not bound? (Unknown; DuraSpeed is ruled out.)
- The standing DECISIONS waiting on Tj: Power menu v2 (build only if the brief flash of the stock menu bothers Tj; the side-key read test has not been run on the tablet), SET1 Settings front door go or no-go, L5 quick-settings tiles check, U3 and V5 unlock and lock-screen numbers, and RS1: after a double-Home on the tablet, do notifications go away?

## 13. Re-attach checklist
- [ ] Nothing to attach if the new chat is Claude Code on the repo tjshea90/Launcher: it reads `CHECKPOINT.md`, `TASKS.md` and `HANDOFF.md` itself.
- [ ] If the new chat has NO repo access: attach `CHECKPOINT.md`, `TASKS.md` and `research/tablet/2026-11-05/FACTS.md` (the phone screenshots are not saved anywhere; their facts are in TASKS.md and section 8 here).

---
<sub>Audit: 13/13 sections · RULES_N rules · CORR_N corrections · DATA_N data points · secrets removed: none found · generated by active-memory</sub>
