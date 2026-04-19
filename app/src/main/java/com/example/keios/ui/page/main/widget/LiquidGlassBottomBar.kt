package com.example.keios.ui.page.main.widget

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.example.keios.ui.animation.DampedDragAnimation
import com.example.keios.ui.animation.InteractiveHighlight
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.MiuixTheme

val LocalLiquidGlassBottomBarTabScale = staticCompositionLocalOf { { 1.01f } }

@Composable
fun RowScope.LiquidGlassBottomBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val selectedScale = LocalLiquidGlassBottomBarTabScale.current
    val targetScale = when {
        pressed -> 0.965f
        selected -> selectedScale()
        else -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 580f),
        label = "liquid_bottom_bar_item_scale"
    )
    Column(
        modifier = modifier
            .clip(AppBottomBarShapes.capsule)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .fillMaxHeight()
            .weight(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun LiquidGlassBottomBar(
    modifier: Modifier = Modifier,
    selectedIndex: () -> Int,
    onSelected: (index: Int) -> Unit,
    tabsCount: Int,
    isLiquidEffectEnabled: Boolean = true,
    onReselectCurrent: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val isInLightTheme = !isSystemInDarkTheme()
    val safeTabsCount = tabsCount.coerceAtLeast(1)
    val clampedSelectedIndex = selectedIndex().coerceIn(0, safeTabsCount - 1)
    val currentSelectedIndex by rememberUpdatedState(clampedSelectedIndex)
    val currentOnSelected by rememberUpdatedState(onSelected)
    val currentOnReselect by rememberUpdatedState(onReselectCurrent)
    val animationScope = rememberCoroutineScope()
    val primary = MiuixTheme.colorScheme.primary
    val surfaceContainer = MiuixTheme.colorScheme.surfaceContainer
    val outline = MiuixTheme.colorScheme.outline
    val horizontalPadding = AppChromeTokens.floatingBottomBarHorizontalPadding
    val palette = rememberLiquidBottomBarPalette(
        isLiquidEffectEnabled = isLiquidEffectEnabled,
        isInLightTheme = isInLightTheme,
        primary = primary,
        surfaceContainer = surfaceContainer,
        outline = outline
    )

    var tabWidthPx by remember { mutableFloatStateOf(0f) }
    var totalWidthPx by remember { mutableFloatStateOf(0f) }
    val horizontalPaddingPx = with(density) { horizontalPadding.toPx() }
    val trackWidthPx by remember(totalWidthPx, horizontalPaddingPx) {
        derivedStateOf {
            (totalWidthPx - horizontalPaddingPx * 2f).coerceAtLeast(0f)
        }
    }

    val offsetAnimation = remember { Animatable(0f) }
    val panelOffset by remember(density, totalWidthPx) {
        derivedStateOf {
            if (totalWidthPx == 0f) {
                0f
            } else {
                val fraction = (offsetAnimation.value / totalWidthPx).fastCoerceIn(-1f, 1f)
                with(density) {
                    4f.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                }
            }
        }
    }

    class DampedDragAnimationHolder {
        var instance: DampedDragAnimation? = null
    }

    val holder = remember { DampedDragAnimationHolder() }
    val dampedDragAnimation = remember(animationScope, safeTabsCount, density, isLtr) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = clampedSelectedIndex.toFloat(),
            valueRange = 0f..(safeTabsCount - 1).toFloat(),
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 78f / 56f,
            canDrag = { offset ->
                if (tabWidthPx == 0f || totalWidthPx == 0f) {
                    return@DampedDragAnimation false
                }
                val currentValue = holder.instance?.value ?: clampedSelectedIndex.toFloat()
                val globalTouchX = indicatorTranslationX(
                    isLtr = isLtr,
                    activeValue = currentValue,
                    tabWidthPx = tabWidthPx,
                    panelOffset = 0f
                ) + horizontalPaddingPx + offset.x
                globalTouchX in 0f..totalWidthPx
            },
            onDragStarted = {},
            onDragStopped = {
                val targetIndex = value.fastRoundToInt().fastCoerceIn(0, safeTabsCount - 1)
                animateToValue(targetIndex.toFloat())
                if (targetIndex != currentSelectedIndex) {
                    currentOnSelected(targetIndex)
                }
                animationScope.launch {
                    offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
                }
            },
            onDrag = { _, dragAmount ->
                if (tabWidthPx > 0f) {
                    val delta = if (isLtr) dragAmount.x else -dragAmount.x
                    snapToValue(
                        (value + delta / tabWidthPx)
                            .fastCoerceIn(0f, (safeTabsCount - 1).toFloat())
                    )
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                }
            }
        )
    }
    holder.instance = dampedDragAnimation

    LaunchedEffect(clampedSelectedIndex, safeTabsCount) {
        dampedDragAnimation.updateValue(clampedSelectedIndex.toFloat())
    }

    val pressProgress = if (isLiquidEffectEnabled) dampedDragAnimation.pressProgress else 0f
    val interactiveHighlight = if (isLiquidEffectEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        remember(animationScope, tabWidthPx, isLtr) {
            InteractiveHighlight(
                animationScope = animationScope,
                position = { size, _ ->
                    Offset(
                        horizontalPaddingPx + indicatorCenterX(
                            isLtr = isLtr,
                            activeValue = dampedDragAnimation.value,
                            tabWidthPx = tabWidthPx,
                            trackWidthPx = trackWidthPx,
                            panelOffset = panelOffset
                        ),
                        size.height / 2f
                    )
                },
                highlightColor = Color.White,
                highlightStrength = if (isInLightTheme) 0.64f else 0.92f,
                highlightRadiusScale = if (isInLightTheme) 0.88f else 1.10f
            )
        }
    } else {
        null
    }

    val baseSpec = rememberBottomBarBaseSpec(density, pressProgress)
    val indicatorSpec = rememberBottomBarIndicatorSpec(density, pressProgress)

    CompositionLocalProvider(
        LocalLiquidGlassBottomBarTabScale provides {
            if (isLiquidEffectEnabled) lerp(1.01f, 1.08f, pressProgress) else 1.01f
        }
    ) {
        Box(
            modifier = modifier.width(IntrinsicSize.Min),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coords ->
                        totalWidthPx = coords.size.width.toFloat()
                        tabWidthPx = (trackWidthPx / safeTabsCount).coerceAtLeast(0f)
                    }
                    .height(AppChromeTokens.floatingBottomBarOuterHeight)
                    .graphicsLayer {
                        shadowElevation = with(density) {
                            if (isInLightTheme) 18.dp.toPx() else 14.dp.toPx()
                        }
                        ambientShadowColor = Color.Black.copy(alpha = if (isInLightTheme) 0.12f else 0.28f)
                        spotShadowColor = Color.Black.copy(alpha = if (isInLightTheme) 0.16f else 0.36f)
                        shape = AppBottomBarShapes.capsule
                        clip = false
                    }
                    .clip(AppBottomBarShapes.capsule)
                    .border(
                        width = 1.dp,
                        color = palette.baseBorderColor,
                        shape = AppBottomBarShapes.capsule
                    )
                    .then(if (interactiveHighlight != null) interactiveHighlight.modifier else Modifier)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(palette.baseTopGlow(pressProgress)),
                            blendMode = BlendMode.Plus
                        )
                        drawRect(
                            brush = Brush.verticalGradient(palette.baseBottomShade(pressProgress)),
                            blendMode = BlendMode.Multiply
                        )
                    }
            ) {
                LiquidBackdropLayer(
                    modifier = Modifier.fillMaxSize(),
                    enabled = isLiquidEffectEnabled,
                    fallbackColor = palette.baseFillColor,
                    spec = baseSpec
                )

                if (tabWidthPx > 0f) {
                    BottomBarIndicatorVisualLayer(
                        density = density,
                        isLtr = isLtr,
                        horizontalPadding = horizontalPadding,
                        tabWidthPx = tabWidthPx,
                        activeValue = dampedDragAnimation.value,
                        panelOffset = panelOffset,
                        pressProgress = pressProgress,
                        dragAnimation = dampedDragAnimation,
                        palette = palette,
                        spec = indicatorSpec,
                        isLiquidEffectEnabled = isLiquidEffectEnabled
                    )
                }

                BottomBarContentLayer(
                    horizontalPadding = horizontalPadding,
                    tabWidthPx = tabWidthPx,
                    tabsCount = safeTabsCount,
                    content = content
                )

                if (tabWidthPx > 0f) {
                    val overlayInteractionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding)
                            .graphicsLayer {
                                translationX = indicatorTranslationX(
                                    isLtr = isLtr,
                                    activeValue = dampedDragAnimation.value,
                                    tabWidthPx = tabWidthPx,
                                    panelOffset = panelOffset
                                )
                                scaleX = dragAnimationScaleX(dampedDragAnimation)
                                scaleY = dragAnimationScaleY(dampedDragAnimation)
                            }
                            .clip(AppBottomBarShapes.capsule)
                            .then(if (interactiveHighlight != null) interactiveHighlight.gestureModifier else Modifier)
                            .then(dampedDragAnimation.modifier)
                            .then(
                                if (currentOnReselect != null) {
                                    val reselectAction = currentOnReselect
                                    Modifier.clickable(
                                        interactionSource = overlayInteractionSource,
                                        indication = null,
                                        role = Role.Tab,
                                        onClick = { reselectAction?.invoke() }
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clearAndSetSemantics {}
                            .height(AppChromeTokens.floatingBottomBarInnerHeight)
                            .width(with(density) { tabWidthPx.toDp() })
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarContentLayer(
    horizontalPadding: androidx.compose.ui.unit.Dp,
    tabWidthPx: Float,
    tabsCount: Int,
    content: @Composable RowScope.() -> Unit
) {
    val separatorColor = if (isSystemInDarkTheme()) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.Black.copy(alpha = 0.05f)
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding)
            .drawWithContent {
                if (tabWidthPx > 0f && tabsCount > 1) {
                    for (index in 1 until tabsCount) {
                        val x = tabWidthPx * index
                        drawLine(
                            color = separatorColor,
                            start = Offset(x, size.height * 0.24f),
                            end = Offset(x, size.height * 0.76f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
                drawContent()
            },
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun BottomBarIndicatorVisualLayer(
    density: androidx.compose.ui.unit.Density,
    isLtr: Boolean,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    tabWidthPx: Float,
    activeValue: Float,
    panelOffset: Float,
    pressProgress: Float,
    dragAnimation: DampedDragAnimation,
    palette: LiquidBottomBarPalette,
    spec: LiquidBackdropSpec,
    isLiquidEffectEnabled: Boolean
) {
    Box(
        modifier = Modifier
            .padding(horizontal = horizontalPadding)
            .graphicsLayer {
                translationX = indicatorTranslationX(
                    isLtr = isLtr,
                    activeValue = activeValue,
                    tabWidthPx = tabWidthPx,
                    panelOffset = panelOffset
                )
                scaleX = dragAnimationScaleX(dragAnimation)
                scaleY = dragAnimationScaleY(dragAnimation)
                shadowElevation = with(density) { lerp(4f, 12f, pressProgress).dp.toPx() }
                ambientShadowColor = palette.indicatorShadowColor
                spotShadowColor = palette.indicatorShadowColor
                shape = AppBottomBarShapes.capsule
                clip = false
            }
            .clip(AppBottomBarShapes.capsule)
            .border(
                width = 1.dp,
                color = palette.indicatorBorderColor,
                shape = AppBottomBarShapes.capsule
            )
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(palette.indicatorTopGlow(pressProgress)),
                    blendMode = BlendMode.Plus
                )
                drawRect(
                    brush = Brush.verticalGradient(palette.indicatorBottomShade(pressProgress)),
                    blendMode = BlendMode.Multiply
                )
            }
            .height(AppChromeTokens.floatingBottomBarInnerHeight)
            .width(with(density) { tabWidthPx.toDp() })
    ) {
        LiquidBackdropLayer(
            modifier = Modifier.fillMaxSize(),
            enabled = isLiquidEffectEnabled,
            fallbackColor = palette.indicatorFillColor,
            spec = spec
        )
    }
}

@Composable
private fun rememberLiquidBottomBarPalette(
    isLiquidEffectEnabled: Boolean,
    isInLightTheme: Boolean,
    primary: Color,
    surfaceContainer: Color,
    outline: Color
): LiquidBottomBarPalette = remember(isLiquidEffectEnabled, isInLightTheme, primary, surfaceContainer, outline) {
    if (!isLiquidEffectEnabled) {
        LiquidBottomBarPalette(
            baseFillColor = surfaceContainer,
            baseBorderColor = outline.copy(alpha = 0.22f),
            indicatorFillColor = primary.copy(alpha = 0.18f),
            indicatorBorderColor = primary.copy(alpha = 0.34f),
            indicatorShadowColor = Color.Black.copy(alpha = 0.16f)
        )
    } else if (isInLightTheme) {
        LiquidBottomBarPalette(
            baseFillColor = Color.White.copy(alpha = 0.20f),
            baseBorderColor = Color.White.copy(alpha = 0.52f),
            indicatorFillColor = Color.White.copy(alpha = 0.075f),
            indicatorBorderColor = Color.White.copy(alpha = 0.62f),
            indicatorShadowColor = Color.Black.copy(alpha = 0.14f)
        )
    } else {
        LiquidBottomBarPalette(
            baseFillColor = Color.White.copy(alpha = 0.05f),
            baseBorderColor = Color.White.copy(alpha = 0.18f),
            indicatorFillColor = Color.White.copy(alpha = 0.032f),
            indicatorBorderColor = Color.White.copy(alpha = 0.24f),
            indicatorShadowColor = Color.Black.copy(alpha = 0.28f)
        )
    }
}

@Composable
private fun rememberBottomBarBaseSpec(
    density: androidx.compose.ui.unit.Density,
    pressProgress: Float
): LiquidBackdropSpec = remember(density, pressProgress) {
    with(density) {
        LiquidBackdropSpec(
            cornerRadiusPx = (AppChromeTokens.floatingBottomBarOuterHeight / 2).toPx(),
            refractionHeightPx = lerp(14f, 18f, pressProgress).dp.toPx(),
            refractionOffsetPx = lerp(42f, 48f, pressProgress).dp.toPx(),
            blurRadiusPx = lerp(6.5f, 8.5f, pressProgress),
            dispersion = lerp(0.08f, 0.12f, pressProgress),
            tintAlpha = 0f,
            tintRed = 1f,
            tintGreen = 1f,
            tintBlue = 1f
        )
    }
}

@Composable
private fun rememberBottomBarIndicatorSpec(
    density: androidx.compose.ui.unit.Density,
    pressProgress: Float
): LiquidBackdropSpec = remember(density, pressProgress) {
    with(density) {
        LiquidBackdropSpec(
            cornerRadiusPx = (AppChromeTokens.floatingBottomBarInnerHeight / 2).toPx(),
            refractionHeightPx = lerp(18f, 24f, pressProgress).dp.toPx(),
            refractionOffsetPx = lerp(48f, 62f, pressProgress).dp.toPx(),
            blurRadiusPx = lerp(10.5f, 14.5f, pressProgress),
            dispersion = lerp(0.20f, 0.34f, pressProgress),
            tintAlpha = 0f,
            tintRed = 1f,
            tintGreen = 1f,
            tintBlue = 1f
        )
    }
}

private fun indicatorTranslationX(
    isLtr: Boolean,
    activeValue: Float,
    tabWidthPx: Float,
    panelOffset: Float
): Float {
    val progressOffset = activeValue * tabWidthPx
    return if (isLtr) progressOffset + panelOffset else -progressOffset + panelOffset
}

private fun indicatorCenterX(
    isLtr: Boolean,
    activeValue: Float,
    tabWidthPx: Float,
    trackWidthPx: Float,
    panelOffset: Float
): Float {
    return if (isLtr) {
        (activeValue + 0.5f) * tabWidthPx + panelOffset
    } else {
        trackWidthPx - (activeValue + 0.5f) * tabWidthPx + panelOffset
    }
}

private fun dragAnimationScaleX(animation: DampedDragAnimation): Float {
    return animation.scaleX /
        (1f - (animation.velocity / 10f * 0.75f).fastCoerceIn(-0.2f, 0.2f))
}

private fun dragAnimationScaleY(animation: DampedDragAnimation): Float {
    return animation.scaleY *
        (1f - (animation.velocity / 10f * 0.25f).fastCoerceIn(-0.2f, 0.2f))
}

@Stable
private class LiquidBottomBarPalette(
    val baseFillColor: Color,
    val baseBorderColor: Color,
    val indicatorFillColor: Color,
    val indicatorBorderColor: Color,
    val indicatorShadowColor: Color
) {
    fun baseTopGlow(pressProgress: Float): List<Color> = listOf(
        Color.White.copy(alpha = lerp(0.070f, 0.096f, pressProgress)),
        Color.White.copy(alpha = lerp(0.018f, 0.026f, pressProgress)),
        Color.Transparent
    )

    fun baseBottomShade(pressProgress: Float): List<Color> = listOf(
        Color.Transparent,
        Color.Transparent,
        Color.Black.copy(alpha = lerp(0.018f, 0.028f, pressProgress))
    )

    fun indicatorTopGlow(pressProgress: Float): List<Color> = listOf(
        Color.White.copy(alpha = lerp(0.080f, 0.118f, pressProgress)),
        Color.White.copy(alpha = lerp(0.020f, 0.032f, pressProgress)),
        Color.Transparent
    )

    fun indicatorBottomShade(pressProgress: Float): List<Color> = listOf(
        Color.Transparent,
        Color.Transparent,
        Color.Black.copy(alpha = lerp(0.028f, 0.042f, pressProgress))
    )
}

private object AppBottomBarShapes {
    val capsule = androidx.compose.foundation.shape.RoundedCornerShape(percent = 50)
}
