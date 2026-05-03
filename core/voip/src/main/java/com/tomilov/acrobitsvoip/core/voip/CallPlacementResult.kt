package com.tomilov.acrobitsvoip.core.voip

sealed interface CallPlacementResult {
    data class Success(val call: CallSession) : CallPlacementResult
    data class Failure(val message: String) : CallPlacementResult
}
