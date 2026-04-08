package com.example.keios.ui.utils

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.util.concurrent.TimeUnit

class ShizukuApiUtils(
    private val requestCode: Int = DEFAULT_REQUEST_CODE
) {

    private var statusCallback: ((String) -> Unit)? = null

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        publishStatus(currentStatus())
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        publishStatus("Shizuku service disconnected")
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { code, grantResult ->
        if (code != requestCode) return@OnRequestPermissionResultListener
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            publishStatus("Shizuku permission: granted")
        } else {
            publishStatus("Shizuku permission: denied")
        }
    }

    fun attach(onStatusChanged: (String) -> Unit) {
        statusCallback = onStatusChanged
        runCatching {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        }.onFailure {
            publishStatus("Shizuku init failed: ${it.javaClass.simpleName}")
        }
        publishStatus(currentStatus())
    }

    fun detach() {
        runCatching {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        }
        statusCallback = null
    }

    fun requestPermissionIfNeeded() {
        runCatching {
            when {
                !Shizuku.pingBinder() -> publishStatus("Shizuku service unavailable (start Shizuku app first)")
                Shizuku.isPreV11() -> publishStatus("Shizuku pre-v11 is unsupported")
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> {
                    publishStatus("Shizuku permission: granted")
                }

                Shizuku.shouldShowRequestPermissionRationale() -> {
                    publishStatus("Shizuku permission denied permanently")
                }

                else -> {
                    publishStatus("Requesting Shizuku permission...")
                    Shizuku.requestPermission(requestCode)
                }
            }
        }.onFailure {
            publishStatus("Shizuku request failed: ${it.javaClass.simpleName}")
        }
    }

    fun currentStatus(): String {
        return runCatching {
            if (!Shizuku.pingBinder()) return "Shizuku service unavailable (start Shizuku app first)"
            if (Shizuku.isPreV11()) return "Shizuku pre-v11 is unsupported"
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                "Shizuku permission: granted"
            } else {
                "Shizuku permission: not granted"
            }
        }.getOrDefault("Shizuku unavailable")
    }

    fun canUseCommand(): Boolean {
        return runCatching {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
    }

    fun execCommand(command: String, timeoutMs: Long = 2000L): String? {
        if (!canUseCommand()) return null
        return runCatching {
            val process = run {
                val method = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                )
                method.isAccessible = true
                method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            }
            val out = process.inputStream.bufferedReader().use { it.readText() }
            val err = process.errorStream.bufferedReader().use { it.readText() }
            process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
            (out.ifBlank { err }).trim().ifBlank { null }
        }.getOrNull()
    }

    private fun publishStatus(message: String) {
        statusCallback?.invoke(message)
    }

    companion object {
        const val DEFAULT_REQUEST_CODE = 1001
    }
}
