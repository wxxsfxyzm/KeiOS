package os.kei.ui.page.main.widget.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.lerp

@Immutable
data class GlassEffectRuntime(
    val reducedProgress: Float = 0f
) {
    fun blurScaleFor(variant: GlassVariant): Float = lerp(
        start = 1f,
        stop = when (variant) {
            GlassVariant.Content -> 0.76f
            GlassVariant.Floating -> 0.78f
            else -> 0.80f
        },
        fraction = reducedProgress
    )

    fun lensScaleFor(variant: GlassVariant): Float = lerp(
        start = 1f,
        stop = when (variant) {
            GlassVariant.Content -> 0.74f
            GlassVariant.Floating -> 0.76f
            else -> 0.78f
        },
        fraction = reducedProgress
    )

    val interactionLensScale: Float
        get() = lerp(
            start = 1f,
            stop = 0.84f,
            fraction = reducedProgress
        )
}

val LocalGlassEffectRuntime = staticCompositionLocalOf { GlassEffectRuntime() }

@Composable
@ReadOnlyComposable
internal fun glassEffectRuntime(): GlassEffectRuntime = LocalGlassEffectRuntime.current

@Composable
@ReadOnlyComposable
internal fun resolvedGlassBlurDp(
    base: Dp,
    variant: GlassVariant
): Dp = (base * glassEffectRuntime().blurScaleFor(variant)).clampGlassBlur()

@Composable
@ReadOnlyComposable
internal fun resolvedGlassLensDp(
    base: Dp,
    variant: GlassVariant
): Dp = base * glassEffectRuntime().lensScaleFor(variant)
