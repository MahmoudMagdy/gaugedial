package com.example.gaugedial.model

import kotlin.math.ceil

//import kotlin.math.round

data class Frame(private val fps: Double) {
    val time = Time(1 / fps)
    var lastFrameTime: Double = 0.0
    var endOfDrawTime: Double = 0.0
    var timeDelta: Double = 0.0
    var droppedFrames = 0
    var overTime = 0L

    var count: Int = 0
    val isLast = count == (fps - 1).toInt()

    fun drop(): Boolean {
        val drop = droppedFrames != 0
        if (drop) {
            droppedFrames--
        }
        return drop
    }

    fun start() {
        lastFrameTime = System.nanoTime().toDouble()
        timeDelta = 0.0
    }

    fun end() {
        endOfDrawTime = System.nanoTime().toDouble()
        timeDelta = time.nanoseconds - (endOfDrawTime - lastFrameTime)
        if (timeDelta < 0) {
            droppedFrames = ceil((-timeDelta) / time.nanoseconds).toInt()
            val dropTime = time.nanoseconds * droppedFrames
            overTime = (dropTime + timeDelta).toLong()
            timeDelta = -(dropTime)
        }
        updateDisplayedFramesCount()
    }

    private fun updateDisplayedFramesCount() {
        count++
        if (count >= fps) {
            count = 0
        }
    }

    fun next() {
        lastFrameTime = System.nanoTime().toDouble()
    }
}