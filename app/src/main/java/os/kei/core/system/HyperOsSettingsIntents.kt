package os.kei.core.system

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.Settings
import com.miui.permcenter.AppPermissionInfo
import os.kei.R
import java.util.ArrayList
import java.util.HashMap

internal object HyperOsSettingsIntents {
    private const val SECURITY_CENTER_PACKAGE = "com.miui.securitycenter"
    private const val POWER_DETAIL_ACTIVITY =
        "com.miui.powercenter.legacypowerrank.PowerDetailActivity"
    private const val PERMISSION_APPS_MODIFY_ACTIVITY =
        "com.miui.permcenter.permissions.PermissionAppsModifyActivity"
    private const val HYPER_OS_VERSION_PREFIX = "OS3"
    private const val HYPER_OS_GET_INSTALLED_APPS_PERMISSION_ID = 57L
    private const val HYPER_OS_SETTINGS_RELATIVE_GROUP_ID = 262144
    private const val HYPER_OS_PERMISSION_LEVEL_DEFAULT = 1
    private const val HYPER_OS_APP_OP_GET_INSTALLED_APPS = 10022
    private const val PERMISSION_ACTION_ACCEPT = 3
    private const val PERMISSION_ACTION_REJECT = 1
    private const val APP_OP_MODE_ALLOWED = 0

    fun isHyperOs3Device(): Boolean {
        return readSystemProperty("ro.mi.os.version.name")
            ?.startsWith(HYPER_OS_VERSION_PREFIX, ignoreCase = true) == true
    }

    fun buildBatteryOptimizationIntent(context: Context, alreadyIgnored: Boolean): Intent? {
        val packageManager = context.packageManager
        val packageUri = Uri.parse("package:${context.packageName}")
        val candidateIntents = buildList {
            if (isHyperOs3Device()) {
                add(buildPowerDetailIntent(context))
            }
            if (!alreadyIgnored) {
                add(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri))
                add(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
            add(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri))
        }
        return candidateIntents.firstOrNull { intent ->
            intent.resolveActivity(packageManager) != null
        }?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun buildAppListPermissionIntent(context: Context): Intent? {
        val packageManager = context.packageManager
        val packageUri = Uri.parse("package:${context.packageName}")
        val candidateIntents = buildList {
            if (isHyperOs3Device()) {
                add(buildPermissionAppsModifyIntent(context))
            }
            add(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri))
        }
        return candidateIntents.firstOrNull { intent ->
            intent.resolveActivity(packageManager) != null
        }?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun buildPowerDetailIntent(context: Context): Intent {
        return Intent().setComponent(
            ComponentName(SECURITY_CENTER_PACKAGE, POWER_DETAIL_ACTIVITY)
        ).apply {
            data = Uri.parse("package:${context.packageName}")
            putExtra("iconPackage", context.packageName)
            putExtra("package_name", context.packageName)
            putExtra("uid", context.applicationInfo.uid)
            putExtra("showMenus", false)
            putExtra("UserId", resolveAndroidUserId(context.applicationInfo.uid))
        }
    }

    private fun buildPermissionAppsModifyIntent(context: Context): Intent {
        val permissionId = HYPER_OS_GET_INSTALLED_APPS_PERMISSION_ID
        val permissionAction = resolveGetInstalledAppsPermissionAction(context)
        return Intent().setComponent(
            ComponentName(SECURITY_CENTER_PACKAGE, PERMISSION_APPS_MODIFY_ACTIVITY)
        ).apply {
            putExtra("extra_permission_info", buildPermissionInfo(context, permissionId, permissionAction))
            putExtra("permission_name", context.getString(R.string.settings_app_list_access_permission_name))
            putExtra("group_id", HYPER_OS_SETTINGS_RELATIVE_GROUP_ID)
            putExtra("permission_id", ArrayList<Long>().apply { add(permissionId) })
            putExtra("permission_action", permissionAction)
        }
    }

    private fun buildPermissionInfo(
        context: Context,
        permissionId: Long,
        permissionAction: Int
    ): AppPermissionInfo {
        val permissionActionMap = HashMap<Long, Int>().apply {
            put(permissionId, permissionAction)
        }
        val permissionLevelMap = HashMap<Long, Int>().apply {
            put(permissionId, HYPER_OS_PERMISSION_LEVEL_DEFAULT)
        }
        return AppPermissionInfo().apply {
            setPackageName(context.packageName)
            setLabel(context.packageManager.getApplicationLabel(context.applicationInfo).toString())
            setUid(context.applicationInfo.uid)
            setTargetSdkVersion(context.applicationInfo.targetSdkVersion)
            setSystem((context.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0)
            setPermissionToAction(permissionActionMap)
            setPermissionToLevel(permissionLevelMap)
        }
    }

    private fun resolveGetInstalledAppsPermissionAction(context: Context): Int {
        val mode = queryAppOpMode(
            context = context,
            opCode = HYPER_OS_APP_OP_GET_INSTALLED_APPS
        )
        return if (mode == APP_OP_MODE_ALLOWED) {
            PERMISSION_ACTION_ACCEPT
        } else {
            PERMISSION_ACTION_REJECT
        }
    }

    private fun queryAppOpMode(context: Context, opCode: Int): Int? {
        val appOpsManager = context.getSystemService(AppOpsManager::class.java) ?: return null
        return runCatching {
            val method = AppOpsManager::class.java.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            method.invoke(
                appOpsManager,
                opCode,
                context.applicationInfo.uid,
                context.packageName
            ) as? Int
        }.getOrNull()
    }

    private fun readSystemProperty(key: String): String? {
        return runCatching {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getDeclaredMethod("get", String::class.java)
            method.isAccessible = true
            method.invoke(null, key) as? String
        }.getOrNull()?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun resolveAndroidUserId(uid: Int): Int {
        return uid / 100000
    }
}
