package com.amjad.mawaqeeti

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MawaqeetiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        com.amjad.mawaqeeti.widget.WidgetUpdateWorker.enqueuePeriodicWork(this)
    }
}
