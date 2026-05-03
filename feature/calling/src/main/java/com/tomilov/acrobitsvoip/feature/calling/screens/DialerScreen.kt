package com.tomilov.acrobitsvoip.feature.calling.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tomilov.acrobitsvoip.feature.calling.CallingUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialerScreen(
    uiState: CallingUiState,
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
