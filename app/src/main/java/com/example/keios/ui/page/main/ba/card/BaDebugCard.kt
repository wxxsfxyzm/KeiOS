package com.example.keios.ui.page.main.ba

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.keios.ui.page.main.widget.GlassTextButton
import com.example.keios.ui.page.main.widget.GlassVariant
import com.kyant.backdrop.Backdrop

@Composable
internal fun BaDebugCard(
    backdrop: Backdrop?,
    onSendApTestNotification: () -> Unit,
    onTestCafePlus3Hours: () -> Unit,
) {
    val accentAmber = Color(0xFFF59E0B)

    BaGlassCard(
        backdrop = backdrop,
        accentColor = accentAmber,
        accentAlpha = 0f,
    ) {
        BaCardHeader(title = "Debug")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassTextButton(
                backdrop = backdrop,
                text = "AP 通知",
                textColor = Color(0xFF3B82F6),
                variant = GlassVariant.SheetPrimaryAction,
                onClick = onSendApTestNotification,
            )
            GlassTextButton(
                backdrop = backdrop,
                text = "咖啡厅 3h AP",
                textColor = Color(0xFF3B82F6),
                variant = GlassVariant.SheetPrimaryAction,
                onClick = onTestCafePlus3Hours,
            )
        }
    }
}
