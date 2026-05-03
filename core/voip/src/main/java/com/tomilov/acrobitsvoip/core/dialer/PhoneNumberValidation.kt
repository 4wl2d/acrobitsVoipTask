package com.tomilov.acrobitsvoip.core.dialer

sealed interface PhoneNumberValidation {
    data class Valid(val normalizedNumber: String) : PhoneNumberValidation
    data class Invalid(val message: String) : PhoneNumberValidation
}
