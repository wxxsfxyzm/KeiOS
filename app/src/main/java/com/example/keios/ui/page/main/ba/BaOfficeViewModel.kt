package com.example.keios.ui.page.main.ba

import android.app.Application
import androidx.lifecycle.AndroidViewModel

internal class BaOfficeViewModel(
    application: Application
) : AndroidViewModel(application) {
    val initialSnapshot: BaPageSnapshot = BASettingsStore.loadSnapshot()
    val office: BaOfficeController = BaOfficeController(initialSnapshot)
}
