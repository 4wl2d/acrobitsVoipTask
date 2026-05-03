package com.tomilov.acrobitsvoip.core.voip

data class CallSession(
    val displayName: String,
    val number: String,
    val stateLabel: String,
    val startedAtMillis: Long?,
    val isMuted: Boolean,
    val isHeld: Boolean
)
