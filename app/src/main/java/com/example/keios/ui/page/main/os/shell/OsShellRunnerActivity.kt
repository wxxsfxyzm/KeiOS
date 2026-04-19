package com.example.keios.ui.page.main

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.keios.R
import com.example.keios.core.prefs.AppThemeMode
import com.example.keios.core.prefs.UiPrefs
import com.example.keios.core.system.ShizukuApiUtils
import com.example.keios.ui.page.main.widget.AppCardHeader
import com.example.keios.ui.page.main.widget.AppChromeTokens
import com.example.keios.ui.page.main.widget.AppPageLazyColumn
import com.example.keios.ui.page.main.widget.AppPageScaffold
import com.example.keios.ui.page.main.widget.AppSurfaceCard
import com.example.keios.ui.page.main.widget.AppTypographyTokens
import com.example.keios.ui.page.main.widget.GlassIconButton
import com.example.keios.ui.page.main.widget.GlassSearchField
import com.example.keios.ui.page.main.widget.GlassVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Replace
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OsShellRunnerActivity : ComponentActivity() {
    private var shizukuStatus by mutableStateOf("Shizuku status: initializing...")
    private val shizukuApiUtils = ShizukuApiUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        shizukuApiUtils.attach { status -> shizukuStatus = status }

        setContent {
            val appThemeMode = UiPrefs.getAppThemeMode()
            val colorSchemeMode = when (appThemeMode) {
                AppThemeMode.FOLLOW_SYSTEM -> ColorSchemeMode.System
                AppThemeMode.LIGHT -> ColorSchemeMode.Light
                AppThemeMode.DARK -> ColorSchemeMode.Dark
            }
            val controller = ThemeController(colorSchemeMode)

            MiuixTheme(controller = controller) {
                OsShellRunnerPage(
                    shizukuStatus = shizukuStatus,
                    canRunShellCommand = shizukuApiUtils.canUseCommand(),
                    onRequestShizukuPermission = { shizukuApiUtils.requestPermissionIfNeeded() },
                    onRunShellCommand = { command ->
                        withContext(Dispatchers.IO) {
                            shizukuApiUtils.execCommand(command = command, timeoutMs = 15_000L)
                        }
                    },
                    onClose = { finish() }
                )
            }
        }
    }

    override fun onDestroy() {
        shizukuApiUtils.detach()
        super.onDestroy()
    }

    companion object {
        fun launch(context: Context) {
            val hostActivity = context.findHostActivity()
            val intent = Intent(context, OsShellRunnerActivity::class.java).apply {
                if (hostActivity == null) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (hostActivity != null) {
                hostActivity.startActivity(intent)
            } else {
                context.startActivity(intent)
            }
        }
    }
}

private tailrec fun Context.findHostActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext?.findHostActivity()
        else -> null
    }
}

