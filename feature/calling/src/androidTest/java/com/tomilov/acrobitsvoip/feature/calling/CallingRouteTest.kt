package com.tomilov.acrobitsvoip.feature.calling

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tomilov.acrobitsvoip.core.voip.CallPlacementResult
import com.tomilov.acrobitsvoip.core.voip.CallSession
import com.tomilov.acrobitsvoip.core.voip.RegistrationStatus
import com.tomilov.acrobitsvoip.core.voip.SipCredentials
import com.tomilov.acrobitsvoip.core.voip.SoftphoneClient
import com.tomilov.acrobitsvoip.core.voip.VoipConfig
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallingRouteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun routeContentRegistersDialsShowsCallAndForwardsCallControls() {
        val softphoneClient = FakeRouteSoftphoneClient()
        composeRule.setContent {
            CallingRouteContent(
                softphoneClient = softphoneClient,
                voipConfig = VoipConfig(
                    sipHost = "pbx.example.test",
                    defaultSipUsername = "",
                    defaultSipPassword = ""
                )
            )
        }

        composeRule
            .onNodeWithTag(CallingTestTags.SipUsernameInput)
            .performTextInput("1000")
        composeRule
            .onNodeWithTag(CallingTestTags.SipPasswordInput)
            .performTextInput("secret")
        composeRule
            .onNodeWithTag(CallingTestTags.RegisterButton)
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(
                SipCredentials(username = "1000", password = "secret"),
                softphoneClient.configuredCredentials
            )
            softphoneClient.registrationStatus.value = RegistrationStatus(
                label = "Registered",
                isRegistered = true
            )
        }

        composeRule
            .onNodeWithText("Ready for outgoing calls.")
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(CallingTestTags.PhoneNumberInput)
            .performScrollTo()
            .performTextInput("1001")
        composeRule
            .onNodeWithTag(CallingTestTags.DialButton)
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        composeRule
            .onNodeWithText("Outgoing call")
            .assertIsDisplayed()
        composeRule
            .onNodeWithText("Receiver")
            .assertIsDisplayed()

        composeRule
            .onNodeWithTag(CallingTestTags.MuteButton)
            .performClick()
        composeRule
            .onNodeWithTag(CallingTestTags.HoldButton)
            .performClick()
        composeRule
            .onNodeWithTag(CallingTestTags.HangUpButton)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(listOf("1001"), softphoneClient.dialedNumbers)
            assertEquals(listOf(true), softphoneClient.muteChanges)
            assertEquals(listOf(true), softphoneClient.holdChanges)
            assertEquals(1, softphoneClient.hangUps)
        }
    }
}

private class FakeRouteSoftphoneClient : SoftphoneClient {
    override val registrationStatus = MutableStateFlow(RegistrationStatus.NotConfigured)
    override val activeCall = MutableStateFlow<CallSession?>(null)

    var configuredCredentials: SipCredentials? = null
        private set
    val dialedNumbers = mutableListOf<String>()
    val muteChanges = mutableListOf<Boolean>()
    val holdChanges = mutableListOf<Boolean>()
    var hangUps = 0
        private set

    override fun configureAccount(credentials: SipCredentials) {
        configuredCredentials = credentials
    }

    override fun dial(number: String): CallPlacementResult {
        dialedNumbers += number
        val call = CallSession(
            displayName = "Receiver",
            number = number,
            stateLabel = "Trying",
            startedAtMillis = null,
            isMuted = false,
            isHeld = false
        )
        activeCall.value = call
        return CallPlacementResult.Success(call)
    }

    override fun hangUp() {
        hangUps += 1
    }

    override fun setMuted(muted: Boolean) {
        muteChanges += muted
    }

    override fun setHeld(held: Boolean) {
        holdChanges += held
    }
}
