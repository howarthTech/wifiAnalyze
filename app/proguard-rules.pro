# Room, Hilt, Glance, WorkManager, and DataStore ship their own consumer R8 rules.
# The app builds JSON exports by hand (no reflection-based serializer), so only
# app-specific reflective entry points need keeping.

# Hilt worker instantiated reflectively by HiltWorkerFactory
-keep class com.wifianalyze.data.worker.SignalMonitorWorker { *; }

# Glance widget receiver referenced from the manifest
-keep class com.wifianalyze.ui.widget.WifiWidgetReceiver { *; }

# Keep source file names/line numbers readable in Play Console crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
