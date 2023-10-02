package com.example.gaugedialinformation

import android.content.Context
import com.example.gaugedialinformation.controller.RandomSpeedProvider
import com.example.gaugedialinformation.controller.SpeedProvider
import com.example.gaugedialinformation.exceptions.ContextDeadException
import com.example.gaugedialinformationlib.IGaugeDialInformationListener
import com.example.gaugedialinformationlib.IGaugeDialInformationService
import java.io.Closeable
import java.lang.ref.WeakReference

class GaugeDialInformationServiceImpl(context: Context) : IGaugeDialInformationService.Stub(),
    Closeable {
    private val weakContext = WeakReference(context.applicationContext)

    private val context: Context
        get() = weakContext.get() ?: throw ContextDeadException()

    //private val speedProvider: SpeedProvider = LocationSpeedProvider(context.applicationContext)
    private val speedProvider: SpeedProvider = RandomSpeedProvider()

    override fun registerListener(listener: IGaugeDialInformationListener?): Boolean {
        if (listener == null) {
            return false
        }
        return speedProvider.registerListener(listener)
    }

    override fun unregisterListener(listener: IGaugeDialInformationListener?): Boolean {
        if (listener == null) {
            return false
        }
        return speedProvider.unregisterListener(listener)
    }

    override fun resume(): Boolean {
        return speedProvider.resume()
    }

    override fun pause(): Boolean {
        return speedProvider.pause()
    }

    override fun close() {
        weakContext.clear()
        speedProvider.close()
    }
}