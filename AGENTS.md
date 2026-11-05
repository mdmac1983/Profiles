# AGENTS.md

<!-- handoff:learnings:begin -->
## Session Learnings (auto-updated by handoff)

### Implicit Rules
- Stack: Android 10 API 29 Go-edition launcher com.tj.launcher, Java/Kotlin, tablet MT8168 armv7 2 GB, sideloaded; separate phoneguide module (plain views, no permissions) runs on Tj's phone
- Project CLAUDE.md wins over generic skills and user-level prefs: Tj's Android 16 preference does not apply, this app targets Android 10
- First action every session: tools/install-hooks.sh (hooks not auto-loaded under shared session root); verify auto-checkpoint commits appear after edits
- Save ladder: capture_inbox hook records every Tj message, autosave hook commits+pushes after tool calls, tools/ckpt.sh did/next after each step (keep messages SHORT: run listings echo commit message), ship.sh at versions, GitHub Actions builds and signs, tools/record-release.sh only after green run
- Release order: ship.sh, then android.yml workflow_dispatch full_build=true on main, then get_release_by_tag, then record-release; tag push from container is refused (403), GitHub creates tag; keystore only in GitHub Secrets, missing local app/sideload.jks is expected, never regenerate
- Emulator checks: emulator.yml manual dispatch with sections input; results = RESULTS block in job log; get_job_logs needs JOB id from list_workflow_jobs not run id; INFO NATIVE CRASH line precedes crash-caused FAILs; section order base,legacy,x1,e1,y2,b6,b4,n,n6,o,t,u,s,p,x,v,g,rs,ka,rs2,rt,im,rs3,gc,rs4,pf,rb,last
- Tick TASKS.md box only when written, tested, committed, proving test named; new Tj request goes into TASKS.md in his words before code; answer every Tj message and re-list open ones at end of reply
- Tablet files: tools/tablet-intake.sh redacts serial/IMEI/MAC/IP/email/Wi-Fi and refuses credentials; read research/tablet/LATEST.md first; never commit raw report or screenshots by hand
- Never commit credentials (secretscan blocks autosave); never add push triggers to workflows (Actions minutes scarce)
- Stock launcher photos in research/reference/stock/INDEX.md are ground truth for look; measure against them before claiming a match
- Use tools/gradle.sh never gradlew directly; one Gradle at a time; read BUILD line and test-result XML not background exit code
- Hooks block curl to GitHub API and variable rm -rf: do not work around

### Key Decisions
- Decision: treat prior-session branch as merged, ignore orphan branches — Reason: git shows 0 commits missing from HEAD; orphans predate 2026-11-02 re-root, merging corrupts history
- Decision: finish cut-off change before new work — Reason: autosave committed half-written change, CHECKPOINT.md stale, resume briefing flagged interrupted mid-change
- Decision: key-test reports 'shell cannot read input devices' separately from 'key not seen' (helper returns total getevent line count) — Reason: false 'not seen' could end Power menu v2 idea for wrong reason
- Decision: key-test line caps counted separately (24 device lines, 40 key events) — Reason: tablet with many input devices could push KEY_POWER out of one shared cap, false negative
- Decision: intake flags notification-reader mismatch only if sample has dotted user-0 package records reader missed, or no record parses — Reason: reader skips system pkg=android by design; stock emulator only has those (run 121 evidence)
- Decision: emulator scenario reopens setup screen and scans in small slow steps; guide checks find nodes via tools/uinode.py row anchor plus seek loop — Reason: autorun screenshot job leaves Settings in front; uiautomator dump lists only on-screen nodes; runs 117/118 failures were test bugs not app bugs
- Decision: guide adb-shell switch rewrites command boxes only, key-test button has re-entrancy guard — Reason: full rebuild lost scroll/opened parts/typed text; double tap raced two 13 s routines; limited cable time
- Decision: code-review medium before release, fix 8 of 9, decline 'next step skips cards of closed group' — Reason: L10 safety rule (be highly confident release breaks nothing); plan text defines FALLBACK/EXTRAS/UNDO as outside main path
- Decision: L10 safety judged OK — Reason: no manifest/permission/resource/dependency change since v0.7.0; KEYTEST helper routine fixed-command, flag held by separate process max 12 s and cleared on death, peer-uid auth unchanged
- Decision: release gate is two parallel runs A (base,legacy,x1,e1,y2,b6,b4,n,n6,o,t,u,s,p,x) + B (v,g,ka,rt,im,gc,pf,rb,last); rs..rs4 research not gate; step timeout 2100 to 2700 s — Reason: everything needs ~42 min, run 124 cut inside rs4
- Decision: accept union of section runs on identical product code (app/ and phoneguide/ byte-identical between gated 3398b67 and release 4686fda) as gate — Reason: later commits only tools/docs; runs 124, 128, 126, 123, 120 cover every gate section
- Decision: ship v0.7.1 despite section legacy ALONE failing reset2 — Reason: proven pre-existing (run 129 on v0.7.0 tag identical 19 PASS 1 FAIL), code on path byte-identical, gate config (base first) fully green; guide needed for Tj's imminent cable session
- Decision: wait for CI with background until-date timer, never poll — Reason: job logs 404 until job ends; polling burns tokens; curl to GitHub API blocked by hook, do not work around
- Decision: RS1 recorded in TASKS.md as open with RS1a-c, not fixed now — Reason: pre-existing, hypothesis unproven, behaviour change in core reset path risky at release time; Tj's tablet answer sets priority

<!-- handoff:learnings:end -->
