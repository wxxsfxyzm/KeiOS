package com.example.keios.ui.page.main.student.section.gallery

import androidx.activity.BackEventCompat
import androidx.activity.ExperimentalActivityApi
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.keios.ui.page.main.student.IMAGE_BACK_GESTURE_CONTENT_FADE_FACTOR
import com.example.keios.ui.page.main.student.IMAGE_BACK_GESTURE_SCRIM_FADE_FACTOR
import com.example.keios.ui.page.main.student.IMAGE_BACK_GESTURE_TRANSLATION_FACTOR
import kotlinx.coroutines.CancellationException

internal data class GuideFullscreenBackGestureState(
    val translationX: Float,
    val contentAlpha: Float,
    val scrimAlpha: Float,
    val onDialogWidthChanged: (Int) -> Unit
)

@OptIn(ExperimentalActivityApi::class)
@Composable
internal fun rememberGuideFullscreenBackGestureState(
    onDismiss: () -> Unit
): GuideFullscreenBackGestureState {
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    var predictiveBackSwipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_NONE) }
    var dialogWidthPx by remember { mutableIntStateOf(0) }

    BackHandler(enabled = true) {
        onDismiss()
    }
    PredictiveBackHandler(enabled = true) { backEvents ->
        var dismissedByPredictiveProgress = false
        try {
            backEvents.collect { event ->
                predictiveBackProgress = event.progress.coerceIn(0f, 1f)
                predictiveBackSwipeEdge = event.swipeEdge
                if (event.progress >= 0.995f) {
                    dismissedByPredictiveProgress = true
                    onDismiss()
                }
            }
            if (!dismissedByPredictiveProgress) {
                onDismiss()
            }
        } catch (_: CancellationException) {
        } finally {
            predictiveBackProgress = 0f
            predictiveBackSwipeEdge = BackEventCompat.EDGE_NONE
        }
    }

    val clampedBackProgress = predictiveBackProgress.coerceIn(0f, 1f)
    val easedBackProgress = clampedBackProgress * clampedBackProgress * (3f - 2f * clampedBackProgress)
    val backEdgeDirection = when (predictiveBackSwipeEdge) {
        BackEventCompat.EDGE_LEFT -> 1f
        BackEventCompat.EDGE_RIGHT -> -1f
        else -> 0f
    }
    val backTranslationX = dialogWidthPx.toFloat() *
        IMAGE_BACK_GESTURE_TRANSLATION_FACTOR *
        backEdgeDirection *
        easedBackProgress
    val backContentAlpha =
        (1f - easedBackProgress * IMAGE_BACK_GESTURE_CONTENT_FADE_FACTOR).coerceIn(0f, 1f)
    val backScrimAlpha =
        (1f - easedBackProgress * IMAGE_BACK_GESTURE_SCRIM_FADE_FACTOR).coerceIn(0f, 1f)

    return GuideFullscreenBackGestureState(
        translationX = backTranslationX,
        contentAlpha = backContentAlpha,
        scrimAlpha = backScrimAlpha,
        onDialogWidthChanged = { width -> dialogWidthPx = width }
    )
}
