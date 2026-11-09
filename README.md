# Desi tvOS Launcher

Apple TV (tvOS) style launcher for **Android 4.4 KitKat (API 19)** — built with native Android views for buttery-smooth D-pad focus navigation. No external dependencies.

Made in Pakistan 🇵🇰 

## Features

- **tvOS look**: dark minimal UI, focus-based D-pad navigation, smooth zoom-on-focus animations with a white focus ring.
- **Home rows**: Favorites → Recent → All Apps, horizontal rows like Apple TV.
- **Top header**: live clock + app search bar.
- **Featured banner**: customizable area at the top for featured content.
- **Recents & Favorites**: long-press CENTER/ENTER on any app card to favorite it; recents update automatically.
- **Search**: remote SEARCH key focuses the search bar; filters apps live.
- **Standby**: BACK = standby clock overlay (big clock + calendar date), any key wakes it. Auto-standby after 60 s idle (configurable).
- **Settings (MENU key)**: dark/light theme, 5 gradient wallpapers, icon size (S/M/L), A-Z sort toggle, auto-standby toggle, system settings shortcut.

## Remote / Keyboard Controls

| Key | Action |
|---|---|
| D-pad (arrows) | Move focus between cards & rows |
| CENTER / ENTER | Open focused app |
| Long-press CENTER | Toggle favorite |
| MENU | Launcher settings |
| SEARCH | Focus search bar |
| BACK | Standby (wake with any key); clears search first |

## Build

```bash
git clone https://github.com/fatikzeeshanh-eng/Desi-tv-launcher
cd Desi-tv-launcher
./gradlew assembleDebug   # or assembleRelease
adb install app/build/outputs/apk/debug/app-debug.apk
```

Then press HOME and choose **Desi tvOS Launcher → Always** to make it your default launcher.

## Project Structure

```
app/src/main/
├── AndroidManifest.xml               # HOME category = default launcher
├── java/com/indiatv/launcher/
│   ├── MainActivity.java             # Home screen, rows, keys, standby
│   ├── AppInfo.java                  # App model
│   ├── AppsRepository.java           # Installed apps, recents, favorites
│   ├── LauncherSettings.java         # Persisted settings + wallpapers
│   ├── FocusEffects.java             # tvOS focus zoom animations
│   └── SettingsDialog.java           # Settings panel
└── res/layout/
    ├── activity_main.xml             # Header, banner, rows container
    ├── row_tvos_apps.xml             # Horizontal scrollable row
    ├── item_tvos_app.xml             # Focusable app card
    ├── overlay_standby.xml           # Standby clock overlay
    └── dialog_settings.xml           # Settings panel
```

## Architecture

- **Views + Handler only** — no external libraries, targets Android 4.4 (API 19) WebView-free native UI.
- **State**: `AppsRepository` (SharedPreferences) for recents/favorites order, `LauncherSettings` for user prefs.
- **Focus engine**: Android's native `View.requestFocus()` + `OnFocusChangeListener` handles D-pad movement; `FocusEffects` adds the tvOS scale animation.
- **Event handling**: `dispatchKeyEvent` routes MENU/BACK/SEARCH to launcher actions; everything else flows to the native focus engine.

## License

MIT — do whatever you want, credit appreciated.
