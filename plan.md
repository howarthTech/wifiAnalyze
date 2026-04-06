# WiFi Analyze - Development Plan

## Overview
WiFi Analyze is a personal-use Android app that helps users understand their WiFi signal strength, identify competing networks, and determine optimal placement for IoT/smart home devices.

## Architecture
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **DI:** Hilt
- **Local Storage:** Room Database + DataStore Preferences
- **Target SDK:** 35 (Android 15)
- **Min SDK:** Modern Android (12+)

## Modes

### Simple Mode (Default) ✅ Complete
Designed for non-technical users who want to understand their WiFi at a glance.

#### Features Implemented
- [x] **Signal Quality Indicator** — Excellent/Good/Fair/Poor with color-coded gauge and animated progress bar
- [x] **Multi-Band Breakdown** — Shows signal quality on each band (2.4 GHz / 5 GHz / 6 GHz) with IoT guidance
- [x] **IoT Readiness Verdict** — Checks 2.4 GHz signal specifically for smart device compatibility
- [x] **Nearby Networks Card** — Expandable list with competing count, congestion level, signal bars, band, channel, security
- [x] **Network Suggestions** — Actionable recommendations (channel optimization, security, band, placement)
- [x] **Room-by-Room Testing** — Save signal readings per room with quick-pick room name chips
- [x] **Saved Rooms List** — Ranked by signal strength, swipe-to-delete, timestamps, IoT ready indicator
- [x] **Contextual Tips** — Smart tips based on signal, congestion, band, and link speed
- [x] **Pull-to-Refresh** — Manual scan trigger on dashboard
- [x] **Permission Onboarding** — Clean first-launch permission flow with explanation
- [x] **Friendly Loading State** — "Checking your WiFi..." on launch, hides cards until data arrives
- [x] **Custom App Icon** — WiFi arcs + pulse line + magnifying glass on indigo background

#### Bug Fixes Applied
- [x] Missing `ACCESS_NETWORK_STATE` permission (crash on second launch)
- [x] SSID redacted on Android 12+ (`WifiManager.connectionInfo` fallback)
- [x] Scan broadcast receiver not receiving on Android 13+ (`RECEIVER_EXPORTED`)
- [x] Competing networks double-counting (now uses `distinctBy` SSID)
- [x] Room reading RSSI back-calculation bug (now saves actual RSSI)

---

### Advanced Mode 🔨 In Progress
For power users who want detailed technical data. Toggle via Settings.

#### Features Implemented
- [x] **Mode Toggle** — Switch in Settings, persists via DataStore, navigates between dashboards
- [x] **Real-time dBm readings** — Large monospace dBm display with frequency, channel, band, link speed, BSSID
- [x] **Signal over time graph** — Compose Canvas line chart with color-coded quality bands, 5-sec sampling
- [x] **Connected network details** — IP, gateway, DNS servers, subnet prefix, TX/RX link speeds
- [x] **Pull-to-refresh** — Manual scan trigger
- [x] **Friendly loading state** — Reuses Simple Mode pattern

#### Remaining Features
- [ ] **Channel usage chart** — Bar chart of channel occupancy per band (2.4 GHz / 5 GHz toggle)
- [ ] **Channel overlap visualization** — Spectrum view showing bandwidth spread (20/40/80 MHz) per AP
- [ ] **AP vendor identification** — Resolve BSSID OUI to manufacturer name via embedded lookup table
- [ ] **Network latency test** — Ping gateway + 8.8.8.8, show min/avg/max RTT
- [ ] **Export/share results** — Save scan snapshot as CSV or JSON via share sheet

---

## Future Enhancements 🔲
- [x] **Custom app icon** — ✅ Done
- [ ] **Dark mode** — Full dark theme support (currently uses system dynamic colors)
- [ ] **Export room report** — Share room comparison as image or text
- [ ] **Router placement optimizer** — Analyze saved rooms to suggest best router location
- [ ] **WiFi speed test** — In-app download/upload speed test
- [ ] **Notification alerts** — Alert when signal drops below threshold
- [ ] **Widget** — Home screen widget showing current signal quality
- [ ] **History tracking** — Track signal quality over days/weeks

---

## Project Structure
```
app/src/main/java/com/wifianalyze/
├── MainActivity.kt
├── WifiAnalyzeApp.kt
├── data/
│   ├── local/
│   │   ├── RoomReadingDao.kt
│   │   ├── WifiAnalyzeDatabase.kt
│   │   └── entity/
│   │       └── RoomReadingEntity.kt
│   ├── preferences/
│   │   └── AppPreferences.kt
│   └── wifi/
│       ├── ConnectionInfoProvider.kt
│       ├── WifiScanner.kt
│       └── WifiScannerImpl.kt
├── di/
│   └── AppModule.kt
├── domain/
│   ├── ChannelHelper.kt
│   ├── CongestionAnalyzer.kt
│   ├── IoTReadinessChecker.kt
│   ├── NetworkOptimizer.kt
│   ├── SignalMapper.kt
│   ├── TipEngine.kt
│   └── model/
│       ├── BandSignalInfo.kt
│       ├── CongestionLevel.kt
│       ├── ConnectionInfo.kt
│       ├── IoTReadiness.kt
│       ├── Recommendation.kt
│       ├── RoomReading.kt
│       ├── SignalQuality.kt
│       ├── WifiBand.kt
│       └── WifiNetwork.kt
└── ui/
    ├── navigation/
    │   └── AppNavigation.kt
    ├── permission/
    │   └── PermissionScreen.kt
    ├── settings/
    │   └── SettingsScreen.kt
    ├── simple/
    │   ├── RoomListScreen.kt
    │   ├── RoomTestScreen.kt
    │   ├── SimpleDashboardScreen.kt
    │   ├── SimpleViewModel.kt
    │   └── components/
    │       ├── CompetingNetworksCard.kt
    │       ├── IoTReadinessCard.kt
    │       ├── NearbyNetworksCard.kt
    │       ├── RecommendationsCard.kt
    │       ├── RoomReadingItem.kt
    │       ├── SignalQualityCard.kt
    │       ├── TipBanner.kt
    │       └── YourNetworkBandsCard.kt
    ├── advanced/
    │   ├── AdvancedDashboardScreen.kt
    │   ├── AdvancedViewModel.kt
    │   └── components/
    │       ├── RawSignalCard.kt
    │       ├── ConnectionDetailsCard.kt
    │       └── SignalHistoryChart.kt
    └── theme/
        ├── Color.kt
        └── Theme.kt
```

---

## Testing Notes
- **Emulator limitations:** WiFi scanning returns empty/mock results on emulators. Must test on a real device.
- **Wireless ADB:** Paired via `adb pair` + `adb connect` for cable-free deployment.
- **Android scan throttling:** Android limits WiFi scans to ~4 per 2 minutes in the foreground. Simple Mode rescans every 10 seconds, Advanced Mode every 5 seconds. Connection RSSI callback is not throttled and keeps signal history chart fed.
- **Location permission:** Required by Android to scan WiFi networks. SSID is redacted without it.
- **Mode persistence:** DataStore preferences persist the Simple/Advanced mode choice across app restarts.
