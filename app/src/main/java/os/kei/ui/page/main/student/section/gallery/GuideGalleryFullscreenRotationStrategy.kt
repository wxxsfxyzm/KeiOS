package os.kei.ui.page.main.student.section.gallery

private fun fitAreaForRatio(
    targetRatio: Float,
    viewportWidth: Float,
    viewportHeight: Float
): Float {
    val normalizedRatio = targetRatio.coerceAtLeast(0.1f)
    val safeViewportHeight = viewportHeight.coerceAtLeast(1f)
    val viewportRatio = viewportWidth / safeViewportHeight
    return if (viewportRatio >= normalizedRatio) {
        val fittedHeight = viewportHeight
        val fittedWidth = fittedHeight * normalizedRatio
        fittedWidth * fittedHeight
    } else {
        val fittedWidth = viewportWidth
        val fittedHeight = fittedWidth / normalizedRatio
        fittedWidth * fittedHeight
    }
}

internal fun resolveGuideImageTargetRotation(
    safeRatio: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    allowAutoRotate: Boolean,
    mediaAdaptiveRotationEnabled: Boolean,
    systemAutoRotateEnabled: Boolean,
    systemRotationDegrees: Int
): Int {
    val normalizedRatio = safeRatio.coerceAtLeast(0.1f)
    val normalArea = fitAreaForRatio(normalizedRatio, viewportWidth, viewportHeight)
    val rotatedRatio = (1f / normalizedRatio).coerceAtLeast(0.1f)
    val rotatedArea = fitAreaForRatio(rotatedRatio, viewportWidth, viewportHeight)
    val shouldRotate90 = normalizedRatio > 1.02f && rotatedArea > (normalArea * 1.12f)
    return if (mediaAdaptiveRotationEnabled) {
        if (allowAutoRotate && shouldRotate90) 90 else 0
    } else {
        if (systemAutoRotateEnabled) systemRotationDegrees else 0
    }
}
