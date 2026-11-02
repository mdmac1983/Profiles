# STOCK LAUNCHER REFERENCE — compare against these, ALWAYS, in every session (Tj, 2026-11-02: "Remember these photos for reference at all times, even in a new Claude session")

The custom launcher must look and function IDENTICALLY to the stock Quickstep v10 on Tj's Alps 711 tablet. These files are the ground truth. Before claiming a look is
"matched", view `stock-home.png` and `stock-drawer.png` next to the newest emulator/device screenshot (and measure: see below). Never trust memory of them.

| File | What it shows |
| :- | :- |
| `stock-home.png` | STOCK home screen (600x1024 screenshot): wallpaper, icons in columns 1/3/5, rows 1/3/5/7, folders Connect + Games, white labels, NO search bar, NO dock, NO caret |
| `stock-drawer.png` | STOCK app drawer: 5 columns, white rounded tiles ~55 px, "Search apps" bar (white, x 31-568, y 36-86), translucent (50% white over wallpaper), row pitch ~113 px, teal scroll handle at the right |
| `stock-home-longpress-popup.webp` | long-press on empty home: popup "Home settings / Wallpapers" |
| `stock-home-settings.png` | Home settings page: Notification dots, Add icon to Home screen, Allow Home screen rotation |
| `stock-drawer-first.webp`, `stock-settings-*.png`, `stock-notification-panel.png`, `stock-quick-settings-rotate-only.png` | other stock screens; quick settings has ONLY the Auto-rotate tile |
| `stock-powermenu-photo.jpg` | stock power menu: Power off / Restart / Screenshot (panel on the right) |
| `device-display-info.png`, `device-cpu-info.png`, `device-display-size-default.png` | 1024x600, 170 dpi, font scale 1.2, 7.2 in, ARMv7 32-bit, display size default |

## Measured facts (from these photos; use them, do not re-guess)
- Screen 600x1024 px at 170 dpi, font scale 1.2, 3-button navigation bar (48 px) + status bar (24 px).
- Grid 5 columns; row pitch 113-114 px => **8 rows, no hotseat/dock** (Tj confirmed 2026-11-02: "It has no dock, use 8 rows"). No all-apps caret on the home screen.
- Drawer: 5 columns (x centres 72, 186, 300, 414, 528), tile ~55 px, scrim = 50% white (least-squares fit over the two stock screenshots, residual 8), label text dark gray ~10% smaller than 14sp*1.2.
- Home labels: white with shadow. Wallpaper: green leaf (Tj's own).
- v0.2.1 on the real tablet (`research/uploads/2026-11-02c/`): drawer matched the stock positions (rows 114 px, columns identical, search bar identical); still had a dock and a "^" caret, 7 rows.
