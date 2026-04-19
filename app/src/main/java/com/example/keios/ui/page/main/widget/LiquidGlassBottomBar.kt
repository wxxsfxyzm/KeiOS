package com.example.keios.ui.page.main.widget

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
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
private val LocalLiquidGlassBottomBarSelectionProgress = staticCompositionLocalOf<(Int) -> Float> { { 0f } }
private val LocalLiquidGlassBottomBarContentColor = staticCompositionLocalOf<(Int) -> Color> { { Color.Unspecified } }
private val LocalLiquidGlassBottomBarItemInteractive = staticCompositionLocalOf { true }

@Composable
fun liquidGlassBottomBarItemSelectionProgress(tabIndex: Int): Float {
    return LocalLiquidGlassBottomBarSelectionProgress.current(tabIndex)
}

@Composable
fun liquidGlassBottomBarItemContentColor(tabIndex: Int): Color {
    return LocalLiquidGlassBottomBarContentColor.current(tabIndex)
}

@Composable
fun RowScope.LiquidGlassBottomBarItem(
    selected: Boolean,
    tabIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val selectedScale = LocalLiquidGlassBottomBarTabScale.current
    val selectionProgress = liquidGlassBottomBarItemSelectionProgress(tabIndex)
    val interactive = LocalLiquidGlassBottomBarItemInteractive.current
    val targetScale = when {
        pressed -> 0.965f
        selected || selectionProgress > 0f -> lerp(1f, selectedScale(), selectionProgress)
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
            .then(
                if (interactive) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Tab,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
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
    content: @Composable RowScope.() -> Unit
) {
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val isInLightTheme = !isSystemInDarkTheme()
    val viewConfiguration = LocalViewConfiguration.current
    val safeTabsCount = tabsCount.coerceAtLeast(1)
    val clampedSelectedIndex = selectedIndex().coerceIn(0, safeTabsCount - 1)
    val currentSelectedIndex by rememberUpdatedState(clampedSelectedIndex)
    val currentOnSelected by rememberUpdatedState(onSelected)
    val animationScope = rememberCoroutineScope()
    val primary = MiuixTheme.colorScheme.primary
    val onSurface = MiuixTheme.colorScheme.onSurface
    val surfaceContainer = MiuixTheme.colorScheme.surfaceContainer
    val outline = MiuixTheme.colorScheme.outline
    val horizontalPadding = AppChromeTokens.floatingBottomBarHorizontalPadding
    val palette = rememberLiquidBottomBarPalette(
        isLiquidEffectEnabled = isLiquidEffectEnabled,
        isInLightTheme = isInLightTheme,
        primary = primary,
        onSurface = onSurface,
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
    val dragActivationThresholdPx = remember(viewConfiguration.touchSlop) {
        (viewConfiguration.touchSlop * 1.15f).coerceAtLeast(8f)
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
    val currentPanelOffset by rememberUpdatedState(panelOffset)

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
                    panelOffset = currentPanelOffset
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
        },
        LocalLiquidGlassBottomBarSelectionProgress provides { tabIndex ->
            (1f - abs(dampedDragAnimation.value - tabIndex)).fastCoerceIn(0f, 1f)
        },
        LocalLiquidGlassBottomBarContentColor provides { palette.inactiveContentColor },
        LocalLiquidGlassBottomBarItemInteractive provides true
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
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                LiquidBackdropLayer(
                    modifier = Modifier.fillMaxSize(),
                    enabled = isLiquidEffectEnabled,
                    fallbackColor = palette.baseFillColor,
                    spec = baseSpec
                )

                BottomBarContentLayer(
                    horizontalPadding = horizontalPadding,
                    content = content
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

                    BottomBarActiveContentLayer(
                        density = density,
                        isLtr = isLtr,
                        horizontalPadding = horizontalPadding,
                        trackWidthPx = trackWidthPx,
                        tabWidthPx = tabWidthPx,
                        activeValue = dampedDragAnimation.value,
                        panelOffset = panelOffset,
                        content = content
                    ) {
                        CompositionLocalProvider(
                            LocalLiquidGlassBottomBarSelectionProgress provides { 1f },
                            LocalLiquidGlassBottomBarContentColor provides { palette.activeContentColor },
                            LocalLiquidGlassBottomBarItemInteractive provides false,
                            content = it
                        )
                    }
                }

                if (tabWidthPx > 0f) {
                    val overlayGestureModifier = Modifier.pointerInput(
                        safeTabsCount,
                        totalWidthPx,
                        tabWidthPx,
                        isLtr,
                        horizontalPaddingPx,
                        currentSelectedIndex,
                        dragActivationThresholdPx
                    ) {
                        if (tabWidthPx <= 0f || totalWidthPx <= 0f) return@pointerInput
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            val fingerOffsetFromCenter = down.position.x - tabWidthPx / 2f
                            var dragMoved = false
                            var dragTravelPx = 0f
                            dampedDragAnimation.press()
                            try {
                                while (true) {
                                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                        ?: event.changes.firstOrNull { it.pressed }
                                    if (change == null || !change.pressed) break
                                    val dragAmount = change.position - change.previousPosition
                                    if (dragAmount.x != 0f || dragAmount.y != 0f) {
                                        dragTravelPx += abs(dragAmount.x)
                                        if (!dragMoved && dragTravelPx >= dragActivationThresholdPx) {
                                            dragMoved = true
                                        }
                                        if (dragMoved) {
                                            val currentIndicatorLeft = indicatorTranslationX(
                                                isLtr = isLtr,
                                                activeValue = dampedDragAnimation.value,
                                                tabWidthPx = tabWidthPx,
                                                panelOffset = currentPanelOffset
                                            ) + horizontalPaddingPx
                                            val globalFingerX = currentIndicatorLeft + change.position.x
                                            val desiredCenterX = (globalFingerX - fingerOffsetFromCenter).fastCoerceIn(
                                                horizontalPaddingPx + tabWidthPx * 0.5f,
                                                totalWidthPx - horizontalPaddingPx - tabWidthPx * 0.5f
                                            )
                                            val rawValue = if (isLtr) {
                                                ((desiredCenterX - horizontalPaddingPx) / tabWidthPx) - 0.5f
                                            } else {
                                                ((totalWidthPx - horizontalPaddingPx - desiredCenterX) / tabWidthPx) - 0.5f
                                            }.fastCoerceIn(0f, (safeTabsCount - 1).toFloat())
                                            dampedDragAnimation.snapToValue(rawValue)
                                            animationScope.launch {
                                                offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                                            }
                                        }
                                    }
                                    if (!event.changes.any { it.pressed }) break
                                }
                            } finally {
                                val targetIndex = dampedDragAnimation.value.fastRoundToInt()
                                    .fastCoerceIn(0, safeTabsCount - 1)
                                if (dragMoved) {
                                    dampedDragAnimation.animateToValue(targetIndex.toFloat())
                                    if (targetIndex != currentSelectedIndex) {
                                        currentOnSelected(targetIndex)
                                    }
                                } else {
                                    dampedDragAnimation.updateValue(currentSelectedIndex.toFloat())
                                    dampedDragAnimation.release()
                                }
                                animationScope.launch {
                                    offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
                                }
                            }
                        }
                    }
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
                            .then(overlayGestureModifier)
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
private fun BottomBarActiveContentLayer(
    density: androidx.compose.ui.unit.Density,
    isLtr: Boolean,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    trackWidthPx: Float,
    tabWidthPx: Float,
    activeValue: Float,
    panelOffset: Float,
    content: @Composable RowScope.() -> Unit,
    layeredContent: @Composable (@Composable () -> Unit) -> Unit
) {
    val indicatorTranslation = indicatorTranslationX(
        isLtr = isLtr,
        activeValue = activeValue,
        tabWidthPx = tabWidthPx,
        panelOffset = panelOffset
    )
    Box(
        modifier = Modifier
            .padding(horizontal = horizontalPadding)
            .graphicsLayer { translationX = indicatorTranslation }
            .clip(AppBottomBarShapes.capsule)
            .clearAndSetSemantics {}
            .height(AppChromeTokens.floatingBottomBarInnerHeight)
            .width(with(density) { tabWidthPx.toDp() }),
        contentAlignment = Alignment.CenterStart
    ) {
        layeredContent {
            Row(
                modifier = Modifier
                    .width(with(density) { trackWidthPx.toDp() })
                    .fillMaxHeight()
                    .graphicsLayer { translationX = -indicatorTranslation },
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
private fun BottomBarContentLayer(
    horizontalPadding: androidx.compose.ui.unit.Dp,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
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
                drawRect(color = palette.indicatorSurfaceOverlay(pressProgress))
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
    onSurface: Color,
    surfaceContainer: Color,
    outline: Color
): LiquidBottomBarPalette = remember(
    isLiquidEffectEnabled,
    isInLightTheme,
    primary,
    onSurface,
    surfaceContainer,
    outline
) {
    if (!isLiquidEffectEnabled) {
        LiquidBottomBarPalette(
            baseFillColor = surfaceContainer,
            baseBorderColor = outline.copy(alpha = 0.22f),
            indicatorFillColor = primary.copy(alpha = 0.18f),
            indicatorBorderColor = primary.copy(alpha = 0.34f),
            indicatorShadowColor = Color.Black.copy(alpha = 0.16f),
            inactiveContentColor = onSurface,
            activeContentColor = primary,
            indicatorRestOverlayColor = Color.Black.copy(alpha = 0.10f),
            indicatorPressedOverlayColor = Color.Black.copy(alpha = 0.03f)
        )
    } else if (isInLightTheme) {
        LiquidBottomBarPalette(
            baseFillColor = surfaceContainer.copy(alpha = 0.42f),
            baseBorderColor = Color.White.copy(alpha = 0.58f),
            indicatorFillColor = Color.White.copy(alpha = 0.09f),
            indicatorBorderColor = Color.White.copy(alpha = 0.84f),
            indicatorShadowColor = Color.Black.copy(alpha = 0.22f),
            inactiveContentColor = onSurface.copy(alpha = 0.88f),
            activeContentColor = primary,
            indicatorRestOverlayColor = Color.Black.copy(alpha = 0.10f),
            indicatorPressedOverlayColor = Color.Black.copy(alpha = 0.03f)
        )
    } else {
        LiquidBottomBarPalette(
            baseFillColor = surfaceContainer.copy(alpha = 0.20f),
            baseBorderColor = Color.White.copy(alpha = 0.20f),
            indicatorFillColor = Color.White.copy(alpha = 0.06f),
            indicatorBorderColor = Color.White.copy(alpha = 0.38f),
            indicatorShadowColor = Color.Black.copy(alpha = 0.32f),
            inactiveContentColor = onSurface.copy(alpha = 0.84f),
            activeContentColor = primary.copy(alpha = 0.98f),
            indicatorRestOverlayColor = Color.White.copy(alpha = 0.10f),
            indicatorPressedOverlayColor = Color.Black.copy(alpha = 0.03f)
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
    val indicatorShadowColor: Color,
    val inactiveContentColor: Color,
    val activeContentColor: Color,
    val indicatorRestOverlayColor: Color,
    val indicatorPressedOverlayColor: Color
) {
    fun indicatorSurfaceOverlay(pressProgress: Float): Color {
        return lerpColor(indicatorRestOverlayColor, indicatorPressedOverlayColor, pressProgress)
    }

    fun baseTopGlow(pressProgress: Float): List<Color> = listOf(
        Color.White.copy(alpha = lerp(0.050f, 0.072f, pressProgress)),
        Color.White.copy(alpha = lerp(0.014f, 0.020f, pressProgress)),
        Color.Transparent
    )

    fun baseBottomShade(pressProgress: Float): List<Color> = listOf(
        Color.Transparent,
        Color.Transparent,
        Color.Black.copy(alpha = lerp(0.018f, 0.028f, pressProgress))
    )

    fun indicatorTopGlow(pressProgress: Float): List<Color> = listOf(
        Color.White.copy(alpha = lerp(0.110f, 0.160f, pressProgress)),
        Color.White.copy(alpha = lerp(0.030f, 0.044f, pressProgress)),
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
