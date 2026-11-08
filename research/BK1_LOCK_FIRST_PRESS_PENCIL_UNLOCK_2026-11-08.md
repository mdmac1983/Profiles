# BK1 (Tj, 2026-11-08 18:16 UTC, about v0.8.10): first lock-screen power-menu press, the pencil, the unlock delay

Tj's words are in TASKS.md BK1 (verbatim). Everything below was measured on the CI EMULATOR (`emulator.yml`); NOTHING of it has run on the tablet yet. Shipped as v0.8.11 (candidate head c4f47b5).

## BK1a — "the first press of the side key on the lock screen flickers the patch, later presses are fine"
* **Measured** (run #389 id 42509822696, head 4c2e2e2, section `fp`): it REPRODUCES and it is a FRESH-PROCESS effect, not a per-lock one.
  First opening in a freshly started process: open_shape 0.194 (good < 0.08), open_max 0.24 (patch +24 % too bright for ~100 ms), only 3 patch frames drawn, one gap of 278 ms.
  Second opening in the same process: 0.122 (first frame 181 ms late). Warm process: first opening of a lock cycle 0.040-0.064, second 0.012-0.051 (a learning curve: GPU programs, class loading, JIT).
* **Cause** (read-only subagent map): NOTHING drew the OPEN look (a colour filter + the open-edge mask in a layer) before the first real opening, and the lock window is rebuilt at every lock.
* **Fix**: `warmOpenLookOnce` (patch view `onDraw`) draws the open look once per window, per look kind (measured / generic), at 1/255 alpha in the first RESTING frame (the real code path, invisible: at most 0.2 of one brightness level for one frame), counted only after a real draw. Log line `PATCH warm-up: the open look (...) was drawn once, invisibly`.
  The report also keeps the FIRST fade since the process started (`FIRST fade since this process started (BK1a)`), next to `last fade seen from the patch (BJ1)`.
* **Proof on the emulator** (runs #390, #392): the warm-up line comes before the first press; the fresh-process first fade has 8 frames and a worst gap of 107 ms instead of 3 and 278 ms; warm first opening = second opening.
  The FILM number of the first opening is dominated by the stock scrim's own ~100 ms steps on the software renderer, so judge the patch's own statistics (frames, worst gap), not the film mean (same lesson as HF4 / HF5).
* **Harness note**: `pt` always skips the first opening of a lock cycle; only `fp` films it (cold process + 4 warm cycles).

## BK1b — "can the pencil in the notification panel be hidden on the lock screen?"
* **Root cause**: the B6 cover (quick-settings pencil) is started by `schedulePencil`, which asked `Mode.isCustom` alone. On the lock screen after a Quickstep session with NO shell helper the STORED mode stays Quickstep, so no cover. With stored mode custom the cover already appeared 75-78 ms after the panel opened (runs #389, #394).
* **Fix**: `coverWorld()` (the lock screen counts as the custom world, like the power-menu cover). A power-menu burst's follow-up no longer searches for the pencil (it took 102 ms inside the film).
* **Proof**: section `b6` phase BK1b runs the lock screen in BOTH stored modes; the Quickstep-stored-mode lock screen shows the cover (diagnostic run #395; the check was fixed to "cover shown anywhere in the phase" because a warm window lingers and the log was cleared after the lock cycle).
* **Known limit, not changed**: the cover is plain WHITE (the dark theme's panel is not white).

## BK1c — "the custom launcher still loads from the lock screen on a delay"
* **Map of the unlock path** (subagent, 2026-11-08): the black cover only exists when the stored mode is Quickstep and no helper is reachable (plain PIN after a Quickstep session). Behind it: the shade step (cap 1000 ms), the Recents step (cap 2500 ms), `settleHome`. With the helper, or in a custom-only session, there is no cover and nothing to wait for.
* **New measurement**: ONE line per unlock, `UNLOCKSUM` in the lock log, report key `unlock_sum`, Control note "Last unlock, start to finish (BK1c)": custom home on screen N ms after the Enter tap [keyguard A ms; Launcher resumed +B, first frame +C, black cover gone +D (up E)]; mode then.
* **Baseline** (section `cs`, no helper, Quickstep -> plain PIN; run #393 id 42512174153): custom home on screen **1865 ms** after Enter [keyguard 399; cover gone +1466; reset 1429 = shade 328 + recents 1380].
* **Change**: the Recents loop and the notification poll looked at the screen THREE times per iteration (press, text, cards), each time one IPC per window (status and navigation bar included). Now the windows' roots are fetched ONCE per look and shared (`recentsRoots`, `pressInRoots`, `anyTextInRoots`, `cardsInRoots`; same searches, order and package rules).
* **After** (run #396 id 42513499462, head 894d06e, `cs`): **1538 ms**, reset **1130 ms**, 19 PASS. **CAVEAT (v0.8.11 gate, 2026-11-08 evening): that single pair is NOT an established gain.** The same unlock measured 866 ms (#398), 1788 ms (#401) and 2173 ms (#405) on the same code in other runs: the CI emulator's noise (0.9 - 2.2 s) is bigger than what removing two of the three window fetches per look can save. The change is sound (same searches, fewer binder round trips; read against the old helpers) but its size is unknown until a TABLET report shows `Last unlock, start to finish (BK1c)`.
* **What is left in the ~1.5 s** (no helper): the keyguard (~400 ms, system), the shade step (~300 ms), closing the Recents cards one by one (each close animates, ~1.1 s). Nothing more can be shaved without changing WHAT is done.
* **Options that need Tj** (from `FAST_UNLOCK_AND_HOME_POWERMENU_2026-11-08.md`): idea C, a home-looking curtain (the unlock READS as instant; touches during ~1 s are swallowed); idea D, a persistent helper (unproven on the tablet: HLP1, dies minutes after start, nothing survives a reboot).
* **ASK Tj once (short)**: which unlock is slow (after the custom launcher, or after Quickstep), what is on screen during the delay, about how many seconds; send the tablet report (`Last unlock ...`, process life, helper flight recorder).
