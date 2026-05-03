package com.tomilov.acrobitsvoip.feature.calling.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.tomilov.acrobitsvoip.core.designsystem.theme.AcrobitsVoipTheme
import com.tomilov.acrobitsvoip.core.voip.CallSession
import com.tomilov.acrobitsvoip.feature.calling.CallingUiState

@Preview(showBackground = true)
@Composable
private fun DialerScreenPreview() {
    AcrobitsVoipTheme {
        DialerScreen(
            uiState = CallingUiState(
                sipUsername = "1000",
                registrationLabel = "Registered",
                isRegistered = true,
                phoneNumber = "1001"
            ),
            onSipUsernameChanged = {},
            onSipPasswordChanged = {},
            onRegister = {},
            onPhoneNumberChanged = {},
            onDial = {},
            onMessageShown = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CallScreenPreview() {
    AcrobitsVoipTheme {
        CallScreen(
            call = CallSession(
                displayName = "CloudSoftphone",
                number = "1001",
                stateLabel = "Established",
                startedAtMillis = null,
                isMuted = false,
                isHeld = false
            ),
            duration = "01:12",
            message = null,
            onHangUp = {},
            onMuteChanged = {},
            onHoldChanged = {},
            onMessageShown = {}
        )
    }
}
