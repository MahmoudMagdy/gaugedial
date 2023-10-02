package com.example.gaugedial.model

data class Point(val x: Float, val y: Float) {
    operator fun plus(delta: Int) = Point(x + delta, y + delta)
}
