package com.tomilov.acrobitsvoip.time

fun interface AppClock {
    fun currentTimeMillis(): Long
}

object SystemAppClock : AppClock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
