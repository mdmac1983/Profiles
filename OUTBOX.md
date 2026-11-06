# OUTBOX - what Claude told Tj (the last reply of every turn), newest last. Written by tools/capture_outbox.sh on the Stop hook; key-shaped strings are redacted and long replies cut in the middle. The mirror of INBOX.md.

## 2026-11-06T19:36:04Z
> Running the repo's self-test (now including the outbox checks) and measuring the briefing size.
<!-- sha:59b18ecc52d9 -->

## 2026-11-06T19:38:19Z
> A is recorded: v0.8.1 will have the helper calibrate the Screenshot patch by itself, and the grey cover (B) is dropped. The session is closed and a new one can start now.
>
> **Why your prompts and findings looked unsaved.** They were saved, but on side branches, and for two separate reasons:
> 1. **`main` was stale.** A new session starts from `main`, and `main` was stuck at v0.7.4 (251 commits behind). The cause was a switch (`MIRROR_MAIN`) turned off during the v0.7.3/0.7.4 testing and never turned back on. Your new session therefore opened without about 100 lines of your prompts, about 160 lines of findings and tasks, and three releases of code. All of it was on the older branches.
> 2. **Claude's replies were never saved at all.** Only your prompts were. My findings survived only if I also wrote them into `TASKS.md` by hand, so analysis in a chat that died was lost.
>
> **What I fixed:**
> - **`main` and the switch:**
>   - `main` is now level with the working branch (a plain fast-forward, nothing rewritten).
>   - The switch is on again, so `main` follows every push.
>   - I did this because you asked for the fix and you approved this mirror on 2026-11-02. If you don't want it, say so and I'll undo it.
> - **Capturing replies:**
>   - Claude's replies now go into a new `OUTBOX.md`, the counterpart of `INBOX.md`. The last reply of each turn is saved. Long replies are also saved after tool calls, so a chat that dies mid-turn still leaves its findings.
>   - The start-of-session briefing shows the end of it.
>   - It is tested, including on this session's real transcript. I can only confirm it live once this turn ends, so the new session's first step checks that `OUTBOX.md` holds this reply.
> - **Docs:** the rules and the cause are written into `CLAUDE.md`, `HANDOFF.md` and `TASKS.md` (AY1).
>
> **For the new session:** it will start from `main` with everything. The plan is v0.8.1: rotation-only tile at every lock, Wi-Fi/Bluetooth retried instead of given up on, and automatic calibration. The plugin handoff file (`.handoff/`) is from 19:24 and I didn't regenerate it; `CHECKPOINT.md` and `TASKS.md` are current.
>
> **Still waiting on you:**
> - **Battery saver tile:** the three checks from my last message.
> - **Old photos in the public repo:** remove them from the tree, make the repo private, or purge history?
> - **v0.8.0 on the tablet:** the checklist.
<!-- sha:e87e8459be6b -->
