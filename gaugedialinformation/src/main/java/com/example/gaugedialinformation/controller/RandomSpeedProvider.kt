package com.example.gaugedialinformation.controller

import android.annotation.SuppressLint
import android.os.RemoteCallbackList
import android.os.RemoteException
import com.example.gaugedialinformationlib.IGaugeDialInformationListener
import com.example.gaugedialinformationlib.SpeedUpdate
import com.example.gaugedialinformationlib.SpeedUpdateType
import kotlin.math.abs
import kotlin.math.sin

class RandomSpeedProvider : SpeedProvider, Runnable {
    companion object {
        private const val UPDATE_TIME = 1000L
        private const val MAX_SPEED = 240.0
    }

    private val listeners: RemoteCallbackList<IGaugeDialInformationListener> = RemoteCallbackList()
    private var thread: Thread? = null
    private val amplitude1 = 10.0
    private val period1 = 2 * Math.PI
    private val amplitude2 = 5.0
    private val period2 = Math.PI
    private var startTimeMillis = 0L

    private var currentSpeed: Float? = null
        private set(value) {
            field = value
            sendSpeedUpdate()
        }

    private fun sendSpeedUpdate() {
        val speed = currentSpeed
        updateListener(
            when {
                !resumed -> SpeedUpdate(SpeedUpdateType.DISABLED)
                speed != null -> SpeedUpdate(SpeedUpdateType.SPEED_CHANGED, speed)
                else -> SpeedUpdate(SpeedUpdateType.SPEED_UNAVAILABLE)
            }
        )
    }

    private var resumed: Boolean = false

    @SuppressLint("MissingPermission")
    override fun resume(): Boolean {
        if (resumed) {
            return true
        }
        startTimeMillis = System.currentTimeMillis()
        thread = Thread(this)
        thread?.start()
        resumed = true
        return resumed
    }

    override fun pause(): Boolean {
        if (!resumed) {
            return true
        }
        resumed = false
        while (true) {
            try {
                thread?.join()
                break
            } catch (exception: InterruptedException) {
                exception.printStackTrace()
            }
        }
        thread = null
        return !resumed
    }


    override fun registerListener(listener: IGaugeDialInformationListener): Boolean {
        return listeners.register(listener)
    }

    override fun unregisterListener(listener: IGaugeDialInformationListener): Boolean {
        return listeners.unregister(listener)
    }

    override fun updateListener(speedUpdate: SpeedUpdate) {
        try {
            for (position in 0 until listeners.beginBroadcast()) {
                listeners.getBroadcastItem(position).onSpeedUpdated(speedUpdate)
            }
        } catch (exception: RemoteException) {
            exception.printStackTrace()
        } finally {
            listeners.finishBroadcast()
        }
    }

    override fun close() {
        pause()
    }

    override fun run() {
        while (resumed) {
            val currentTimeMillis = System.currentTimeMillis()
            val elapsedTime =
                (currentTimeMillis - startTimeMillis).toDouble() / 1000.0 // Convert to seconds

            val speed = abs(
                MAX_SPEED * (amplitude1 * sin(2 * Math.PI * elapsedTime / period1) +
                        amplitude2 * sin(2 * Math.PI * elapsedTime / period2))
            ) % MAX_SPEED
            currentSpeed = speed.toFloat()
            Thread.sleep(UPDATE_TIME)
        }
    }
}