# Launcher — standing rules, decisions and build traps

## What this is
An Android **home-screen launcher** for **Android 10 (API 29) Go edition** (low-RAM devices, typically 1-2 GB,
often 32-bit ARM). Package `com.tj.launcher`. Sideloaded; never published to Play. Tj's request (2026-11-02): "I
will be building an Android 10 go launcher apk." The feature list is not written yet — it arrives in `TASKS.md`.

## Decided (foundation session, 2026-11-02)
| Item | Decision | Why / how to revisit |
| :- | :- | :- |
| Platform | `minSdk 29`, `targetSdk 29`, `compileSdk 36` | Target = the real device's runtime, so no newer-Android behaviour is assumed. Lint's Play-only `ExpiredTargetSdkVersion` check is disabled for that reason. Revisit only if the app must run on a newer phone. |
| Language / UI | Kotlin; the foundation Home screen is a plain `GridView`, **no UI library** | Go devices are RAM-tight, so UI weight matters; Compose is possible but is OPEN (below). |
| Dependencies | Kotlin stdlib only | Add one only with a reason written here. |
| ABI | No native code → one APK covers armv7 and arm64 | Re-verify on the real device. |
| Release build | R8 minify + resource shrink on | Small APK for Go. Add keep rules in `app/proguard-rules.pro` if reflection ever appears. |
| Toolchain (pinned, copied from Mirage where it is proven) | AGP 8.13.2, Kotlin 2.3.10, Gradle 8.14.3, JDK 17 target (CI runs 21) | Do not "upgrade" because a dependency demands a newer compileSdk/AGP; lint's version checks are off on purpose. |
| Building | GitHub Actions (`android.yml`); this container only runs tests/lint/debug | Tj's rule on all repos. |
| Signing | Not created yet. `app/sideload.jks` is gitignored; builds sign with it only when present | See Keystore. Without the CI secret a build is a **preview** (debug-signed artifact). |
| Branches | Work on the assigned branch; `tools/MIRROR_MAIN` = `off` | Turn on only on Tj's word. |

## The device and the plan (2026-11-02 second message — full spec in TASKS.md L1-L11, design in `research/DESIGN_2026-11-02.md`)
Target = Alps 711 `tb8168p1_bsp` tablet, MT8168B, 32-bit, Android 10 Go, locked bootloader, stock launcher Quickstep v10, 600x1024, 1.3 GB free.
Decided: port AOSP Launcher3 Go (android-10.0.0_r47, `tools/fetch_launcher3.sh`) minus the Quickstep recents layer (not installable without system signature);
UI toolkit = Launcher3's own Views (no Compose); switching back to Quickstep = pass-through mode; every secure-setting feature is opt-in, snapshot-first,
default OFF; staged releases v0.2 safe core -> v0.3 in-app features -> v0.4+ permission features. **The safety rules in DESIGN section 6 override every feature.**

## OPEN — ask Tj (do not assume)
1. (SUPERSEDED 2026-11-04, v0.4.0) The Quickstep gesture is gone: Quickstep opens only by an unlock handshake (UnlockMethods) or the master failsafes (FailsafeKeys); the ways back are ReturnMethods. See research/PROFILES_2026-11-04.md.
2. Whether he can use a PC with USB debugging (adb) — needed for L1/L4/L5/L7 permissions and for the first-install safety net.
3. Power-menu block (L4) is experimental and may not be possible reliably: is a flickering best-effort acceptable?
4. Keystore creation (below).

## The signing keystore (not created yet)
Generating it is safe until something signed with it is installed; after that it is **irreplaceable**: a different key
cannot update the install in place, and Android then forces an uninstall (erasing app data). A preview (debug-signed)
build installed earlier has a different certificate, so the first signed release over it needs an uninstall once.
1. `bash tools/init-keystore.sh` — creates `app/sideload.jks` (alias/password `launcher`, 27-year validity), pins the
   SHA-256 into `tools/keystore.fingerprint` (commit that file; `checkkeystore.sh`/`verify-apk.sh` enforce it forever).
2. **Back the file up out of band now** (password manager / personal cloud): containers are destroyed at session end.
3. Tj adds the GitHub secret `SIGNING_KEYSTORE_BASE64` (`base64 -w0 app/sideload.jks`, run by him, never pasted into
   chat). Until then `android.yml` makes previews only.
Never regenerate it once anything is installed. Never change `applicationId`. Always bump `versionCode` before shipping.

## Build environment traps (each has cost real time in the sibling repos)
1. **Never unset or blank `JAVA_TOOL_OPTIONS`.** The container's HTTPS proxy and truststore live in it; clearing it
   makes Gradle fail with "Plugin ... was not found" — which reads like a version error but is a network error.
2. A cold Gradle build can exceed a 2-minute foreground timeout: background it
   (`setsid nohup bash tools/gradle.sh ... > build.log 2>&1 < /dev/null & disown`) and poll `tail build.log`.
3. **Never run two Gradle builds at once, never kill one mid-flight** — it corrupts the Kotlin incremental caches and
   the next compile reports nonsense ("Unresolved reference" for a symbol in the same file). Fix:
   `pkill -9 -f KotlinCompileDaemon; rm -rf app/build/kotlin ~/.gradle/kotlin ~/.kotlin`.
4. `assembleRelease` may need `build-tools;35.0.0` as well as 36.0.0 on a cold container (`setup-android-sdk.sh`
   installs both).
5. Maven Central can answer HTTP 429 on a cold container ("Could not resolve ..."): rate limiting, not a bad version.
   Gradle keeps what it downloaded, so retry: `for i in 1 2 3 4 5 6; do bash tools/gradle.sh <task> && break; sleep 30; done`.
6. `tools/gradle.sh` refuses a **release** task without the correct keystore (by design); tests/lint/debug need none.
7. Hook output is capped at 10,000 characters; longer is cut to a 2,000-char preview and the briefing is silently
   lost. `tools/test_handoff.sh` checks `resume.sh` stays under it — keep it short.

## Launcher-specific gotchas (full list: `.claude/skills/android-go-launcher`)
- The Home activity needs `HOME` + `DEFAULT` categories, `singleTask`, and must swallow Back.
- Android 11+ hides other apps unless declared (`<queries>` is already in the manifest); harmless on 10.
- Keep RAM low: no big bitmaps in memory, cache icons at display size, release resources in `onTrimMemory`.
