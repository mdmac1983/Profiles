# Speed, smoothness and restart audit (AQ1, 2026-11-06)

Tj: "Do a full test of the launcher app for maximum optimization and speed for my hardware. Make sure it can open each launcher as quickly as possible from the lock screen without flicker or showing quickstep items if i unlock to the custom launcher. Make sure everything can survive a tablet restart. The goal of the custom launcher is a smooth interface that looks completely stock without artifacts or blinking or flashing elements."

Hardware: MediaTek MT8168 (4x A53), 2 GB RAM, Android 10 Go. The emulator (x86, KVM) is faster than that, so its milliseconds are only ever compared with each other; what it can prove exactly is ORDER (which launcher is resumed when) and the absence of work.

## 1. Unlock flows (emulator section `uf`, read from Android's own event log `am_resume_activity`)

| Unlock | What was resumed, in order | Span | Verdict |
| :- | :- | :- | :- |
| plain PIN in a custom session | our launcher once | 0 ms | instant: the custom home was prepared behind the lock screen, the stock launcher is never resumed |
| plain PIN after a Quickstep session (Contacts was open) | our launcher 3 times | 3.3 s | no stock launcher, no Quickstep app: the clean-up of Quickstep's apps runs behind the black cover (no adb helper in the test) |
| Quickstep handshake (Delete, Delete, PIN) | our launcher, then the stock launcher | 260 ms | Quickstep in front at once; our launcher never comes back after the stock one |

Checks that keep it so (gate): UF1 (ends on ours, stock never resumed), UF2 (mode custom, neither stock nor the Quickstep app resumed after the Enter), UF3 (ends on stock, ours never resumed after it).
The 3.3 s of UF2 is the return reset (shade + Recents driven behind the cover). It is skipped when nothing needs clearing (B4: no Quickstep app opened, no notification icons) and it is invisible with the adb shell helper (`cleanAtLock` / `runInvisible`). On the tablet the helper is usually not running, so after a Quickstep session the unlock shows the cover for about the time the AD8 lines report ("Last unlock", "Last reset" in Launcher control, `UNLOCKSPEED` in the lock log).

## 2. Work removed or bounded in this session

* AN1: SystemUI content redraws (the shade, tiles) no longer start the power-menu scan (two whole-tree searches per system window, run by SystemUI's own UI thread, the thread that handles a tile tap); the pencil cover follows its node with ONE node refresh and searches the tree at most every 200 ms inside a burst. The AD8 line now counts both (`shade redraws without a scan`, `pencil cover: N tree searches, M one-node refreshes`).
* AN1: the battery-saver guard no longer reacts to every change of `low_power` (it logs, nothing else) and no longer re-applies its rule at every process start (only within 3 minutes of a boot).
* AO1b: the wallpaper picker runs in its own process (`:wallpaper_picker`) and does not count toward the crash breaker; its resources are loaded only when it opens.
* AP1: `RecentsFilter` costs a few string comparisons per accessibility event; it scans only after a window change of the stock launcher (or SystemUI "Recents"), never in the shade.
* AR1: closed groups of Launcher control build no rows at all (the page opened and rebuilt every row on every switch before).
* Already in place (AB2 / AD8): events of other apps dropped at the door, stock-launcher lookup cached, wallpaper bitmaps dropped under memory pressure, one preference handle per process.

## 3. Overlay windows (nothing may blink or flash)

| Window | When it exists | Why it cannot flash |
| :- | :- | :- |
| `TjWallpaperPatch` | custom launcher, lock screen: resting over the Screenshot slot only once calibrated; home screen: while the menu is open | resting = REAL pixels of the lock screen (pixel-identical: MAE 0); hidden (alpha 0, untouchable) when not calibrated, when wallpaper / rotation / display changed, while the PIN pad is up |
| power card (`power_card` only, legacy) | an open power menu in the old card mode | built hidden and kept warm, shown when the stock menu is found |
| `TjLockNumber` | lock screen, number switch on | removed the moment the PIN pad is pulled up |
| `TjPencilCover` | custom launcher, quick settings expanded | white like the panel, follows the pencil by node refresh |
| `TjResetCover` | return reset only | black, untouchable, 4.5 s failsafe |
| `TjPhotosCover` | the stock wallpaper picker only (Settings route) | opaque in the picker's own theme colour, removed when the picker leaves |
| `TjHotspot` | Solitaire corner, switch on | invisible by construction |

## 4. Restart survival (emulator section `rb`, plus the design)

After a tablet restart: the default Home is still the router (RB4: PIN unlocks and Home shows our launcher), the helper is re-bound by Android, mode / tiles / status-bar policy are read back unchanged (RB INFO lines), the display override survives, hidden apps / switches / calibration crop are app-private files (survive), keep-alive is off by default, the battery rule is applied once within three minutes of the boot (a later process restart never undoes a tap on the tile), the shell helper is gone until the adb command is run again (by design, documented in Launcher control).

## 4b. What the emulator cannot tell (send with a tablet report)

`accessibility helper load (AD8)` (events, scans, pencil searches in ms), `Last unlock`, `Speed of the last menu`, `battery saver guard`, the lock log (`ENTER lag`, `UNLOCKSPEED`, `RESET`), and one calibration of the lock-screen patch.
