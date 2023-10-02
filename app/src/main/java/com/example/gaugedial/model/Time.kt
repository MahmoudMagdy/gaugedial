package com.example.gaugedial.model

data class Time(val seconds: Double) {
    companion object {
        private const val MILLISECONDS = 1000
        private const val NANOSECONDS = 1000_000_000
    }

    val milliseconds = seconds * MILLISECONDS

    val nanoseconds = seconds * NANOSECONDS
}