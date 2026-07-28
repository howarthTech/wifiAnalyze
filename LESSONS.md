# wifiAnalyze — dev lessons (senior memory)

One generalized line per surprise — a command that failed unexpectedly, a wrong
assumption about the stack, a correction or denial from Darrell. Read at session
start (after CLAUDE.md); append before finishing. Newest last; prune duplicates.

- No `java` on PATH here: prefix Gradle with `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"`.
- Google Drive locks `app\build` / `wear\build`, so Gradle fails with AccessDeniedException on resource-merge tasks; fix is `.\gradlew.bat --stop` then delete the build dir — expect this most sessions, and delete BOTH module build dirs since the failure moves from :app to :wear.
- A `proguardFiles(..., "proguard-rules.pro")` entry pointing at a nonexistent file silently passes debug builds and only breaks release — verify release builds, not just debug, before calling a release "ready".
- Test the RELEASE-signed APK (`assembleRelease`), not the debug APK, when validating a build destined for Play — debug skips R8 so it can't catch shrinker breakage.
