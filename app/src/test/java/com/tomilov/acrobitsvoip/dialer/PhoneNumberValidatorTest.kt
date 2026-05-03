package com.tomilov.acrobitsvoip.dialer

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumberValidatorTest {
    @Test
    fun `accepts SIP extension numbers`() {
        assertEquals(
            PhoneNumberValidation.Valid("1001"),
            PhoneNumberValidator.validate("1001")
        )
    }

    @Test
    fun `normalizes common phone number formatting`() {
        assertEquals(
            PhoneNumberValidation.Valid("+12345678900"),
            PhoneNumberValidator.validate("+1 (234) 567-8900")
        )
    }

    @Test
    fun `rejects blank number input`() {
        assertEquals(
            PhoneNumberValidation.Invalid("Enter a number to call."),
            PhoneNumberValidator.validate("  ")
        )
    }

    @Test
    fun `rejects unsupported characters`() {
        assertEquals(
            PhoneNumberValidation.Invalid(
                "Use digits, an optional leading +, spaces, dashes, or parentheses."
            ),
            PhoneNumberValidator.validate("1001a")
        )
    }

    @Test
    fun `rejects plus outside the first character`() {
        assertEquals(
            PhoneNumberValidation.Invalid(
                "Use digits, an optional leading +, spaces, dashes, or parentheses."
            ),
            PhoneNumberValidator.validate("31+01")
        )
    }

    @Test
    fun `rejects numbers shorter than two digits`() {
        assertEquals(
            PhoneNumberValidation.Invalid("Enter at least 2 digits."),
            PhoneNumberValidator.validate("1")
        )
    }

    @Test
    fun `rejects numbers longer than thirty two digits`() {
        assertEquals(
            PhoneNumberValidation.Invalid("Enter no more than 32 digits."),
            PhoneNumberValidator.validate("1".repeat(33))
        )
    }
}
