package com.example.keios.ui.page.main.host.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.example.keios.ui.navigation.KeiosRoute

@Composable
internal fun rememberMainScreenSettingsReturnToken(
    backStack: List<NavKey>
): Int {
    var token by rememberSaveable { mutableIntStateOf(0) }
    var previousTopRoute by remember { mutableStateOf<NavKey?>(null) }
    LaunchedEffect(backStack.lastOrNull()) {
        val currentTopRoute = backStack.lastOrNull()
        if (previousTopRoute == KeiosRoute.Settings && currentTopRoute == KeiosRoute.Main) {
            token++
        }
        previousTopRoute = currentTopRoute
    }
    return token
}

@Composable
internal fun BindMainScreenRequestedBottomPageEffect(
    requestedBottomPageToken: Int,
    requestedBottomPage: String?,
    onReturnToMain: () -> Unit
) {
    LaunchedEffect(requestedBottomPageToken, requestedBottomPage) {
        if (requestedBottomPage.isNullOrBlank()) return@LaunchedEffect
        onReturnToMain()
    }
}
