package com.tomilov.acrobitsvoip.ui

import com.tomilov.acrobitsvoip.voip.CallSession

data class VoipUiState(
    val sipUsername: String = "",
    val sipPassword: String = "",
    val registrationLabel: String = "Not configured",
    val isRegistered: Boolean = false,
    val phoneNumber: String = "",
    val phoneNumberError: String? = null,
    val activeCall: CallSession? = null,
    val callDuration: String = "00:00",
    val message: String? = null
) {
    val canRegister: Boolean
        get() = sipUsername.isNotBlank() && sipPassword.isNotBlank()

    val canDial: Boolean
        get() = isRegistered &&
            activeCall == null &&
            phoneNumber.isNotBlank() &&
            phoneNumberError == null
}
