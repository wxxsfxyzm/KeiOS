package com.example.keios.ui.page.main

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keios.ui.page.main.student.BaGuideGalleryItem
import com.example.keios.ui.page.main.student.BaStudentGuideStore
import com.example.keios.ui.page.main.student.GuideBottomTab
import com.example.keios.ui.page.main.student.GuideTab
import com.example.keios.ui.page.main.student.GuideCombatMetaTile
import com.example.keios.ui.page.main.student.GuideGallerySection
import com.example.keios.ui.page.main.student.GuideProfileMetaLine
import com.example.keios.ui.page.main.student.GuideRemoteIcon
import com.example.keios.ui.page.main.student.GuideRemoteImage
import com.example.keios.ui.page.main.student.GuideRowsSection
import com.example.keios.ui.page.main.student.bottomTabIconUrl
import com.example.keios.ui.page.main.student.buildCombatMetaItems
import com.example.keios.ui.page.main.student.buildProfileMetaItems
import com.example.keios.ui.page.main.student.fetchGuideInfo
import com.example.keios.ui.page.main.student.growthRowsForDisplay
import com.example.keios.ui.page.main.student.normalizeGuideUrl
import com.example.keios.ui.page.main.student.profileRowsForDisplay
import com.example.keios.ui.page.main.student.shouldHideMovedHeaderRow
import com.example.keios.ui.page.main.student.showLoadingText
import com.example.keios.ui.page.main.student.skillRowsForDisplay
import com.example.keios.ui.page.main.widget.FloatingBottomBar
import com.example.keios.ui.page.main.widget.FloatingBottomBarItem
import com.example.keios.ui.page.main.widget.FrostedBlock
import com.example.keios.ui.page.main.widget.MiuixInfoItem
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BaStudentGuidePage(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accent = MiuixTheme.colorScheme.primary
    val surfaceColor = MiuixTheme.colorScheme.surface
    val backdrop: LayerBackdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    val scrollBehavior = MiuixScrollBehavior()

    val sourceUrl = remember { BaStudentGuideStore.loadCurrentUrl() }
    var info by remember(sourceUrl) { mutableStateOf(BaStudentGuideStore.loadInfo(sourceUrl)) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshSignal by remember { mutableStateOf(0) }
    var selectedBottomTabIndex by rememberSaveable(sourceUrl) { mutableIntStateOf(0) }
    val bottomTabs = GuideBottomTab.entries
    val activeBottomTab = bottomTabs.getOrElse(selectedBottomTabIndex) { GuideBottomTab.Archive }
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val pageTitle = info?.title?.ifBlank { "学生图鉴" } ?: "学生图鉴"

    fun openExternal(url: String) {
        val target = normalizeGuideUrl(url)
        if (target.isBlank()) return
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(sourceUrl, refreshSignal) {
        if (sourceUrl.isBlank()) return@LaunchedEffect
        val cached = BaStudentGuideStore.loadInfo(sourceUrl)
        if (cached != null) info = cached
        loading = true
        val result = withContext(Dispatchers.IO) {
            runCatching { fetchGuideInfo(sourceUrl) }
        }
        result.onSuccess { latest ->
            info = latest
            error = null
            withContext(Dispatchers.IO) { BaStudentGuideStore.saveInfo(latest) }
        }.onFailure {
            error = if (info != null) "网络请求失败，已显示本地缓存" else "图鉴信息加载失败"
        }
        loading = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = pageTitle,
                scrollBehavior = scrollBehavior,
                color = MiuixTheme.colorScheme.surface,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Regular.Back,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            refreshSignal += 1
                        }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Regular.Refresh,
                            contentDescription = "刷新",
                            tint = MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                FloatingBottomBar(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .padding(
                            horizontal = 12.dp,
                            vertical = 12.dp + navigationBarBottom
                        ),
                    selectedIndex = { selectedBottomTabIndex },
                    onSelected = { index -> selectedBottomTabIndex = index },
                    backdrop = backdrop,
                    tabsCount = bottomTabs.size,
                    isBlurEnabled = true
                ) {
                    bottomTabs.forEachIndexed { index, tab ->
                        val useDynamicIcon = tab != GuideBottomTab.Archive
                        val dynamicIconUrl = if (useDynamicIcon) info?.bottomTabIconUrl(tab).orEmpty() else ""
                        FloatingBottomBarItem(
                            onClick = { selectedBottomTabIndex = index },
                            modifier = Modifier.defaultMinSize(minWidth = 72.dp)
                        ) {
                            if (dynamicIconUrl.isNotBlank()) {
                                GuideRemoteIcon(
                                    imageUrl = dynamicIconUrl,
                                    iconWidth = 18.dp,
                                    iconHeight = 18.dp
                                )
                            } else {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = MiuixTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .graphicsLayer {
                                            scaleX = 1f
                                            scaleY = 1f
                                        }
                                )
                            }
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                color = MiuixTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            item { SmallTitle(activeBottomTab.label) }
            item { Spacer(modifier = Modifier.height(14.dp)) }

            if (sourceUrl.isBlank()) {
                item {
                    FrostedBlock(
                        backdrop = backdrop,
                        title = "未选择学生",
                        subtitle = "请从 BA 卡池信息中点击对应卡池进入",
                        accent = accent
                    )
                }
            } else {
                when (activeBottomTab) {
                    GuideBottomTab.Archive -> {
                        item {
                            val guide = info
                            val profileItems = guide?.buildProfileMetaItems().orEmpty()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.defaultColors(
                                    color = Color(0x223B82F6),
                                    contentColor = MiuixTheme.colorScheme.onBackground
                                ),
                                onClick = {}
                            ) {
                                if (showLoadingText(loading = loading, hasInfo = guide != null)) {
                                    Text(
                                        text = "同步中...",
                                        color = MiuixTheme.colorScheme.onBackgroundVariant,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                    )
                                }
                                if (guide != null) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(modifier = Modifier.width(112.dp)) {
                                                if (guide.imageUrl.isNotBlank()) {
                                                    GuideRemoteImage(
                                                        imageUrl = guide.imageUrl,
                                                        imageHeight = 152.dp
                                                    )
                                                } else {
                                                    Text(
                                                        text = "暂无图片",
                                                        color = MiuixTheme.colorScheme.onBackgroundVariant
                                                    )
                                                }
                                            }
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(152.dp),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                profileItems.forEach { item ->
                                                    GuideProfileMetaLine(item)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(10.dp)) }
                        item {
                            val guide = info
                            val combatItems = guide?.buildCombatMetaItems().orEmpty()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.defaultColors(
                                    color = Color(0x223B82F6),
                                    contentColor = MiuixTheme.colorScheme.onBackground
                                ),
                                onClick = {}
                            ) {
                                if (showLoadingText(loading = loading, hasInfo = guide != null)) {
                                    Text(
                                        text = "同步中...",
                                        color = MiuixTheme.colorScheme.onBackgroundVariant,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                    )
                                }
                                if (guide != null) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        combatItems.forEach { item ->
                                            GuideCombatMetaTile(
                                                item = item,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        item {
                            FrostedBlock(
                                backdrop = backdrop,
                                title = activeBottomTab.label,
                                subtitle = info?.subtitle?.ifBlank { "GameKee" } ?: "GameKee",
                                accent = accent,
                                content = {
                                    if (showLoadingText(loading = loading, hasInfo = info != null)) {
                                        Text("同步中...", color = MiuixTheme.colorScheme.onBackgroundVariant)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    error?.takeIf { it.isNotBlank() }?.let {
                                        Text(
                                            text = it,
                                            color = MiuixTheme.colorScheme.error,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    val guide = info
                                    if (guide != null) {
                                        val skillRows = guide.skillRowsForDisplay()
                                        val profileRows = guide.profileRowsForDisplay()
                                        val visibleSkillRows = skillRows.filterNot { shouldHideMovedHeaderRow(it) }
                                        val visibleProfileRows = profileRows.filterNot { shouldHideMovedHeaderRow(it) }
                                        val growthRows = guide.growthRowsForDisplay()
                                        val galleryItems = if (guide.galleryItems.isNotEmpty()) {
                                            guide.galleryItems
                                        } else {
                                            listOfNotNull(
                                                guide.imageUrl
                                                    .takeIf { it.isNotBlank() }
                                                    ?.let { BaGuideGalleryItem("立绘", it) }
                                            )
                                        }
                                        val activeGuideTab = activeBottomTab.guideTab

                                        Text(
                                            text = guide.summary.ifBlank { guide.description },
                                            color = MiuixTheme.colorScheme.onBackground
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        when (activeGuideTab) {
                                            GuideTab.Skills -> {
                                                GuideRowsSection(
                                                    rows = visibleSkillRows,
                                                    emptyText = "暂未解析到角色技能数据。"
                                                )
                                            }

                                            GuideTab.Profile -> {
                                                GuideRowsSection(
                                                    rows = visibleProfileRows,
                                                    emptyText = "暂未解析到学生档案数据。"
                                                )
                                            }

                                            GuideTab.Voice -> {
                                                GuideRowsSection(
                                                    rows = guide.voiceRows,
                                                    emptyText = "语音台词解析中，当前版本先完善其他栏目。"
                                                )
                                            }

                                            GuideTab.Gallery -> {
                                                GuideGallerySection(
                                                    items = galleryItems,
                                                    emptyText = "暂未解析到影画鉴赏内容。"
                                                )
                                            }

                                            GuideTab.Simulate -> {
                                                GuideRowsSection(
                                                    rows = growthRows,
                                                    emptyText = "暂未解析到养成模拟数据。"
                                                )
                                            }

                                            else -> {}
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        MiuixInfoItem(
                                            "来源",
                                            guide.sourceUrl,
                                            onClick = { openExternal(guide.sourceUrl) }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

