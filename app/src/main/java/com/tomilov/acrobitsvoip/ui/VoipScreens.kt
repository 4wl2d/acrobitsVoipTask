package com.tomilov.acrobitsvoip.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tomilov.acrobitsvoip.ui.theme.AcrobitsVoipTheme
import com.tomilov.acrobitsvoip.voip.CallSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    permissionRequested: Boolean,
    onRequestMicrophonePermission: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Acrobits VoIP") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Microphone access is required before the app can register and place VoIP calls.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRequestMicrophonePermission) {
                Text(
                    if (permissionRequested) {
                        "Grant microphone permission"
                    } else {
                        "Continue"
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialerScreen(
    uiState: VoipUiState,
    onSipUsernameChanged: (String) -> Unit,
    onSipPasswordChanged: (String) -> Unit,
    onRegister: () -> Unit,
    onPhoneNumberChanged: (String) -> Unit,
    onDial: () -> Unit,
    onMessageShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dialer") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            RegistrationStatusCard(
                registrationLabel = uiState.registrationLabel,
                isRegistered = uiState.isRegistered
            )
            AccountCard(
                sipUsername = uiState.sipUsername,
                sipPassword = uiState.sipPassword,
                canRegister = uiState.canRegister,
                onSipUsernameChanged = onSipUsernameChanged,
                onSipPasswordChanged = onSipPasswordChanged,
                onRegister = onRegister
            )
            DialCard(
                phoneNumber = uiState.phoneNumber,
                phoneNumberError = uiState.phoneNumberError,
                canDial = uiState.canDial,
                onPhoneNumberChanged = onPhoneNumberChanged,
                onDial = onDial
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    call: CallSession,
    duration: String,
    message: String?,
    onHangUp: () -> Unit,
    onMuteChanged: (Boolean) -> Unit,
    onHoldChanged: (Boolean) -> Unit,
    onMessageShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    message?.let { userMessage ->
        LaunchedEffect(userMessage) {
            snackbarHostState.showSnackbar(userMessage)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Outgoing call") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = call.displayName.ifBlank { call.number },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = call.number.ifBlank { "Unknown number" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            CallInfoCard(
                stateLabel = call.stateLabel,
                duration = duration
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                FilterChip(
                    selected = call.isMuted,
                    onClick = { onMuteChanged(!call.isMuted) },
                    label = { Text(if (call.isMuted) "Muted" else "Mute") }
                )
                FilterChip(
                    selected = call.isHeld,
                    onClick = { onHoldChanged(!call.isHeld) },
                    label = { Text(if (call.isHeld) "On hold" else "Hold") }
                )
            }
            Button(
                onClick = onHangUp,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Hang up")
            }
        }
    }
}

@Composable
private fun RegistrationStatusCard(
    registrationLabel: String,
    isRegistered: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SIP registration",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = registrationLabel,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = if (isRegistered) {
                    "Ready for outgoing calls."
                } else {
                    "Register a SIP account before dialing."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccountCard(
    sipUsername: String,
    sipPassword: String,
    canRegister: Boolean,
    onSipUsernameChanged: (String) -> Unit,
    onSipPasswordChanged: (String) -> Unit,
    onRegister: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SIP account",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = sipUsername,
                onValueChange = onSipUsernameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = sipPassword,
                onValueChange = onSipPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            OutlinedButton(
                onClick = onRegister,
                enabled = canRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }
        }
    }
}

@Composable
private fun DialCard(
    phoneNumber: String,
    phoneNumberError: String?,
    canDial: Boolean,
    onPhoneNumberChanged: (String) -> Unit,
    onDial: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Telephone number") },
                singleLine = true,
                isError = phoneNumberError != null,
                supportingText = {
                    Text(phoneNumberError ?: "Enter the extension or number to call.")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Button(
                onClick = onDial,
                enabled = canDial,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dial")
            }
        }
    }
}

@Composable
private fun CallInfoCard(
    stateLabel: String,
    duration: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Call state",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stateLabel,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Duration: $duration",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DialerScreenPreview() {
    AcrobitsVoipTheme {
        DialerScreen(
            uiState = VoipUiState(
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
