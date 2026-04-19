package com.example.keios.ui.page.main

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.composables.icons.lucide.R as LucideR

@Composable
internal fun osLucideConsoleIcon(): ImageVector = osLucideVector(LucideR.drawable.lucide_ic_square_terminal)

@Composable
internal fun osLucideShellIcon(): ImageVector = osLucideVector(LucideR.drawable.lucide_ic_terminal)

@Composable
internal fun osLucideCardIcon(): ImageVector = osLucideVector(LucideR.drawable.lucide_ic_square_stack)

@Composable
internal fun osLucideEnterIcon(): ImageVector = osLucideVector(LucideR.drawable.lucide_ic_log_in)

@Composable
internal fun osLucideRunIcon(): ImageVector = osLucideVector(LucideR.drawable.lucide_ic_play)

@Composable
internal fun osLucideStopIcon(): ImageVector = osLucideVector(LucideR.drawable.lucide_ic_square_stop)

@Composable
internal fun osLucideSaveIcon(): ImageVector = osLucideVector(LucideR.drawable.lucide_ic_save)

@Composable
internal fun osLucideFormatIcon(): ImageVector = osLucideVector(LucideR.drawable.lucide_ic_wand_sparkles)

@Composable
internal fun osLucideClearIcon(): ImageVector = osLucideVector(LucideR.drawable.lucide_ic_eraser)

@Composable
internal fun osLucideClearAllIcon(): ImageVector = osLucideVector(LucideR.drawable.lucide_ic_trash_2)

@Composable
internal fun osLucideSettingsIcon(): ImageVector = osLucideVector(LucideR.drawable.lucide_ic_settings_2)

@Composable
internal fun osLucideCopyIcon(): ImageVector = osLucideVector(LucideR.drawable.lucide_ic_copy)

@Composable
private fun osLucideVector(@DrawableRes drawableRes: Int): ImageVector = ImageVector.vectorResource(drawableRes)
