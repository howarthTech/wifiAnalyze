# WiFi Analyze — Product Status

**What it is:** Android app that helps people understand their home WiFi — signal quality in
plain English, room-by-room dead-spot mapping, IoT readiness, plus an Advanced mode with
dBm charts, channel analysis, speed/latency tests, and an A–F network score.

**Current state:** `live` — **published on Google Play** 2026-08-12 (v1.2, versionCode 5, targetSdk 36).

**Live URL:** https://play.google.com/store/apps/details?id=com.wifianalyze

`Verify: $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat :app:assembleDebug :app:bundleRelease`

**Where things stand (2026-08-12):**
- **Published on Google Play** — listing live and verified (HTTP 200). First HTS
  first-party product to ship to a store. Portfolio pack flipped to `ready`; Chief of
  Staff pinged to move the portfolio index row to `live`.

**Earlier (2026-08-11):**
- Play Console flagged the annual target-API requirement (action by 2026-08-31): raised
  `targetSdk` 35 → 36 (Android 16) in both app and wear modules, bumped versionCode 3 → 4.
  compileSdk was already 36 so no toolchain change; the only Android 16 behavior change
  touching this app (edge-to-edge) was already handled. Rebuilt signed AAB.

**Earlier (2026-07-28):**
- v1.2 tested on a real device from the release-signed APK — works.
- Privacy policy verified live at
  `https://howarthtech.github.io/wifiAnalyze/store/privacy-policy.html`, and corrected:
  it had said "Approximate Location" (manifest declares FINE/precise) and claimed the
  speed test was the only external request (the latency test also pings 8.8.8.8). Play
  requires the policy, the Data safety form, and the manifest to agree.
- All store assets validated: icon 512×512, feature graphic 1024×500, 7 screenshots at
  exactly 2:1. Listing text within Play's character limits.
- `store/RELEASE-CHECKLIST.md` now has click-through-ready answers for the location
  declaration and Data safety form.

**Earlier (2026-07-11):**
- v1.2 prepared: Play Store blockers fixed (missing proguard file, feature graphic and
  screenshot dimensions), backup rules, themed icon, dark launch theme, widget preview,
  plus a round of UX/correctness fixes (scan throttling, alert spam, permission recovery,
  WiFi/Location-off guidance, stale widget data).
- Release AAB builds and is signed via local keystore (gitignored).
- Wear OS companion exists but is **not** in this release (wrong applicationId for Play
  co-distribution — see `store/RELEASE-CHECKLIST.md`).

**What's next:**
1. **Watch post-launch:** first-day installs, crash/ANR rate in Play Console (Android vitals),
   and any early reviews. Address crashes fast — a new listing is fragile.
2. Confirm the Chief of Staff has moved the portfolio index row to `live`, and the
   Portal/Marketing dev has integrated the pack onto howarth.tech.
3. Recapture a real **router-placement screenshot** (the store one was a duplicate of the
   room-list shot) and swap it into the Play listing + portfolio gallery.
4. Later: Wear OS distribution (needs applicationId change + release signing), string
   resource extraction for localization.

**Money:** free app with a "Buy Me a Pizza" link — no tracker needed until revenue exists.
