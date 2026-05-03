package com.tomilov.acrobitsvoip.feature.calling

import com.tomilov.acrobitsvoip.core.voip.CallSession

data class CallingUiState(
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
