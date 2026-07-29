# WiFi Analyze — Product Status

**What it is:** Android app that helps people understand their home WiFi — signal quality in
plain English, room-by-room dead-spot mapping, IoT readiness, plus an Advanced mode with
dBm charts, channel analysis, speed/latency tests, and an A–F network score.

**Current state:** `in build` → Play Store submission ready (v1.2, versionCode 3)

`Verify: $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat :app:assembleDebug :app:bundleRelease`

**Live URL:** not yet published — pending first Play Store submission

**Where things stand (2026-07-28):**
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

**What's next (all manual Play Console work — the code and assets are done):**
1. Create the app in Play Console, complete the location declaration + Data safety form
   using the prepared answers in `store/RELEASE-CHECKLIST.md`.
2. Upload `app/build/outputs/bundle/release/app-release.aab` to Internal testing first,
   then promote to Production.
3. Later: Wear OS distribution (needs applicationId change + release signing), string
   resource extraction for localization.

**Money:** free app with a "Buy Me a Pizza" link — no tracker needed until revenue exists.
