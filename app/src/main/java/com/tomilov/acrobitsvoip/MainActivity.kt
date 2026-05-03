package com.tomilov.acrobitsvoip

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tomilov.acrobitsvoip.ui.CallScreen
import com.tomilov.acrobitsvoip.ui.DialerScreen
import com.tomilov.acrobitsvoip.ui.VoipViewModel
import com.tomilov.acrobitsvoip.ui.WelcomeScreen
import com.tomilov.acrobitsvoip.ui.theme.AcrobitsVoipTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AcrobitsVoipTheme {
                VoipApp()
            }
        }
    }
}

@Composable
private fun VoipApp(
    viewModel: VoipViewModel = viewModel()
) {
    val context = LocalContext.current
    var hasMicrophonePermission by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicrophonePermission = granted
        permissionRequested = true
    }

    LaunchedEffect(hasMicrophonePermission, permissionRequested) {
        if (!hasMicrophonePermission && !permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (!hasMicrophonePermission) {
        WelcomeScreen(
            permissionRequested = permissionRequested,
            onRequestMicrophonePermission = {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        )
        return
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeCall = uiState.activeCall
    if (activeCall == null) {
        DialerScreen(
            uiState = uiState,
            onSipUsernameChanged = viewModel::onSipUsernameChanged,
            onSipPasswordChanged = viewModel::onSipPasswordChanged,
            onRegister = viewModel::onRegisterClicked,
            onPhoneNumberChanged = viewModel::onPhoneNumberChanged,
            onDial = viewModel::onDialClicked,
            onMessageShown = viewModel::onMessageShown
        )
    } else {
        CallScreen(
            call = activeCall,
            duration = uiState.callDuration,
            message = uiState.message,
            onHangUp = viewModel::onHangUpClicked,
            onMuteChanged = viewModel::onMuteChanged,
            onHoldChanged = viewModel::onHoldChanged,
            onMessageShown = viewModel::onMessageShown
        )
    }
}
