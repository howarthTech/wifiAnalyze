# Play Store Release Checklist — WiFi Analyze v1.2 (versionCode 3)

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
- [x] Feature graphic resized to exactly 1024×500
- [x] Screenshots cropped to 1080×2160 (Play's max 2:1 aspect ratio)
- [x] Backup rules (`dataExtractionRules` / `fullBackupContent`) — WiFi history excluded from cloud backup, consistent with the privacy policy
- [x] App theme with dark-mode launch window (no more white flash)
- [x] Themed (monochrome) launcher icon for Android 13+
- [x] Widget picker preview + description
- [x] Predictive back opt-in
- [x] versionCode 3 / versionName 1.2

## Play Console — one-time setup (manual)
1. **Privacy policy URL** — the app and listing point to
   `https://howarthtech.github.io/wifiAnalyze/store/privacy-policy.html`.
   **Verify this URL loads in a browser before submitting.** If GitHub Pages isn't enabled
   for the repo yet: repo Settings → Pages → deploy from `main`. Enter the URL in
   *App content → Privacy policy*.
2. **Location permissions declaration** (*App content → Sensitive app permissions*):
   the app declares `ACCESS_FINE_LOCATION`. Declare it as **required for core functionality**:
   "Android requires location permission for apps to scan WiFi networks. The app analyzes
   WiFi signal strength and nearby networks. Location data itself is never collected,
   stored, or shared." The in-app permission screen already shows a prominent disclosure.
3. **Data safety form** (*App content → Data safety*):
   - Data collected: **none** (all readings stay on the device; nothing is sent to the developer).
   - Data shared: **none**.
   - Note the app makes network requests to third parties when the user runs tests:
     `speed.cloudflare.com` (speed test) and `8.8.8.8` (latency ping). No personal data is
     transmitted — only generated test bytes. This matches the privacy policy.
   - Security practices: data not encrypted in transit N/A (no user data transmitted);
     users can delete data via "Clear All Saved Rooms" in Settings.
4. **App category**: Tools. Content rating questionnaire: everyone, no sensitive content.
5. **Store listing**: use `store/listing.md` (title/short/full description all within limits),
   `store/icon-512.png`, `store/feature-graphic.png`, and the 7 screenshots in
   `store/screenshots/`.

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
