package com.example.gaugedialinformation.controller

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.os.RemoteCallbackList
import android.os.RemoteException
import androidx.core.content.getSystemService
import androidx.core.location.LocationListenerCompat
import com.example.common.checkPermission
import com.example.gaugedialinformation.exceptions.ContextDeadException
import com.example.gaugedialinformationlib.IGaugeDialInformationListener
import com.example.gaugedialinformationlib.SpeedUpdate
import com.example.gaugedialinformationlib.SpeedUpdateType
import java.lang.ref.WeakReference

class LocationSpeedProvider(context: Context) : SpeedProvider {
    companion object {
        private const val MINIMUM_UPDATE_TIME = 1000L
        private const val MINIMUM_UPDATE_DISTANCE = 0f
    }

    private val weakContext = WeakReference(context.applicationContext)

    private val context: Context
        get() = weakContext.get() ?: throw ContextDeadException()

    private val listeners: RemoteCallbackList<IGaugeDialInformationListener> = RemoteCallbackList()

    private var locationManager: LocationManager? = context.getSystemService()

    private val mProvider: String? = getBestProvider()

    private val locationListener: LocationListenerCompat = getLocationListener()

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
                !isProviderEnabled -> SpeedUpdate(SpeedUpdateType.PROVIDER_DISABLED)
                else -> SpeedUpdate(SpeedUpdateType.SPEED_UNAVAILABLE)
            }
        )
    }

    private var resumed: Boolean = false
        private set(value) {
            field = value
            sendSpeedUpdate()
        }

    private var isProviderEnabled = false
        private set(value) {
            field = value
            sendSpeedUpdate()
        }

    init {
        updateProviderState()
    }

    private fun getLocationListener() = object : LocationListenerCompat {
        override fun onLocationChanged(location: Location) {
            currentSpeed = location.speed
        }

        override fun onProviderEnabled(provider: String) {
            if (provider == mProvider) {
                isProviderEnabled = true
            }
        }

        override fun onProviderDisabled(provider: String) {
            if (provider == mProvider) {
                isProviderEnabled = false
            }
            currentSpeed = null
        }
    }

    private fun getBestProvider() = locationManager?.getBestProvider(
        Criteria().apply { isSpeedRequired = true }, false
    )

    private fun updateProviderState() {
        isProviderEnabled =
            mProvider != null && locationManager?.isProviderEnabled(mProvider) == true
    }

    @SuppressLint("MissingPermission")
    override fun resume(): Boolean {
        if (resumed) {
            return true
        }
        updateProviderState()
        if (context.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION).granted && mProvider != null) {
            locationManager?.requestLocationUpdates(
                mProvider,
                MINIMUM_UPDATE_TIME,
                MINIMUM_UPDATE_DISTANCE,
                locationListener,
                Looper.getMainLooper()
            )
        }
        resumed = true
        return resumed
    }

    override fun pause(): Boolean {
        if (!resumed) {
            return true
        }
        resumed = false
        updateProviderState()
        locationManager?.removeUpdates(locationListener)
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
        locationManager = null
    }
}