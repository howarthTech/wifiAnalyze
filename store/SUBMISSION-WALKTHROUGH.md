# Play Console submission walkthrough — WiFi Analyze

Companion to `RELEASE-CHECKLIST.md` (which has the exact text to paste into each form).
This file is the **click path**. Play Console reorganizes its menus often — if a label
below doesn't match, use the search bar at the top of the Console.

Upload artifact: `app/build/outputs/bundle/release/WiFi-Analyze-v1.2-upload.aab`
(versionCode 5, versionName 1.2, targetSdk 36 — verified).

> **"Version code N has already been used"** on upload means that code was already
> uploaded to your account (a draft or a test track) — Play reserves it forever. Either
> attach the existing build via **Add from library** instead of uploading, or bump
> `versionCode` in `app/build.gradle.kts`, rebuild, and upload the new file. Codes 3 and 4
> were consumed this way; the current build is **5**.

---

## 0. Before you start
- I can't do these steps for you — they require your Google login and are publishing
  actions only you should take. This is the guide; you drive.
- Have ready: the AAB above, the privacy-policy URL, and `RELEASE-CHECKLIST.md` open in
  another tab for the field values.

---

## PART A — one-time app setup (skip any section already showing a green check)

These live under **Policy → App content** and the dashboard's "Set up your app" list. You
must finish all of them once before Play will let you release to Production.

1. **Privacy policy** — paste
   `https://howarthtech.github.io/wifiAnalyze/store/privacy-policy.html`
2. **App access** — "All functionality is available without special access" (no login).
3. **Ads** — "No, my app does not contain ads."
4. **Content rating** — start the questionnaire, category Utility/Tools, answer No to all
   sensitive-content questions → submit → it issues ratings automatically.
5. **Target audience and content** — target age 18+ / general; "No" to appealing to children.
6. **Data safety** — see RELEASE-CHECKLIST §3. Key answers: collects/shares **no** user
   data; deletion available in-app. Declare that the app requests location (but does not
   collect it) and makes user-initiated calls to Cloudflare and 8.8.8.8.
7. **Government apps / Financial features / Health** — No / None / No.
8. **Advertising ID** — the app does not use it → declare "No, my app doesn't use
   advertising ID."

## PART B — store listing (Grow → Store presence → Main store listing)
Values are in `store/listing.md`.
- App name: `WiFi Analyze`
- Short description (73 chars) and full description — paste from `listing.md`.
- App icon: `store/icon-512.png` · Feature graphic: `store/feature-graphic.png`
- Phone screenshots: the 7 PNGs in `store/screenshots/`
- Category: Tools · add a contact email.
- Tags: pick ≤5 from Play's dropdown (guidance in `listing.md` — Tools, Wi-Fi/Networking,
  Smart home, Device info).

---

## PART C — upload the build and release

### First upload only: Play App Signing
On your first AAB upload Play offers **Play App Signing** — accept it. Google then holds the
real app-signing key and your `release.jks` becomes the *upload* key (a lost upload key can
be reset via support; see the key-backup README).

### Recommended path: test track first
1. Left nav **Test and release → Testing → Internal testing → Create new release**.
2. Upload `WiFi-Analyze-v1.2-upload.aab`.
3. Release name: set to `1.2 (5) — Initial release` (or leave Play's `5 (1.2)`).
4. Release notes: paste the `<en-US>…</en-US>` block from `RELEASE-CHECKLIST`/`RELEASE-NOTES`.
5. **Next → Save → Review release → Start rollout to Internal testing.**
6. On the Internal testing **Testers** tab, add tester emails and share the opt-in link;
   install from that link on your phone to confirm the Play-delivered build.

### Promote to Production
1. **Test and release → Production → Create release** (or "Promote" the internal release).
2. Confirm the same AAB (versionCode 5) is attached; keep the release notes.
3. Select countries/regions (all, or your choice).
4. **Review release → Start rollout to Production.**
5. First production release goes to Google **review** — hours to a few days.

Uploading versionCode 5 / targetSdk 36 clears the "update your target API level" warning.

---

## Account type — resolved

Confirmed **organization** account (Howarth Tech Solutions), so the 12-tester / 14-day
closed-testing requirement that applies to post-2023 personal accounts **does not apply**.
Cleared to go straight to Production. (The first production release still passes through
Google's standard content review — typically hours to a couple of days.)
