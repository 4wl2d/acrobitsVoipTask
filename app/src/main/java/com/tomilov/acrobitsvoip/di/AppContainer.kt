package com.tomilov.acrobitsvoip.di

import android.app.Application
import com.tomilov.acrobitsvoip.voip.AcrobitsSoftphoneClient
import com.tomilov.acrobitsvoip.voip.SoftphoneClient

object AppContainer {
    private var initializedServices: AppServices? = null

    val services: AppServices
        get() = checkNotNull(initializedServices) {
            "AppContainer must be initialized from AcrobitsVoipApplication before use."
        }

    fun initialize(application: Application) {
        if (initializedServices != null) return
        initializedServices = AppServices(
            softphoneClient = AcrobitsSoftphoneClient(application)
        )
    }
}

data class AppServices(
    val softphoneClient: SoftphoneClient
)
