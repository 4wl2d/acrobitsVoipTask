package com.tomilov.acrobitsvoip.feature.calling

import com.tomilov.acrobitsvoip.core.voip.CallPlacementResult
import com.tomilov.acrobitsvoip.core.voip.CallSession
import com.tomilov.acrobitsvoip.core.voip.RegistrationStatus
import com.tomilov.acrobitsvoip.core.voip.SipCredentials
import com.tomilov.acrobitsvoip.core.voip.SoftphoneClient
import com.tomilov.acrobitsvoip.core.voip.VoipConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class CallingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `register with valid credentials configures account and shows message`() = runTest(
        mainDispatcherRule.dispatcher
    ) {
        val softphoneClient = FakeSoftphoneClient()
        val viewModel = createViewModel(softphoneClient)
        advanceUntilIdle()

        viewModel.onSipUsernameChanged(" 1000 ")
        viewModel.onSipPasswordChanged("password")
        viewModel.onRegisterClicked()

        assertEquals(
            SipCredentials(username = "1000", password = "password"),
            softphoneClient.configuredCredentials
        )
        assertEquals(
            "Registering 1000 on pbx.example.test.",
            viewModel.uiState.value.message
        )
    }

    @Test
    fun `dial before registered does not call softphone and shows message`() = runTest(
        mainDispatcherRule.dispatcher
    ) {
        val softphoneClient = FakeSoftphoneClient()
        val viewModel = createViewModel(softphoneClient)
        advanceUntilIdle()

        viewModel.onPhoneNumberChanged("2002")
        viewModel.onDialClicked()

        assertEquals(emptyList<String>(), softphoneClient.dialedNumbers)
        assertEquals(
            "Register SIP account before dialing.",
            viewModel.uiState.value.message
        )
    }

    @Test
    fun `registered dial normalizes formatted number and exposes active call`() = runTest(
        mainDispatcherRule.dispatcher
    ) {
        val call = CallSession(
            displayName = "Receiver",
            number = "+12345678900",
            stateLabel = "Trying",
            startedAtMillis = null,
            isMuted = false,
            isHeld = false
        )
        val softphoneClient = FakeSoftphoneClient(
            dialResult = CallPlacementResult.Success(call)
        )
        val viewModel = createViewModel(softphoneClient)
        advanceUntilIdle()

        softphoneClient.registrationStatus.value = RegistrationStatus(
            label = "Registered",
            isRegistered = true
        )
        advanceUntilIdle()

        viewModel.onPhoneNumberChanged("+1 (234) 567-8900")
        viewModel.onDialClicked()

        assertEquals(listOf("+12345678900"), softphoneClient.dialedNumbers)
        assertEquals(call, viewModel.uiState.value.activeCall)
    }

    private fun createViewModel(
        softphoneClient: SoftphoneClient,
        voipConfig: VoipConfig = VoipConfig(
            sipHost = "pbx.example.test",
            defaultSipUsername = "",
            defaultSipPassword = ""
        )
    ): CallingViewModel = CallingViewModel(
        softphoneClient = softphoneClient,
        voipConfig = voipConfig
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeSoftphoneClient(
    private val dialResult: CallPlacementResult = CallPlacementResult.Failure("Not configured.")
) : SoftphoneClient {
    override val registrationStatus = MutableStateFlow(RegistrationStatus.NotConfigured)
    override val activeCall: StateFlow<CallSession?> = MutableStateFlow(null)

    var configuredCredentials: SipCredentials? = null
        private set
    private val mutableDialedNumbers = mutableListOf<String>()
    val dialedNumbers: List<String>
        get() = mutableDialedNumbers.toList()

    override fun configureAccount(credentials: SipCredentials) {
        configuredCredentials = credentials
    }

    override fun dial(number: String): CallPlacementResult {
        mutableDialedNumbers += number
        return dialResult
    }

    override fun hangUp() = Unit

    override fun setMuted(muted: Boolean) = Unit

    override fun setHeld(held: Boolean) = Unit
}
