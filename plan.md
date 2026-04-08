# WiFi Analyze - Development Plan

## Overview
WiFi Analyze is a personal-use Android app that helps users understand their WiFi signal strength, identify competing networks, and determine optimal placement for IoT/smart home devices.

## Architecture
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 (phone) / Wear Compose (watch)
- **DI:** Hilt
- **Local Storage:** Room Database (v4) + DataStore Preferences
- **Background:** WorkManager + Glance widget
- **Wearable:** Wearable Data Layer API (phone → watch via `/wifi-signal`)
- **Target SDK:** 35 (Android 15)
- **Min SDK:** 33 (phone) / 30 (wear)
- **Modules:** `:app` (phone), `:wear` (Wear OS)

---

## Modes

### Simple Mode ✅ Complete
Designed for non-technical users who want to understand their WiFi at a glance.

- [x] Signal Quality Indicator — Excellent/Good/Fair/Poor with color-coded gauge and animated progress bar
- [x] Multi-Band Breakdown — Shows signal quality on each band (2.4 GHz / 5 GHz / 6 GHz)
- [x] IoT Readiness Verdict — Checks 2.4 GHz signal specifically for smart device compatibility
- [x] Nearby Networks Card — Expandable list with competing count, congestion level, signal bars, band, channel, security
- [x] Network Suggestions — Actionable recommendations (channel optimization, security, band, placement)
- [x] Room-by-Room Testing — Save signal readings per room with quick-pick room name chips
- [x] Saved Rooms List — Ranked by signal strength, swipe-to-delete, timestamps, IoT ready indicator
- [x] Contextual Tips — Smart tips based on signal, congestion, band, and link speed
- [x] Pull-to-Refresh — Manual scan trigger on dashboard
- [x] Friendly Loading State — "Checking your WiFi..." on launch
- [x] Router Placement Optimizer — Best/worst room analysis and placement tip card in Room List
- [x] Export Room Report — Share button in Room List, formatted text report

### Advanced Mode ✅ Complete
For power users who want detailed technical data.

- [x] Mode Toggle — Switch in Settings, persists via DataStore
- [x] Real-time dBm readings — Large monospace dBm display with frequency, channel, band, link speed, BSSID
- [x] Signal over time graph — Canvas line chart with color-coded quality bands, 5-sec sampling
- [x] Connected network details — IP, gateway, DNS, subnet, TX/RX link speeds (with copy-to-clipboard)
- [x] Channel usage chart — Bar chart of channel occupancy per band (2.4 GHz / 5 GHz toggle)
- [x] Channel overlap visualization — Spectrum view showing bandwidth spread per AP
- [x] AP vendor identification — BSSID OUI → manufacturer name lookup
- [x] Network latency test — Ping gateway + 8.8.8.8, show min/avg/max RTT
- [x] Export/share results — Save scan snapshot as JSON via share sheet
- [x] WiFi speed test — Download + upload via Cloudflare, live Mbps display
- [x] Network Score — A–F grade from signal (40pts) + stability (20pts) + speed (20pts) + latency (20pts)
- [x] Signal history chart — 24h/7d chart with colored quality bands and stability score
- [x] Speed test history chart — Dual-line chart (download/upload) with recent results list
- [x] Latency history chart — Dual-line chart (gateway/internet) with recent results list

---

## Cross-Cutting Features ✅ Complete

- [x] Custom app icon — WiFi arcs + pulse line on blue gradient background (adaptive icon)
- [x] Dark mode — Force dark toggle in Settings
- [x] Notification alerts — Threshold slider, signal-weak alert + all-clear recovery notification
- [x] Channel congestion notification — Background worker, once-per-day cooldown
- [x] Home screen widget — Glance widget showing quality, dBm, SSID; updated every 15 min
- [x] Onboarding flow — 4-page HorizontalPager (Welcome, Room Coverage, Advanced, Privacy), permission request on last page, full-width Skip button
- [x] Play Store listing — Title, short/full description, screenshot brief, feature graphic spec, release notes

---

## Wear OS Companion ✅ Complete

`:wear` module — glanceable signal strength on a round watch face.

- [x] `WearDataSender` (phone) — pushes ssid/rssi/quality/band via Data Layer to `/wifi-signal` on every scan cycle
- [x] `WearDataListener` (watch) — `WearableListenerService` receives data items, updates `WearSignalState` singleton
- [x] `WearSignalScreen` — round watch UI with:
  - Colored arc `CircularProgressIndicator` (green=Excellent → red=No Signal)
  - Quality label + dBm reading + SSID + band
  - "No WiFi" disconnected state
- [x] Live updates — watch refreshes within ~10 seconds of phone scan cycle

### Installing the watch APK
```bash
adb connect <watch-ip>:5555
adb -s <watch-ip>:5555 install wear/build/outputs/apk/debug/wear-debug.apk
```

---

## Project Structure
```
wifiAnalyze/
├── app/src/main/java/com/wifianalyze/
│   ├── MainActivity.kt
│   ├── WifiAnalyzeApp.kt
│   ├── data/
│   │   ├── local/
│   │   │   ├── WifiAnalyzeDatabase.kt          (Room v4)
│   │   │   ├── RoomReadingDao.kt
│   │   │   ├── SpeedTestHistoryDao.kt
│   │   │   ├── LatencyHistoryDao.kt
│   │   │   └── entity/
│   │   │       ├── RoomReadingEntity.kt
│   │   │       ├── SpeedTestHistoryEntity.kt
│   │   │       └── LatencyHistoryEntity.kt
│   │   ├── notification/
│   │   │   └── NotificationHelper.kt
│   │   ├── preferences/
│   │   │   └── AppPreferences.kt
│   │   ├── wear/
│   │   │   └── WearDataSender.kt
│   │   ├── widget/
│   │   │   └── WidgetUpdater.kt
│   │   ├── worker/
│   │   │   └── SignalMonitorWorker.kt
│   │   └── wifi/
│   │       ├── ConnectionInfoProvider.kt
│   │       └── WifiScanner.kt
│   ├── di/
│   │   └── AppModule.kt
│   ├── domain/  (SignalMapper, CongestionAnalyzer, etc.)
│   └── ui/
│       ├── navigation/AppNavigation.kt
│       ├── onboarding/OnboardingScreen.kt
│       ├── permission/PermissionScreen.kt
│       ├── settings/SettingsScreen.kt
│       ├── simple/  (SimpleDashboardScreen, RoomListScreen, SimpleViewModel, …)
│       └── advanced/ (AdvancedDashboardScreen, HistoryScreen, AdvancedViewModel, …)
│
└── wear/src/main/java/com/wifianalyze/wear/
    ├── MainActivity.kt
    ├── WearSignalState.kt
    ├── WearDataListener.kt
    └── presentation/
        └── WearSignalScreen.kt
```

---

## Testing Notes
- **Emulator limitations:** WiFi scanning returns empty/mock results on emulators. Must test on a real device.
- **Wireless ADB:** Paired via `adb pair` + `adb connect` for cable-free deployment.
- **Android scan throttling:** Android limits WiFi scans to ~4 per 2 minutes in the foreground.
- **Location permission:** Required by Android to scan WiFi networks. SSID is redacted without it.
- **Wear OS testing:** Emulator won't receive real Data Layer events unless paired with phone. Use Android Studio App Inspection → Data Layer tab to manually send test items, or test on a physical watch paired via the Wear OS companion app.
- **Windows build issue:** If `mergeDebugResources` fails with file-lock errors, run: `./gradlew --stop` then `powershell Remove-Item -Recurse -Force app\build`.
