package com.example.gaugedialinformation

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.io.Closeable

class GaugeDialInformationService : Service(), Closeable {
    private var gaugeDialInformationService: GaugeDialInformationServiceImpl? = null

    override fun onCreate() {
        super.onCreate()
        gaugeDialInformationService = GaugeDialInformationServiceImpl(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return gaugeDialInformationService
    }

    override fun onDestroy() {
        super.onDestroy()
        close()
    }

    override fun close() {
        gaugeDialInformationService?.close()
        gaugeDialInformationService = null
    }
}