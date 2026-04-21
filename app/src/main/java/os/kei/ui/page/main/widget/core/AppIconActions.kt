package os.kei.ui.page.main.widget.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AppCompactIconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MiuixTheme.colorScheme.primary,
    minSize: Dp = 30.dp
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        minWidth = minSize,
        minHeight = minSize,
        cornerRadius = minSize,
        backgroundColor = Color.Transparent
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}
