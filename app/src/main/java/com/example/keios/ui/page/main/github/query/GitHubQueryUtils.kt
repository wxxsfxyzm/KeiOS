package com.example.keios.ui.page.main.github.query

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import com.example.keios.feature.github.model.InstalledAppItem

internal const val installerXRevivedPackageName = "com.rosan.installer.x.revived"
internal const val packageInstallerOnlinePackageName = "io.github.vvb2060.packageinstaller"

internal data class DownloaderOption(
    val packageName: String,
    val label: String
)

internal data class OnlineShareTargetOption(
    val packageName: String,
    val label: String
)

internal val systemDefaultDownloaderOption = DownloaderOption(
    packageName = "",
    label = "系统默认值"
)

internal val systemDownloadManagerOption = DownloaderOption(
    packageName = "__system_download_manager__",
    label = "系统内置下载器"
)

internal val noOnlineShareTargetOption = OnlineShareTargetOption(
    packageName = "",
    label = "不联动"
)

internal fun queryOnlineShareTargetOptions(
    context: Context,
    appList: List<InstalledAppItem>
): List<OnlineShareTargetOption> {
    val knownTargets = listOf(
        OnlineShareTargetOption(installerXRevivedPackageName, "InstallerX Revived"),
        OnlineShareTargetOption(packageInstallerOnlinePackageName, "Package Installer")
    )
    return knownTargets.filter { target ->
        appList.any { it.packageName == target.packageName } || runCatching {
            context.packageManager.getPackageInfo(
                target.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
            true
        }.recoverCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(target.packageName, 0)
            true
        }.getOrDefault(false)
    }
}

internal fun queryDownloaderOptions(context: Context): List<DownloaderOption> {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/")).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
    }
    val resolved = runCatching {
        context.packageManager.queryIntentActivities(
            intent,
            PackageManager.ResolveInfoFlags.of(0)
        )
    }.recoverCatching {
        @Suppress("DEPRECATION")
        context.packageManager.queryIntentActivities(intent, 0)
    }.getOrDefault(emptyList<ResolveInfo>())

    return resolved.mapNotNull { it.toDownloaderOptionOrNull(context) }
        .filterNot { it.packageName == context.packageName }
        .distinctBy { it.packageName }
        .sortedWith(
            compareByDescending<DownloaderOption> { it.label.contains("推荐") }
                .thenBy { it.label.lowercase() }
        )
}

internal fun isPackageInstalled(context: Context, packageName: String): Boolean {
    return runCatching {
        context.packageManager.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(0)
        )
        true
    }.recoverCatching {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(packageName, 0)
        true
    }.getOrDefault(false)
}

private fun ResolveInfo.toDownloaderOptionOrNull(context: Context): DownloaderOption? {
    val packageName = activityInfo?.packageName?.trim().orEmpty()
    val label = loadLabel(context.packageManager).toString().trim().ifBlank { packageName }
    if (packageName.isBlank()) return null

    val normalizedPackage = packageName.lowercase()
    val normalizedLabel = label.lowercase()
    val combined = "$normalizedPackage $normalizedLabel"
    val excludedKeywords = listOf("android", "system ui", "package installer")
    val positiveKeywords = listOf(
        "download", "downloader", "downloadmanager", "manager", "loader",
        "adm", "idm", "1dm", "aria", "fetch", "torrent", "下载"
    )
    val knownDownloaderPackages = listOf(
        "com.dv.adm",
        "idm.internet.download.manager.plus",
        "idm.internet.download.manager",
        "idm.internet.download.manager.adm.lite",
        "com.apps2sd.adm"
    )
    if (excludedKeywords.any { combined == it || combined.contains(" $it") }) return null

    val decoratedLabel = when {
        positiveKeywords.any { combined.contains(it) } ||
            knownDownloaderPackages.any {
                normalizedPackage == it || normalizedPackage.startsWith("$it.")
            } -> "$label · 推荐"
        else -> label
    }
    return DownloaderOption(packageName = packageName, label = decoratedLabel)
}
