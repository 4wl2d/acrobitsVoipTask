package com.tomilov.acrobitsvoip.voip

sealed interface CallPlacementResult {
    data class Success(val call: CallSession) : CallPlacementResult
    data class Failure(val message: String) : CallPlacementResult
}
