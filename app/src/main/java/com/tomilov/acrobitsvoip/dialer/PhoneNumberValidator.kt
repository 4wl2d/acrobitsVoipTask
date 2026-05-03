package com.tomilov.acrobitsvoip.dialer

object PhoneNumberValidator {
    private const val MIN_DIGITS = 2
    private const val MAX_DIGITS = 32
    private val formattingCharacters = setOf(' ', '-', '(', ')')

    fun validate(input: String): PhoneNumberValidation {
        val trimmedInput = input.trim()
        if (trimmedInput.isEmpty()) {
            return PhoneNumberValidation.Invalid("Enter a number to call.")
        }

        val normalizedNumber = buildString {
            trimmedInput.forEachIndexed { index, character ->
                when {
                    character.isDigit() -> append(character)
                    character == '+' && index == 0 -> append(character)
                    character in formattingCharacters -> Unit
                    else -> return PhoneNumberValidation.Invalid(
                        "Use digits, an optional leading +, spaces, dashes, or parentheses."
                    )
                }
            }
        }

        val digitCount = normalizedNumber.count(Char::isDigit)
        return when {
            digitCount < MIN_DIGITS -> PhoneNumberValidation.Invalid("Enter at least $MIN_DIGITS digits.")
            digitCount > MAX_DIGITS -> PhoneNumberValidation.Invalid("Enter no more than $MAX_DIGITS digits.")
            normalizedNumber == "+" -> PhoneNumberValidation.Invalid("Enter digits after +.")
            else -> PhoneNumberValidation.Valid(normalizedNumber)
        }
    }
}
