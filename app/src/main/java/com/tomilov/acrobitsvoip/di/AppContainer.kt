package com.tomilov.acrobitsvoip.di

import android.app.Application
import com.tomilov.acrobitsvoip.BuildConfig
import com.tomilov.acrobitsvoip.core.voip.SoftphoneClient
import com.tomilov.acrobitsvoip.core.voip.VoipConfig
import com.tomilov.acrobitsvoip.softphone.acrobits.AcrobitsSoftphoneConfig
import com.tomilov.acrobitsvoip.softphone.acrobits.createAcrobitsSoftphoneClient

object AppContainer {
    private var initializedServices: AppServices? = null

    val services: AppServices
        get() = checkNotNull(initializedServices) {
            "AppContainer must be initialized from AcrobitsVoipApplication before use."
        }

    fun initialize(application: Application) {
        if (initializedServices != null) return
        initializedServices = AppServices(
            softphoneClient = createAcrobitsSoftphoneClient(
                application = application,
                config = AcrobitsSoftphoneConfig(
                    licenseKey = BuildConfig.ACROBITS_LICENSE_KEY,
                    sipHost = BuildConfig.SIP_HOST,
                    trafficLoggingEnabled = BuildConfig.DEBUG
                )
            ),
            voipConfig = VoipConfig(
                sipHost = BuildConfig.SIP_HOST,
                defaultSipUsername = BuildConfig.DEFAULT_SIP_USERNAME,
                defaultSipPassword = BuildConfig.DEFAULT_SIP_PASSWORD
            )
        )
    }
}

data class AppServices(
    val softphoneClient: SoftphoneClient,
    val voipConfig: VoipConfig
)
