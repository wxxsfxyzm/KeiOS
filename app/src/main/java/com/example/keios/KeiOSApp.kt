package com.example.keios

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import com.example.keios.core.background.AppBackgroundScheduler
import com.tencent.mmkv.MMKV

class KeiOSApp : Application() {
    companion object {
        @Volatile
        private lateinit var instance: KeiOSApp

        val appContext: Application
            get() = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add(AnimatedImageDecoder.Factory())
                }
                .build()
        }
        MMKV.initialize(this)
        AppBackgroundScheduler.scheduleAll(this)
    }
}
