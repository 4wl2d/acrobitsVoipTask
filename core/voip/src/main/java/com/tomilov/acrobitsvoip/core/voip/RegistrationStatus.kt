package com.tomilov.acrobitsvoip.core.voip

data class RegistrationStatus(
    val label: String,
    val isRegistered: Boolean
) {
    companion object {
        val NotConfigured = RegistrationStatus(
            label = "Not configured",
            isRegistered = false
        )
    }
}
