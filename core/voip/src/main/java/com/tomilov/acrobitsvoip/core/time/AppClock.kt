package com.tomilov.acrobitsvoip.core.time

fun interface AppClock {
    fun currentTimeMillis(): Long
}

object SystemAppClock : AppClock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
