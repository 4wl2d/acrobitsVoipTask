package com.tomilov.acrobitsvoip.feature.calling

/**
 * Semantics tags used by instrumentation tests.
 *
 * These are not stable feature API; production code should not depend on them.
 */
object CallingTestTags {
    const val WelcomePermissionButton = "welcome_permission_button"
    const val SipUsernameInput = "sip_username_input"
    const val SipPasswordInput = "sip_password_input"
    const val RegisterButton = "register_button"
    const val PhoneNumberInput = "phone_number_input"
    const val DialButton = "dial_button"
    const val MuteButton = "mute_button"
    const val HoldButton = "hold_button"
    const val HangUpButton = "hang_up_button"
}
