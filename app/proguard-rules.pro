# ---------- KeiOS R8 baseline ----------
# Keep enum member names stable because multiple stores persist enum.name across app restarts/upgrades.
-keepclassmembernames enum os.kei.** { *; }

# Keep manifest component class names stable for runtime introspection in About page.
-keepnames class os.kei.KeiOSApp
-keepnames class os.kei.MainActivity
-keepnames class os.kei.ui.page.main.github.share.GitHubShareImportActivity
-keepnames class os.kei.ui.page.main.os.shell.OsShellRunnerActivity
-keepnames class os.kei.ui.page.main.student.GuideVideoFullscreenActivity
-keepnames class os.kei.mcp.service.McpKeepAliveService
-keepnames class os.kei.feature.notification.NotificationActionReceiver
-keepnames class os.kei.core.background.AppBackgroundTickReceiver
-keep class com.miui.permcenter.AppPermissionInfo { *; }

# ShizukuApiUtils reflects these no-arg static method names.
# Keep only the reflective surface so the rest can still be optimized/shrunk.
-keepclassmembers class rikka.shizuku.Shizuku {
    public static *** getUid(...);
    public static *** getVersion(...);
    public static *** getServerPatchVersion(...);
    public static *** getSELinuxContext(...);
    public static *** getLatestServiceVersion(...);
    private static *** newProcess(java.lang.String[],java.lang.String[],java.lang.String);
}

# Keep annotation/signature metadata used by Kotlin + library runtime features.
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# Drop release log calls to reduce overhead and method count.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Ktor debug probe references JDK-only management APIs on Android.
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
