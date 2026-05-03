package com.tomilov.acrobitsvoip.feature.calling.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tomilov.acrobitsvoip.core.voip.CallSession
import com.tomilov.acrobitsvoip.feature.calling.CallingTestTags

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
                    modifier = Modifier.testTag(CallingTestTags.MuteButton),
                    label = { Text(if (call.isMuted) "Muted" else "Mute") }
                )
                FilterChip(
                    selected = call.isHeld,
                    onClick = { onHoldChanged(!call.isHeld) },
                    modifier = Modifier.testTag(CallingTestTags.HoldButton),
                    label = { Text(if (call.isHeld) "On hold" else "Hold") }
                )
            }
            Button(
                onClick = onHangUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(CallingTestTags.HangUpButton),
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
