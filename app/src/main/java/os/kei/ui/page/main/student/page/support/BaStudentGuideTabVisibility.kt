package os.kei.ui.page.main.student.page.support

import os.kei.ui.page.main.student.BaGuideVoiceEntry
import os.kei.ui.page.main.student.BaStudentGuideInfo
import os.kei.ui.page.main.student.GuideBottomTab
import os.kei.ui.page.main.student.fetch.normalizeGuideUrl
import os.kei.ui.page.main.student.profileRowsForDisplay
import os.kei.ui.page.main.student.simulateRowsForDisplay
import os.kei.ui.page.main.student.skillCardsForDisplay
import os.kei.ui.page.main.student.tabcontent.buildGrowthTitleVoiceEntries
import os.kei.ui.page.main.student.tabcontent.isGrowthTitleVoiceRow
import os.kei.ui.page.main.student.tabcontent.simulate.buildGuideSimulateData
import os.kei.ui.page.main.student.weaponCardForDisplay

internal fun resolveGuideBottomTabs(info: BaStudentGuideInfo?): List<GuideBottomTab> {
    val allTabs = GuideBottomTab.entries.toList()
    val guide = info ?: return allTabs
    val visibleTabs = allTabs.filter { tab ->
        when (tab) {
            GuideBottomTab.Skills -> hasSkillsTabData(guide)
            GuideBottomTab.Voice -> hasVoiceTabData(guide)
            GuideBottomTab.Simulate -> hasSimulateTabData(guide)
            else -> true
        }
    }
    return if (visibleTabs.isNotEmpty()) {
        visibleTabs
    } else {
        listOf(GuideBottomTab.Archive)
    }
}

private fun hasSkillsTabData(guide: BaStudentGuideInfo): Boolean {
    return guide.skillCardsForDisplay().isNotEmpty() || guide.weaponCardForDisplay() != null
}

private fun hasVoiceTabData(guide: BaStudentGuideInfo): Boolean {
    return buildVisibleVoiceEntries(guide).isNotEmpty()
}

private fun buildVisibleVoiceEntries(guide: BaStudentGuideInfo): List<BaGuideVoiceEntry> {
    val structuredVoiceEntries = guide.voiceEntries.filter { entry ->
        entry.lines.any { line -> line.trim().isNotBlank() }
    }
    val migratedVoiceEntries = buildGrowthTitleVoiceEntries(
        guide.profileRowsForDisplay()
            .filter(::isGrowthTitleVoiceRow)
    )
    return (structuredVoiceEntries + migratedVoiceEntries)
        .distinctBy { entry ->
            listOf(
                entry.section.trim(),
                entry.title.trim(),
                entry.lineHeaders.joinToString("|") { it.trim() },
                entry.lines.joinToString("|") { it.trim() },
                entry.audioUrls.joinToString("|") { normalizeGuideUrl(it) },
                normalizeGuideUrl(entry.audioUrl)
            ).joinToString("|")
        }
}

private fun hasSimulateTabData(guide: BaStudentGuideInfo): Boolean {
    val simulateData = buildGuideSimulateData(guide.simulateRowsForDisplay())
    return simulateData.initialRows.isNotEmpty() ||
        simulateData.maxRows.isNotEmpty() ||
        simulateData.weaponRows.isNotEmpty() ||
        simulateData.equipmentRows.isNotEmpty() ||
        simulateData.favorRows.isNotEmpty() ||
        simulateData.unlockRows.isNotEmpty() ||
        simulateData.bondRows.isNotEmpty()
}
