package com.example.keios.ui.page.main.widget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun RowScope.LiquidGlassBottomBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val targetScale = when {
        pressed -> 0.96f
        selected -> 1.02f
        else -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 580f),
        label = "liquid_bottom_bar_item_scale"
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
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
    content: @Composable RowScope.() -> Unit
) {
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val safeTabsCount = tabsCount.coerceAtLeast(1)
    val selectedTabIndex = selectedIndex().coerceIn(0, safeTabsCount - 1)
    val shape = RoundedCornerShape(percent = 50)
    val primary = MiuixTheme.colorScheme.primary
    val horizontalPadding = AppChromeTokens.floatingBottomBarHorizontalPadding

    val containerFallbackColor = if (isLiquidEffectEnabled) {
        MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.36f)
    } else {
        MiuixTheme.colorScheme.surfaceContainer
    }
    val containerBorderColor = MiuixTheme.colorScheme.outline.copy(alpha = if (isLiquidEffectEnabled) 0.30f else 0.42f)
    val indicatorFallbackColor = primary.copy(alpha = if (isLiquidEffectEnabled) 0.22f else 0.26f)
    val indicatorBorderColor = primary.copy(alpha = if (isLiquidEffectEnabled) 0.56f else 0.64f)

    var dragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(selectedTabIndex.toFloat()) }
    var dragDistancePx by remember { mutableFloatStateOf(0f) }
    var barWidthPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(selectedTabIndex, safeTabsCount) {
        if (!dragging) {
            dragProgress = selectedTabIndex.toFloat()
        }
    }

    val settledProgress by animateFloatAsState(
        targetValue = selectedTabIndex.toFloat(),
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 520f),
        label = "liquid_bottom_bar_progress"
    )
    val activeProgress = if (dragging) dragProgress else settledProgress

    val tabWidthPx = remember(barWidthPx, safeTabsCount, density, horizontalPadding) {
        val horizontalInsets = with(density) { (horizontalPadding * 2).toPx() }
        ((barWidthPx - horizontalInsets).coerceAtLeast(0f) / safeTabsCount).coerceAtLeast(0.0001f)
    }
    val swayTargetPx = if (dragging) {
        val normalized = (dragDistancePx / tabWidthPx).fastCoerceIn(-1f, 1f)
        with(density) { 4.dp.toPx() } * normalized
    } else {
        0f
    }
    val swayPx by animateFloatAsState(
        targetValue = swayTargetPx,
        animationSpec = spring(dampingRatio = 0.70f, stiffness = 600f),
        label = "liquid_bottom_bar_sway"
    )
    val swayDp = with(density) { swayPx.toDp() }
    val indicatorScale by animateFloatAsState(
        targetValue = if (dragging) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.70f, stiffness = 620f),
        label = "liquid_bottom_bar_indicator_scale"
    )

    BoxWithConstraints(
        modifier = modifier
            .height(AppChromeTokens.floatingBottomBarOuterHeight)
            .fillMaxWidth()
            .onSizeChanged { barWidthPx = it.width.toFloat() }
            .clip(shape)
            .background(containerFallbackColor)
            .border(width = 1.dp, color = containerBorderColor, shape = shape)
            .pointerInput(safeTabsCount, tabWidthPx, isLtr, selectedTabIndex) {
                if (safeTabsCount <= 1 || tabWidthPx <= 0f) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = {
                        dragging = true
                        dragProgress = activeProgress
                        dragDistancePx = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val direction = if (isLtr) 1f else -1f
                        dragDistancePx += dragAmount
                        dragProgress = (dragProgress + (dragAmount / tabWidthPx) * direction)
                            .fastCoerceIn(0f, (safeTabsCount - 1).toFloat())
                    },
                    onDragCancel = {
                        dragging = false
                        dragDistancePx = 0f
                        dragProgress = selectedTabIndex.toFloat()
                    },
                    onDragEnd = {
                        val nextIndex = dragProgress.roundToInt().coerceIn(0, safeTabsCount - 1)
                        dragging = false
                        dragDistancePx = 0f
                        dragProgress = nextIndex.toFloat()
                        if (nextIndex != selectedTabIndex) {
                            onSelected(nextIndex)
                        }
                    }
                )
            }
    ) {
        val tabWidth = maxWidth / safeTabsCount
        val indicatorWidth = (tabWidth - horizontalPadding * 2).coerceAtLeast(28.dp)
        val minOffset = horizontalPadding
        val maxOffset = (maxWidth - indicatorWidth - horizontalPadding).coerceAtLeast(minOffset)
        val indicatorOffset = (tabWidth * activeProgress + horizontalPadding + swayDp)
            .coerceIn(minOffset, maxOffset)

        val containerSpec = LiquidBackdropSpec(
            cornerRadiusPx = with(density) { (AppChromeTokens.floatingBottomBarOuterHeight / 2).toPx() },
            refractionHeightPx = with(density) { 20.dp.toPx() },
            refractionOffsetPx = with(density) { 70.dp.toPx() },
            blurRadiusPx = 14f,
            dispersion = 0.50f,
            tintAlpha = 0.07f,
            tintRed = primary.red,
            tintGreen = primary.green,
            tintBlue = primary.blue
        )
        val indicatorSpec = LiquidBackdropSpec(
            cornerRadiusPx = with(density) { (AppChromeTokens.floatingBottomBarInnerHeight / 2).toPx() },
            refractionHeightPx = with(density) { 16.dp.toPx() },
            refractionOffsetPx = with(density) { 54.dp.toPx() },
            blurRadiusPx = 18f,
            dispersion = 0.58f,
            tintAlpha = 0.13f,
            tintRed = primary.red,
            tintGreen = primary.green,
            tintBlue = primary.blue
        )

        LiquidBackdropLayer(
            modifier = Modifier.fillMaxSize(),
            enabled = isLiquidEffectEnabled,
            fallbackColor = containerFallbackColor,
            spec = containerSpec
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = indicatorOffset)
                .height(AppChromeTokens.floatingBottomBarInnerHeight)
                .width(indicatorWidth)
                .graphicsLayer {
                    scaleX = indicatorScale
                    scaleY = indicatorScale
                }
                .clip(shape)
                .background(indicatorFallbackColor)
                .border(width = 1.dp, color = indicatorBorderColor, shape = shape)
        ) {
            LiquidBackdropLayer(
                modifier = Modifier.fillMaxSize(),
                enabled = isLiquidEffectEnabled,
                fallbackColor = indicatorFallbackColor,
                spec = indicatorSpec
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppChromeTokens.floatingBottomBarOuterHeight)
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}
