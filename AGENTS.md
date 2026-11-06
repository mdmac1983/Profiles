# AGENTS.md

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

<!-- handoff:learnings:end -->
