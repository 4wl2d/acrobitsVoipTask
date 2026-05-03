package com.tomilov.acrobitsvoip

import android.app.Application
import com.tomilov.acrobitsvoip.di.AppContainer

class AcrobitsVoipApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
    }
}
