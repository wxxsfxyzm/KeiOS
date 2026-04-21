package os.kei.ui.page.main.student

import android.content.Context
import android.database.ContentObserver
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Display
import android.view.OrientationEventListener
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

internal fun isSystemAutoRotateEnabled(context: Context): Boolean {
    return runCatching {
        Settings.System.getInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            0
        ) == 1
    }.getOrDefault(false)
}

internal fun currentDisplayRotationDegrees(context: Context): Int {
    val rotation = runCatching { context.display.rotation }
        .getOrElse {
            runCatching {
                context.getSystemService(DisplayManager::class.java)
                    ?.getDisplay(Display.DEFAULT_DISPLAY)
                    ?.rotation
                    ?: Surface.ROTATION_0
            }.getOrDefault(Surface.ROTATION_0)
        }
    return when (rotation) {
        Surface.ROTATION_90 -> 270
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 90
        else -> 0
    }
}

internal fun normalizeRotationDegreesByOrientation(rawOrientation: Int): Int {
    val orientation = ((rawOrientation % 360) + 360) % 360
    return when {
        orientation in 45..134 -> 270
        orientation in 135..224 -> 180
        orientation in 225..314 -> 90
        else -> 0
    }
}

internal fun circularAngleDistance(a: Int, b: Int): Int {
    val diff = kotlin.math.abs(a - b) % 360
    return kotlin.math.min(diff, 360 - diff)
}

internal fun snapCardinalOrientation(rawOrientation: Int): Int? {
    val orientation = ((rawOrientation % 360) + 360) % 360
    val candidates = intArrayOf(0, 90, 180, 270)
    var best = 0
    var bestDistance = Int.MAX_VALUE
    candidates.forEach { candidate ->
        val distance = circularAngleDistance(orientation, candidate)
        if (distance < bestDistance) {
            bestDistance = distance
            best = candidate
        }
    }
    return if (bestDistance <= 24) best else null
}

@Composable
internal fun rememberSystemAutoRotateEnabled(active: Boolean): Boolean {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(isSystemAutoRotateEnabled(context)) }
    DisposableEffect(context, active) {
        if (!active) {
            enabled = false
            return@DisposableEffect onDispose { }
        }
        enabled = isSystemAutoRotateEnabled(context)
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                enabled = isSystemAutoRotateEnabled(context)
            }
        }
        val uri = Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION)
        context.contentResolver.registerContentObserver(uri, false, observer)
        onDispose {
            runCatching {
                context.contentResolver.unregisterContentObserver(observer)
            }
        }
    }
    return enabled
}

@Composable
internal fun rememberDeviceRotationDegrees(active: Boolean): Int {
    val context = LocalContext.current
    var degrees by remember { mutableStateOf(0) }
    DisposableEffect(context, active) {
        if (!active) {
            degrees = 0
            return@DisposableEffect onDispose { }
        }
        degrees = currentDisplayRotationDegrees(context)
        val handler = Handler(Looper.getMainLooper())
        var pendingDegrees = degrees
        var pendingApplyRunnable: Runnable? = null
        val listener = object : OrientationEventListener(context.applicationContext) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val snapped = snapCardinalOrientation(orientation) ?: return
                val nextDegrees = normalizeRotationDegreesByOrientation(snapped)
                if (nextDegrees == degrees) {
                    pendingApplyRunnable?.let(handler::removeCallbacks)
                    pendingApplyRunnable = null
                    pendingDegrees = degrees
                    return
                }
                pendingDegrees = nextDegrees
                pendingApplyRunnable?.let(handler::removeCallbacks)
                val applyRunnable = Runnable {
                    if (pendingDegrees != degrees) {
                        degrees = pendingDegrees
                    }
                }
                pendingApplyRunnable = applyRunnable
                handler.postDelayed(applyRunnable, 120L)
            }
        }
        if (listener.canDetectOrientation()) {
            listener.enable()
        }
        onDispose {
            listener.disable()
            pendingApplyRunnable?.let(handler::removeCallbacks)
            pendingApplyRunnable = null
        }
    }
    return degrees
}
