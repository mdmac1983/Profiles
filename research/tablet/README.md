# research/tablet — what the real tablet told us, kept in the project

**Why this exists (Tj, 2026-11-05: "make sure claude can save and use all necessary information from the tablet"):** the tablet report is the only way Claude sees the real tablet.
A report that stays in a chat is lost when the session ends, and a raw one could carry a serial number, an address or an e-mail address into a repository that may be public.
So everything Tj sends goes through one script that **saves it here, redacted, with a facts file Claude reads first**.

## For the next Claude session (do this, in this order)

1. Tj sends a file: normally `tablet-report.txt` (the small one), sometimes the whole `launcher-probe` folder or a zip of it (the PC route `tools/adb-all.sh`, or "4. Collect EVERYTHING").
   It arrives as a chat attachment; if he **pasted the text** instead, pipe it in with `-`.
2. Run the intake (it never extracts a zip to disk and never trusts a file name):
   ```bash
   bash tools/tablet-intake.sh /path/to/tablet-report.txt        # or the folder, or the zip
   bash tools/tablet-intake.sh - < pasted.txt                    # the text itself
   ```
3. Read **`research/tablet/LATEST.md`** (the same as the newest `<date>/FACTS.md`): **red flags first**, then the answers to the open questions (side-key test, parser self-check, encryption, users, adb over the network, tiles, ...).
4. Commit it so the next session has it: `git add research/tablet && bash tools/ckpt.sh "tablet intake <date>" "what to do about the red flags"`.
5. Write what the answers decide into `TASKS.md` / `BRIEF.md` (for example: the side key was seen -> Power menu v2 is possible; tell Tj and ask for his go). Do not decide an OPEN item for Tj.

## What is kept, what is not

| Kept (small text, redacted) | Listed only (name, size, sha256) | Dropped |
| :- | :- | :- |
| `tablet-report.txt`, `launcher-probe.txt`, `status.txt`, `shots-*.xml` (screen structure) | `*.apk` system apps (they are the ROM's, big, and not ours to commit) | everything else; screenshots (`.png`) unless Tj agrees to `--keep-images`, because a picture cannot be redacted |

Redacted before saving: serial / IMEI / android_id / Bluetooth address values, MAC addresses, IPv4 addresses (not 127.0.0.1 / 0.0.0.0), e-mail addresses, Wi-Fi names, any 14-19 digit number.
If anything shaped like a live credential is left after that, the **whole intake is refused** and nothing is saved (remove it, run again). Size caps: text 12 MB, screen file 200 KB, kept image 300 KB (4 MB in all).

## Layout

```
research/tablet/
  LATEST.md             the newest FACTS.md (read this)
  <date>/               one folder per intake (a second one on the same day is <date>-2)
    FACTS.md            red flags, then the table of answers
    tablet-report.txt   the report, redacted
    INTAKE.txt          what was kept, what was skipped, how many redactions
    apk-list.txt        system apps that came along: name, size, sha256 (the files are NOT kept)
    shots-*.xml         screen structure of the stock Settings pages
```

The rules are pinned by `tools/test_tablet_intake.sh` (part of `tools/test_handoff.sh`, so every checkpoint runs it); the fixture is `tools/testdata/tablet-sample/` (all private-looking strings in it are invented).
How the report is made, and what Tj taps to make it: the phone guide (`phoneguide/src/main/assets/steps.txt`, PART 3) and `TabletReport.java` / `ReportState.java` / `probe-commands.txt` in the app.
