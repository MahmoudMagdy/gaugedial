package com.example.gaugedialinformation.controller

import com.example.gaugedialinformationlib.IGaugeDialInformationListener
import com.example.gaugedialinformationlib.SpeedUpdate
import java.io.Closeable

interface SpeedProvider : Closeable {
    fun registerListener(listener: IGaugeDialInformationListener): Boolean
    fun unregisterListener(listener: IGaugeDialInformationListener): Boolean
    fun updateListener(speedUpdate: SpeedUpdate)
    fun resume(): Boolean
    fun pause(): Boolean
}