@Composable
private fun OsShellRunnerPage(
    shizukuStatus: String,
    canRunShellCommand: Boolean,
    onRequestShizukuPermission: () -> Unit,
    onRunShellCommand: suspend (String) -> String?,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    val scope = rememberCoroutineScope()
    val pageListState = rememberLazyListState()
    val shellPageTitle = stringResource(R.string.os_shell_page_title)
    val inputTitle = stringResource(R.string.os_shell_input_title)
    val outputTitle = stringResource(R.string.os_shell_output_title)
    val outputHint = stringResource(R.string.os_shell_output_hint)
    val runActionDescription = stringResource(R.string.os_shell_action_run)
    val requestPermissionDescription = stringResource(R.string.os_shell_action_request_permission)
    val clearInputDescription = stringResource(R.string.os_shell_action_clear_input)
    val clearOutputDescription = stringResource(R.string.os_shell_action_clear_output)
    val outputRunningSubtitle = stringResource(R.string.os_shell_output_subtitle_running)
    val outputReadySubtitle = stringResource(R.string.os_shell_output_subtitle_ready)
    val noOutputText = stringResource(R.string.os_shell_run_empty_output)
    val missingPermissionText = stringResource(R.string.os_shell_run_requires_permission)
    val emptyCommandText = stringResource(R.string.os_shell_run_empty_command)

    var commandInput by rememberSaveable { mutableStateOf("") }
    var outputText by rememberSaveable { mutableStateOf("") }
    var runningCommand by remember { mutableStateOf(false) }
    val outputScrollState = rememberScrollState()

    fun appendOutput(command: String, result: String) {
        val timestamp = SimpleDateFormat(
            "HH:mm:ss",
            Locale.getDefault()
        ).format(Date())
        val previousOutput = outputText
        outputText = buildString {
            if (previousOutput.isNotBlank()) {
                appendLine(previousOutput)
                appendLine()
            }
            appendLine("$ $command")
            appendLine(result)
            append("[$timestamp]")
        }
    }

    fun runCommand() {
        if (runningCommand) return
        val command = commandInput.trim()
        if (command.isBlank()) {
            outputText = emptyCommandText
            return
        }
        if (!canRunShellCommand) {
            outputText = missingPermissionText
            return
        }
        scope.launch {
            runningCommand = true
            try {
                val output = runCatching { onRunShellCommand(command) }
                    .getOrElse { throwable ->
                        throwable.localizedMessage?.takeIf { it.isNotBlank() }
                            ?: throwable.javaClass.simpleName
                    }
                    ?.takeIf { it.isNotBlank() }
                    ?: noOutputText
                appendOutput(command, output)
            } finally {
                runningCommand = false
            }
        }
    }

    LaunchedEffect(outputText) {
        if (outputText.isNotBlank()) {
            outputScrollState.scrollTo(outputScrollState.maxValue)
        }
    }

    AppPageScaffold(
        title = "",
        largeTitle = shellPageTitle,
        navigationIcon = {
            Icon(
                imageVector = MiuixIcons.Regular.Back,
                contentDescription = stringResource(R.string.common_close),
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.clickable { onClose() }
            )
        }
    ) { innerPadding ->
        AppPageLazyColumn(
            innerPadding = innerPadding,
            state = pageListState,
            topExtra = 0.dp,
            sectionSpacing = AppChromeTokens.pageSectionGap
        ) {
            item(key = "shell_input_card") {
                AppSurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    AppCardHeader(
                        title = inputTitle,
                        subtitle = shizukuStatus,
                        endActions = {
                            GlassIconButton(
                                backdrop = null,
                                icon = MiuixIcons.Regular.Play,
                                contentDescription = runActionDescription,
                                onClick = { runCommand() },
                                iconTint = if (runningCommand) {
                                    MiuixTheme.colorScheme.onBackgroundVariant
                                } else {
                                    MiuixTheme.colorScheme.primary
                                },
                                variant = GlassVariant.Bar
                            )
                            GlassIconButton(
                                backdrop = null,
                                icon = MiuixIcons.Regular.Lock,
                                contentDescription = requestPermissionDescription,
                                onClick = onRequestShizukuPermission,
                                iconTint = MiuixTheme.colorScheme.primary,
                                variant = GlassVariant.Bar
                            )
                            GlassIconButton(
                                backdrop = null,
                                icon = MiuixIcons.Regular.Replace,
                                contentDescription = clearInputDescription,
                                onClick = { commandInput = "" },
                                iconTint = MiuixTheme.colorScheme.onBackgroundVariant,
                                variant = GlassVariant.Bar
                            )
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .padding(bottom = 14.dp)
                    ) {
                        GlassSearchField(
                            value = commandInput,
                            onValueChange = { commandInput = it },
                            label = stringResource(R.string.os_shell_input_hint),
                            backdrop = null,
                            variant = GlassVariant.SheetInput,
                            singleLine = true,
                            onImeActionDone = { runCommand() },
                            textColor = MiuixTheme.colorScheme.primary,
                            minHeight = 54.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
            item(key = "shell_output_card") {
                AppSurfaceCard(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    AppCardHeader(
                        title = outputTitle,
                        subtitle = if (runningCommand) outputRunningSubtitle else outputReadySubtitle,
                        endActions = {
                            GlassIconButton(
                                backdrop = null,
                                icon = MiuixIcons.Regular.Replace,
                                contentDescription = clearOutputDescription,
                                onClick = { outputText = "" },
                                iconTint = MiuixTheme.colorScheme.onBackgroundVariant,
                                variant = GlassVariant.Bar
                            )
                        }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 360.dp)
                            .padding(horizontal = 14.dp)
                            .padding(bottom = 14.dp)
                            .background(
                                color = MiuixTheme.colorScheme.surface.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        if (outputText.isBlank()) {
                            Text(
                                text = outputHint,
                                color = MiuixTheme.colorScheme.onBackgroundVariant,
                                fontSize = AppTypographyTokens.Body.fontSize,
                                lineHeight = AppTypographyTokens.Body.lineHeight,
                            )
                        } else {
                            SelectionContainer(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(outputScrollState)
                            ) {
                                Text(
                                    text = outputText,
                                    color = MiuixTheme.colorScheme.onBackground,
                                    fontSize = AppTypographyTokens.Body.fontSize,
                                    lineHeight = AppTypographyTokens.Body.lineHeight,
                                    maxLines = Int.MAX_VALUE,
                                    overflow = TextOverflow.Clip
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
