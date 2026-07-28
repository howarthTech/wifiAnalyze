# WiFi Analyze — Product Status

**What it is:** Android app that helps people understand their home WiFi — signal quality in
plain English, room-by-room dead-spot mapping, IoT readiness, plus an Advanced mode with
dBm charts, channel analysis, speed/latency tests, and an A–F network score.

**Current state:** `in build` → Play Store submission ready (v1.2, versionCode 3)

`Verify: $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat :app:assembleDebug :app:bundleRelease`

**Live URL:** not yet published — pending first Play Store submission

**Where things stand (2026-07-11):**
- v1.2 prepared: Play Store blockers fixed (missing proguard file, feature graphic and
  screenshot dimensions), backup rules, themed icon, dark launch theme, widget preview,
  plus a round of UX/correctness fixes (scan throttling, alert spam, permission recovery,
  WiFi/Location-off guidance, stale widget data).
- Release AAB builds and is signed via local keystore (gitignored).
- Wear OS companion exists but is **not** in this release (wrong applicationId for Play
  co-distribution — see `store/RELEASE-CHECKLIST.md`).

**What's next:**
1. Verify the privacy policy URL is live (GitHub Pages), then complete Play Console
   setup per `store/RELEASE-CHECKLIST.md` (location declaration + Data safety form).
2. Upload `app-release.aab`, submit for review.
3. Later: Wear OS distribution (applicationId change + signing), string resource
   extraction for localization.

**Money:** free app with a "Buy Me a Pizza" link — no tracker needed until revenue exists.
