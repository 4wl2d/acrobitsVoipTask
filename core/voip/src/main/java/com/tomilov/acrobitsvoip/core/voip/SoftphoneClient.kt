package com.tomilov.acrobitsvoip.core.voip

import kotlinx.coroutines.flow.StateFlow

interface SoftphoneClient {
    val registrationStatus: StateFlow<RegistrationStatus>
    val activeCall: StateFlow<CallSession?>

    fun configureAccount(credentials: SipCredentials)
    fun dial(number: String): CallPlacementResult
    fun hangUp()
    fun setMuted(muted: Boolean)
    fun setHeld(held: Boolean)
}
