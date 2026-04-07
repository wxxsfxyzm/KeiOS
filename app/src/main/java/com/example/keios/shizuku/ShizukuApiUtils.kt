package com.example.keios.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

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
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
        publishStatus(currentStatus())
    }

    fun detach() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        statusCallback = null
    }

    fun requestPermissionIfNeeded() {
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
    }

    fun currentStatus(): String {
        if (!Shizuku.pingBinder()) return "Shizuku service unavailable (start Shizuku app first)"
        if (Shizuku.isPreV11()) return "Shizuku pre-v11 is unsupported"
        return if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            "Shizuku permission: granted"
        } else {
            "Shizuku permission: not granted"
        }
    }

    private fun publishStatus(message: String) {
        statusCallback?.invoke(message)
    }

    companion object {
        const val DEFAULT_REQUEST_CODE = 1001
    }
}
