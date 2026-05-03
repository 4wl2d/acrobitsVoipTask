package com.tomilov.acrobitsvoip.softphone.acrobits

data class AcrobitsSoftphoneConfig(
    val licenseKey: String,
    val sipHost: String,
    val trafficLoggingEnabled: Boolean
)
