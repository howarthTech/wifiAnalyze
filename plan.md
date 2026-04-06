# WiFi Analyze - Development Plan

## Overview
WiFi Analyze is a personal-use Android app that helps users understand their WiFi signal strength, identify competing networks, and determine optimal placement for IoT/smart home devices.

## Architecture
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **DI:** Hilt
- **Local Storage:** Room Database
- **Target SDK:** 35 (Android 15)
- **Min SDK:** Modern Android (12+)

## Modes

### Simple Mode (Default) ✅ Complete
Designed for non-technical users who want to understand their WiFi at a glance.

#### Features Implemented
- [x] **Signal Quality Indicator** — Excellent/Good/Fair/Poor with color-coded gauge and animated progress bar
- [x] **Multi-Band Breakdown** — Shows signal quality on each band (2.4 GHz / 5 GHz / 6 GHz) with IoT guidance
- [x] **IoT Readiness Verdict** — Checks 2.4 GHz signal specifically for smart device compatibility
- [x] **Competing Networks Count** — Unique SSIDs with congestion level (Low/Medium/High)
- [x] **Nearby Networks List** — Expandable list of all detected networks with signal bars, band, channel, security
- [x] **Network Suggestions** — Actionable recommendations (channel optimization, security, band, placement)
- [x] **Room-by-Room Testing** — Save signal readings per room with quick-pick room name chips
- [x] **Saved Rooms List** — Ranked by signal strength, swipe-to-delete, timestamps, IoT ready indicator
- [x] **Contextual Tips** — Smart tips based on signal, congestion, band, and link speed
- [x] **Pull-to-Refresh** — Manual scan trigger on dashboard
- [x] **Permission Onboarding** — Clean first-launch permission flow with explanation

#### Bug Fixes Applied
- [x] Missing `ACCESS_NETWORK_STATE` permission (crash on second launch)
- [x] SSID redacted on Android 12+ (`WifiManager.connectionInfo` fallback)
- [x] Scan broadcast receiver not receiving on Android 13+ (`RECEIVER_EXPORTED`)
- [x] Competing networks double-counting (now uses `distinctBy` SSID)
- [x] Room reading RSSI back-calculation bug (now saves actual RSSI)

---

### Advanced Mode 🔲 Planned
For power users who want detailed technical data.

#### Planned Features
- [ ] **Real-time dBm readings** — Raw signal strength values
- [ ] **Signal over time graph** — Line chart showing RSSI fluctuations
- [ ] **Channel usage chart** — Visual graph of channel occupancy per band
- [ ] **Channel overlap visualization** — Shows bandwidth spread (20/40/80 MHz) of each network
- [ ] **AP vendor identification** — Resolve BSSID OUI to manufacturer name
- [ ] **Connected network details** — IP, gateway, DNS, subnet, link speed TX/RX
- [ ] **Network latency test** — Ping to gateway and external host
- [ ] **Export/share results** — Save scan data as CSV or JSON

---

## Future Enhancements 🔲
- [ ] **Custom app icon** — Branded WiFi analyzer icon
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
    └── theme/
        ├── Color.kt
        └── Theme.kt
```

---

## Testing Notes
- **Emulator limitations:** WiFi scanning returns empty/mock results on emulators. Must test on a real device.
- **Wireless ADB:** Paired via `adb pair` + `adb connect` for cable-free deployment.
- **Android scan throttling:** Android limits WiFi scans to ~4 per 2 minutes in the foreground. App rescans every 10 seconds but may get throttled results.
- **Location permission:** Required by Android to scan WiFi networks. SSID is redacted without it.
