package com.example.keios.ui.page.main.widget.glass

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.capsule.ContinuousCapsule
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val LiquidSwitchTrackWidth = 52.dp
private val LiquidSwitchTrackHeight = 32.dp
private val LiquidSwitchThumbSize = 26.dp
private val LiquidSwitchThumbPadding = 3.dp

@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val isDark = isSystemInDarkTheme()
    val primary = MiuixTheme.colorScheme.primary

    val thumbOffsetX by animateDpAsState(
        targetValue = if (checked) {
            LiquidSwitchTrackWidth - LiquidSwitchThumbSize - LiquidSwitchThumbPadding
        } else {
            LiquidSwitchThumbPadding
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "liquid_switch_thumb_offset"
    )
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "liquid_switch_press_scale"
    )
    val switchAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.48f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "liquid_switch_enabled_alpha"
    )

    val offTrackColor = if (isDark) {
        MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.52f)
    } else {
        Color.White.copy(alpha = 0.68f)
    }
    val onTrackColor = primary.copy(alpha = if (isDark) 0.30f else 0.24f)
    val trackColor = lerp(offTrackColor, onTrackColor, if (checked) 1f else 0f)
    val offTrackBorderColor = if (isDark) {
        Color.White.copy(alpha = 0.24f)
    } else {
        MiuixTheme.colorScheme.onBackgroundVariant.copy(alpha = 0.24f)
    }
    val onTrackBorderColor = primary.copy(alpha = if (isDark) 0.54f else 0.64f)
    val trackBorderColor = lerp(offTrackBorderColor, onTrackBorderColor, if (checked) 1f else 0f)
    val thumbColor = if (checked) {
        Color.White.copy(alpha = if (isDark) 0.94f else 0.98f)
    } else {
        Color.White.copy(alpha = if (isDark) 0.88f else 0.96f)
    }

    val trackBackdrop = rememberLayerBackdrop()
    Box(
        modifier = modifier
            .requiredSize(width = LiquidSwitchTrackWidth, height = LiquidSwitchTrackHeight)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .alpha(switchAlpha)
            .clip(ContinuousCapsule)
            .layerBackdrop(trackBackdrop)
            .drawBackdrop(
                backdrop = trackBackdrop,
                shape = { ContinuousCapsule },
                effects = {
                    vibrancy()
                    blur(7.dp.toPx())
                    lens(5.dp.toPx(), 5.dp.toPx())
                },
                highlight = {
                    Highlight.Default.copy(alpha = if (checked) 0.94f else 0.72f)
                },
                shadow = {
                    Shadow.Default.copy(alpha = if (isDark) 0.26f else 0.18f)
                },
                innerShadow = {
                    InnerShadow(
                        alpha = if (isDark) 0.18f else 0.12f,
                        radius = 7.dp
                    )
                }
            )
            .background(trackColor, ContinuousCapsule)
            .border(1.dp, trackBorderColor, ContinuousCapsule),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .requiredSize(LiquidSwitchTrackWidth, LiquidSwitchTrackHeight)
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Switch,
                    interactionSource = interactionSource,
                    indication = null,
                    onValueChange = onCheckedChange
                )
        )
        Box(
            modifier = Modifier
                .offset(x = thumbOffsetX)
                .size(LiquidSwitchThumbSize)
                .clip(CircleShape)
                .drawBackdrop(
                    backdrop = trackBackdrop,
                    shape = { CircleShape },
                    effects = {
                        blur(6.dp.toPx())
                        vibrancy()
                    },
                    highlight = {
                        Highlight.Default.copy(alpha = if (isDark) 0.84f else 0.94f)
                    },
                    shadow = {
                        Shadow.Default.copy(alpha = if (isDark) 0.34f else 0.22f)
                    }
                )
                .background(thumbColor, CircleShape)
                .border(
                    width = 1.dp,
                    color = if (isDark) Color.White.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.72f),
                    shape = CircleShape
                )
        )
    }
}
