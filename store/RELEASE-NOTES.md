# WiFi Analyze — Release Details

## This release at a glance
| | |
|---|---|
| **App name** | WiFi Analyze |
| **Package** | `com.wifianalyze` |
| **Version name** | 1.2 |
| **Version code** | 3 |
| **Release name (internal)** | `1.2 (3) — Initial release` |
| **Min / Target SDK** | 33 (Android 13) / 35 (Android 15) |
| **Upload artifact** | `app/build/outputs/bundle/release/WiFi-Analyze-v1.2-upload.aab` (4.98 MB, signed) |
| **Status** | First public release — nothing previously on Play |
| **Track** | Start on Internal testing, then promote to Production |

---

## Release notes shown to users (Play Console — max 500 chars/language)

Play accepts the notes inside a language tag. Paste this exact block into the
**Release notes** field (it already contains the `<en-US>` tag):

```
<en-US>
Know your WiFi and fix your home network.

See your signal in plain English, walk room to room to find dead spots, and get router placement tips based on your own readings. Check whether a spot is solid enough for smart home devices.

Advanced mode adds dBm readings, channel congestion charts, speed and latency tests, and an A-F network score.

Everything stays on your device. No account, no ads, no tracking.
</en-US>
```
(412 characters, within the 500 limit.)

---

## What this release contains (for your records — not shown to users)

**The app (features users get):**
- Simple mode: plain-English signal quality, room-by-room dead-spot mapping, router
  placement tips, IoT/smart-home readiness check, nearby-network congestion view
- Advanced mode: live dBm + stability score, channel usage & overlap charts, speed test
  (Cloudflare), latency test (gateway + 8.8.8.8), A–F network score, JSON export
- History charts (24h / 7d) for signal, speed, latency
- Home-screen widget, optional weak-signal notifications, dark mode

**Engineering work in v1.2 (the pre-release hardening pass):**
- Fixed the release build (missing `proguard-rules.pro` blocked every AAB)
- Scan rate-limiting to respect Android's ~4-scans-per-2-min throttle
- WiFi-off / Location-off guidance cards with one-tap deep links
- Permission flow recovers from an accidental single "Deny"
- Edge-triggered signal alerts (one alert, one all-clear — no 15-min repeats)
- Widget shows "Not Connected" instead of stale data when WiFi drops
- Friendly speed-test error messages; network-callback leak fix
- Play readiness: backup rules, dark launch theme, themed icon, widget preview,
  predictive back, corrected privacy policy + listing (precise location, latency
  disclosure), store assets resized to spec

**Testing:** Release-signed APK installed and verified on a physical device.

---

## Known limitations
- **Wear OS companion not included** — its applicationId (`com.wifianalyze.wear`) can't
  share the phone listing, and it lacks release signing. Documented for a later release.

## Next release checklist
- [ ] Bump `versionCode` (→4) and `versionName` in `app/build.gradle.kts`
- [ ] Use the changelog-style notes (saved in `listing.md`) — users now have a prior version
- [ ] Rebuild AAB, upload, update release notes
