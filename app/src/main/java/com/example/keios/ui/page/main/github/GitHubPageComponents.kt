package com.example.keios.ui.page.main

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.keios.ui.utils.AppIconCache
import com.kyant.capsule.ContinuousCapsule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun VersionValueRow(
    label: String,
    value: String,
    valueColor: Color,
    emphasized: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MiuixTheme.colorScheme.primary,
            modifier = Modifier.width(34.dp)
        )
        Text(
            text = value,
            color = valueColor,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun AppIcon(
    packageName: String,
    size: Dp
) {
    val context = LocalContext.current
    val bitmapState = produceState<Bitmap?>(initialValue = AppIconCache.get(packageName), packageName) {
        value = withContext(Dispatchers.IO) { AppIconCache.getOrLoad(context, packageName) }
    }
    val bitmap = bitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = packageName,
            modifier = Modifier
                .width(size)
                .height(size)
                .clip(ContinuousCapsule)
        )
    } else {
        Box(
            modifier = Modifier
                .width(size)
                .height(size)
                .clip(ContinuousCapsule),
            contentAlignment = Alignment.Center
        ) {
            Text("App", color = MiuixTheme.colorScheme.primary)
        }
    }
}
