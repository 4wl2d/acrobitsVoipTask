package com.tomilov.acrobitsvoip.feature.calling.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tomilov.acrobitsvoip.core.designsystem.theme.AcrobitsVoipTheme
import com.tomilov.acrobitsvoip.core.voip.CallSession
import com.tomilov.acrobitsvoip.feature.calling.CallingTestTags
import com.tomilov.acrobitsvoip.feature.calling.CallingUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallingScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun welcomePermissionButtonRequestsMicrophonePermission() {
        var permissionRequests = 0
        composeRule.setThemedContent {
            WelcomeScreen(
                permissionRequested = false,
                onRequestMicrophonePermission = { permissionRequests += 1 }
            )
        }

        composeRule
            .onNodeWithTag(CallingTestTags.WelcomePermissionButton)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, permissionRequests)
        }
    }

    @Test
    fun dialerAccountFieldsEnableRegisterAndRegisterClick() {
        var registerClicks = 0
        lateinit var uiState: CallingUiState
        composeRule.setThemedContent {
            var state by remember { mutableStateOf(CallingUiState()) }
            uiState = state

            DialerScreen(
                uiState = state,
                onSipUsernameChanged = { state = state.copy(sipUsername = it) },
                onSipPasswordChanged = { state = state.copy(sipPassword = it) },
                onRegister = { registerClicks += 1 },
                onPhoneNumberChanged = { state = state.copy(phoneNumber = it) },
                onDial = {},
                onMessageShown = {}
            )
        }

        composeRule
            .onNodeWithTag(CallingTestTags.RegisterButton)
            .assertIsNotEnabled()
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
            assertEquals("1000", uiState.sipUsername)
            assertEquals("secret", uiState.sipPassword)
            assertEquals(1, registerClicks)
        }
    }

    @Test
    fun dialerPhoneFieldEnablesDialOnlyWhenRegisteredAndValid() {
        var dialClicks = 0
        lateinit var uiState: CallingUiState
        composeRule.setThemedContent {
            var state by remember {
                mutableStateOf(
                    CallingUiState(
                        sipUsername = "1000",
                        sipPassword = "secret",
                        isRegistered = true,
                        registrationLabel = "Registered"
                    )
                )
            }
            uiState = state

            DialerScreen(
                uiState = state,
                onSipUsernameChanged = { state = state.copy(sipUsername = it) },
                onSipPasswordChanged = { state = state.copy(sipPassword = it) },
                onRegister = {},
                onPhoneNumberChanged = { state = state.copy(phoneNumber = it, phoneNumberError = null) },
                onDial = { dialClicks += 1 },
                onMessageShown = {}
            )
        }

        composeRule
            .onNodeWithTag(CallingTestTags.DialButton)
            .performScrollTo()
            .assertIsNotEnabled()
        composeRule
            .onNodeWithTag(CallingTestTags.PhoneNumberInput)
            .performScrollTo()
            .performTextInput("1001")
        composeRule
            .onNodeWithTag(CallingTestTags.DialButton)
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals("1001", uiState.phoneNumber)
            assertEquals(1, dialClicks)
        }
    }

    @Test
    fun callControlsToggleMuteHoldAndHangUp() {
        val muteChanges = mutableListOf<Boolean>()
        val holdChanges = mutableListOf<Boolean>()
        var hangUps = 0
        composeRule.setThemedContent {
            CallScreen(
                call = activeCall(isMuted = false, isHeld = false),
                duration = "00:05",
                message = null,
                onHangUp = { hangUps += 1 },
                onMuteChanged = { muteChanges += it },
                onHoldChanged = { holdChanges += it },
                onMessageShown = {}
            )
        }

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
            assertEquals(listOf(true), muteChanges)
            assertEquals(listOf(true), holdChanges)
            assertEquals(1, hangUps)
        }
    }

    @Test
    fun selectedCallControlsToggleMuteAndHoldOff() {
        val muteChanges = mutableListOf<Boolean>()
        val holdChanges = mutableListOf<Boolean>()
        composeRule.setThemedContent {
            CallScreen(
                call = activeCall(isMuted = true, isHeld = true),
                duration = "00:05",
                message = null,
                onHangUp = {},
                onMuteChanged = { muteChanges += it },
                onHoldChanged = { holdChanges += it },
                onMessageShown = {}
            )
        }

        composeRule
            .onNodeWithTag(CallingTestTags.MuteButton)
            .performClick()
        composeRule
            .onNodeWithTag(CallingTestTags.HoldButton)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(false), muteChanges)
            assertEquals(listOf(false), holdChanges)
        }
    }

    private fun activeCall(
        isMuted: Boolean,
        isHeld: Boolean
    ): CallSession = CallSession(
        displayName = "Receiver",
        number = "1001",
        stateLabel = "Established",
        startedAtMillis = null,
        isMuted = isMuted,
        isHeld = isHeld
    )

    private fun ComposeContentTestRule.setThemedContent(
        content: @Composable () -> Unit
    ) {
        setContent {
            AcrobitsVoipTheme(dynamicColor = false) {
                content()
            }
        }
    }
}
