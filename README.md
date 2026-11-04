# Launcher

Android 10 (API 29) Go-edition home-screen launcher (`com.tj.launcher`). Built by GitHub Actions; developed across
Claude Code sessions with a lossless checkpoint/handoff system. Start with **`HANDOFF.md`** (new session, no context), then `CLAUDE.md`, `BRIEF.md`, `TASKS.md`,
`CHECKPOINT.md`.

```bash
bash tools/install-hooks.sh                      # once per session
bash tools/gradle.sh testDebugUnitTest           # tests (installs the SDK on first use)
bash tools/ckpt.sh "what I did" "what is next"   # checkpoint after every step
bash ship.sh "note"                              # gate + push, then trigger android.yml
```
