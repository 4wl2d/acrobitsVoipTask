package com.tomilov.acrobitsvoip.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomilov.acrobitsvoip.BuildConfig
import com.tomilov.acrobitsvoip.di.AppContainer
import com.tomilov.acrobitsvoip.dialer.PhoneNumberValidation
import com.tomilov.acrobitsvoip.dialer.PhoneNumberValidator
import com.tomilov.acrobitsvoip.voip.CallPlacementResult
import com.tomilov.acrobitsvoip.voip.CallSession
import com.tomilov.acrobitsvoip.voip.SipCredentials
import com.tomilov.acrobitsvoip.voip.SoftphoneClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VoipViewModel(
    private val softphoneClient: SoftphoneClient = AppContainer.services.softphoneClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VoipUiState(
            sipUsername = BuildConfig.DEFAULT_SIP_USERNAME,
            sipPassword = BuildConfig.DEFAULT_SIP_PASSWORD
        )
    )
    val uiState: StateFlow<VoipUiState> = _uiState.asStateFlow()

    private var callTimerJob: Job? = null

    init {
        viewModelScope.launch {
            softphoneClient.registrationStatus.collect { status ->
                _uiState.update {
                    it.copy(
                        registrationLabel = status.label,
                        isRegistered = status.isRegistered
                    )
                }
            }
        }

        viewModelScope.launch {
            softphoneClient.activeCall.collect(::onActiveCallChanged)
        }
    }

    fun onSipUsernameChanged(value: String) {
        _uiState.update { it.copy(sipUsername = value.trim()) }
    }

    fun onSipPasswordChanged(value: String) {
        _uiState.update { it.copy(sipPassword = value) }
    }

    fun onRegisterClicked() {
        val current = _uiState.value
        if (!current.canRegister) {
            _uiState.update { it.copy(message = "Enter SIP username and password.") }
            return
        }

        runCatching {
            softphoneClient.configureAccount(
                SipCredentials(
                    username = current.sipUsername,
                    password = current.sipPassword
                )
            )
        }.onSuccess {
            _uiState.update { it.copy(message = "Registering ${current.sipUsername} on ${BuildConfig.SIP_HOST}.") }
        }.onFailure { error ->
            _uiState.update { it.copy(message = error.message ?: "SIP registration failed.") }
        }
    }

    fun onPhoneNumberChanged(value: String) {
        val validation = PhoneNumberValidator.validate(value)
        _uiState.update {
            it.copy(
                phoneNumber = value,
                phoneNumberError = (validation as? PhoneNumberValidation.Invalid)?.message
            )
        }
    }

    fun onDialClicked() {
        when (val validation = PhoneNumberValidator.validate(_uiState.value.phoneNumber)) {
            is PhoneNumberValidation.Invalid -> _uiState.update {
                it.copy(phoneNumberError = validation.message)
            }

            is PhoneNumberValidation.Valid -> placeCall(validation.normalizedNumber)
        }
    }

    fun onHangUpClicked() {
        softphoneClient.hangUp()
    }

    fun onMuteChanged(muted: Boolean) {
        softphoneClient.setMuted(muted)
    }

    fun onHoldChanged(held: Boolean) {
        softphoneClient.setHeld(held)
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private fun placeCall(number: String) {
        when (val result = softphoneClient.dial(number)) {
            is CallPlacementResult.Success -> _uiState.update {
                it.copy(activeCall = result.call)
            }

            is CallPlacementResult.Failure -> _uiState.update {
                it.copy(message = result.message)
            }
        }
    }

    private fun onActiveCallChanged(call: CallSession?) {
        _uiState.update {
            it.copy(
                activeCall = call,
                callDuration = formatCallDuration(call)
            )
        }

        callTimerJob?.cancel()
        if (call?.startedAtMillis != null) {
            callTimerJob = viewModelScope.launch {
                while (true) {
                    _uiState.update { it.copy(callDuration = formatCallDuration(call)) }
                    delay(1_000)
                }
            }
        }
    }

    private fun formatCallDuration(call: CallSession?): String {
        val startedAtMillis = call?.startedAtMillis ?: return "00:00"
        val elapsedSeconds = (System.currentTimeMillis() - startedAtMillis) / 1_000
        return CallDurationFormatter.format(elapsedSeconds)
    }
}
