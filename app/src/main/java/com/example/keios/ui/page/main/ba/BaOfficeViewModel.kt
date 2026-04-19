package com.example.keios.ui.page.main.ba

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.keios.ui.page.main.ba.support.BASettingsStore
import com.example.keios.ui.page.main.ba.support.BaPageSnapshot

internal class BaOfficeViewModel(
    application: Application
) : AndroidViewModel(application) {
    val initialSnapshot: BaPageSnapshot = BASettingsStore.loadSnapshot()
    val office: BaOfficeController = BaOfficeController(initialSnapshot)
}
