# Play Store Release Checklist — WiFi Analyze v1.2 (versionCode 4, targetSdk 36)

## Build the upload artifact
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:bundleRelease
# Output: app\build\outputs\bundle\release\app-release.aab
```
Signing uses `keystore.properties` + `app/release.jks` (both gitignored — **back the keystore
up somewhere safe**; losing it means you can never update the app).

## Already done in the repo (v1.2)
- [x] `app/proguard-rules.pro` created (release build was failing without it)
- [x] Feature graphic resized to exactly 1024x500
- [x] Screenshots cropped to 1080x2160 (Play's max 2:1 aspect ratio)
- [x] Backup rules (`dataExtractionRules` / `fullBackupContent`) — WiFi history excluded from cloud backup, consistent with the privacy policy
- [x] App theme with dark-mode launch window (no more white flash)
- [x] Themed (monochrome) launcher icon for Android 13+
- [x] Widget picker preview + description
- [x] Predictive back opt-in
- [x] targetSdk 36 (Android 16) — satisfies Play's annual target-API requirement
- [x] versionCode 4 / versionName 1.2
- [x] Privacy policy verified live and corrected for accuracy
- [x] Release-signed APK tested on a real device (v3/target-35; re-smoke the v4/target-36 build)

## Play Console — one-time setup (manual)

### 1. Privacy policy URL — verified live
`https://howarthtech.github.io/wifiAnalyze/store/privacy-policy.html`

Confirmed serving (GitHub Pages, `main` branch root). Paste into
*App content -> Privacy policy*. It was corrected in July 2026 to say **Precise** location
(matching the `ACCESS_FINE_LOCATION` the manifest declares) and to disclose the latency
test's ping to 8.8.8.8. Keep your Data safety answers consistent with it.

### 2. Location permissions declaration
*App content -> Sensitive app permissions -> Location*. The app declares
`ACCESS_FINE_LOCATION`, so this form is mandatory.

- Is location access required for core functionality? **Yes.**
- Suggested wording:
  > Android requires the location permission for any app to scan for nearby WiFi networks
  > or read network names (SSIDs). WiFi Analyze uses it solely to display WiFi signal
  > strength, nearby networks, and channel congestion. The app never requests, reads,
  > stores, or transmits GPS coordinates, and location is not used for any other purpose.
- Is it used in the background? **No** — foreground only. The background worker reads only
  the already-connected network's signal.
- The in-app permission screen already provides the prominent in-app disclosure Play requires.

### 3. Data safety form
*App content -> Data safety*.

- **Does your app collect or share any of the required user data types? -> No.**
  Everything (room readings, speed/latency history, preferences) is stored locally in Room
  and DataStore, and nothing is transmitted to the developer.
- **Is all data encrypted in transit?** The only outbound traffic is the user-initiated
  speed test (HTTPS to Cloudflare) and the latency ping to 8.8.8.8. Neither carries user
  data — only filler bytes and timing requests.
- **Can users request data deletion?** Yes — Settings -> "Clear All Saved Rooms", or
  uninstalling removes everything. There is no account and no server-side data.
- Note: declaring a *permission* (location) is separate from *collecting* data. Requesting
  location for WiFi scanning without recording it is correctly declared as "not collected."

### 4. If Play asks about foreground services
The merged manifest contains `FOREGROUND_SERVICE` and
`androidx.work.impl.foreground.SystemForegroundService`. **These come from the WorkManager
library, not from our code** — `SignalMonitorWorker` is a plain periodic `CoroutineWorker`
that never calls `setForeground()`, and the work is never scheduled as expedited (see
`WifiAnalyzeApp.scheduleBackgroundMonitoring`). No foreground service is ever started, so
answer that the app does not use one. (Worth verifying, because a foreground service with no
`foregroundServiceType` would crash on targetSdk 34+ if it ever did start.)

### 5. App content questionnaires
- **Category**: Tools. **Content rating**: Everyone — no user-generated content, no ads,
  no purchases, no sensitive material.
- **Ads**: contains no ads. **In-app purchases**: none. The "Buy Me a Pizza" button opens an
  external browser link and is not a Play billing product.
- **Target audience**: general / 18+ — not designed for children.
- **Government app**: no. **Financial features**: none. **Data deletion URL**: not required
  (no account system).

### 6. Store listing
Use `store/listing.md` for title, short and full description (all verified within Play's
limits), plus `store/icon-512.png` (512x512), `store/feature-graphic.png` (1024x500), and
the 7 screenshots in `store/screenshots/` (1080x2160, exactly 2:1). Release notes for v1.2
are drafted at the bottom of `listing.md`.

### 7. Release track
Start with **Internal testing** to confirm the Play-signed build behaves like the local one,
then promote to Production. Note that Play re-signs your upload with its own app signing key;
the keystore you hold is the *upload* key.

## Known limitation — Wear OS app is NOT in this release
The `:wear` module uses applicationId `com.wifianalyze.wear`, but Play requires a Wear app
to share the **same applicationId** as the phone app to ship on the same listing. It also
has no release signing config. Ship phone-only now; to distribute the watch app later:
change its applicationId to `com.wifianalyze`, give it its own versionCode range (e.g.
1000+), add the release `signingConfig`, and upload its AAB to the same listing's Wear track.

## Every future release
- [ ] Bump `versionCode` (+1) and `versionName` in `app/build.gradle.kts`
- [ ] `.\gradlew.bat :app:bundleRelease` and upload the AAB
- [ ] Update release notes in Play Console (draft them in `store/listing.md`)
