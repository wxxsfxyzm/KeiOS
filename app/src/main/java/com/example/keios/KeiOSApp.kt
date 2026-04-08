package com.example.keios

import android.app.Application
import com.tencent.mmkv.MMKV

class KeiOSApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }
}
