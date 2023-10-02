package com.example.gaugedial

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.common.PermissionRequestState
import com.example.common.checkMultiplePermissions
import com.example.common.checkPermission
import com.example.common.launchMobileSettings
import com.example.common.permissionsRequestState
import com.example.common.requestPermissions
import com.example.gaugedialinformationlib.IGaugeDialInformationListener
import com.example.gaugedialinformationlib.SpeedUpdate
import com.example.gaugedialinformationlib.SpeedUpdateType
import com.example.gaugedialinformationmanager.GaugeDialInformationManager
import com.example.gaugedialinformationmanager.model.ConnectionState
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), GaugeDial.OnTwoFingerScrollListener {

    companion object {
        private const val MOTOR_RADIUS_IN_METERS = 0.2f
    }

    private lateinit var speedometer: GaugeDial
    private lateinit var tachometer: GaugeDial
    private var gaugeDialInformationManager: GaugeDialInformationManager? =
        GaugeDialInformationManager()
    private val listener: IGaugeDialInformationListener =
        object : IGaugeDialInformationListener.Stub() {
            override fun onSpeedUpdated(update: SpeedUpdate?) {
                val speed = update?.speed
                if (update?.type == SpeedUpdateType.SPEED_CHANGED && speed != null) {
                    speedometer.updateTheta(speed)
                    tachometer.updateTheta(
                        calculateRPMToKUnit(
                            kmPerHourToMetersPerSecond(speed),
                            MOTOR_RADIUS_IN_METERS
                        )
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
        setContentView(R.layout.activity_main)
        speedometer = findViewById(R.id.speedometer)
        tachometer = findViewById(R.id.tachometer)
        speedometer.setOnTwoFingerScrollListener(this)
        tachometer.setOnTwoFingerScrollListener(this)
        lifecycleScope.launch {
            gaugeDialInformationManager?.connectionState?.collect {
                if (it == ConnectionState.CONNECTED) {
                    gaugeDialInformationManager?.registerListener(listener)
                    gaugeDialInformationManager?.resume()
                }
            }
        }
        gaugeDialInformationManager?.bind(this)
    }

    // Function to hide NavigationBar
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(
                window,
                window.decorView.findViewById(android.R.id.content)
            ).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())

                // When the screen is swiped up at the bottom
                // of the application, the navigationBar shall
                // appear for some time
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.apply {
                // Hide both the navigation bar and the status bar.
                // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
                // a general rule, you should design your app to hide the status bar whenever you
                // hide the navigation bar.
                systemUiVisibility =
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (tachometer.visibility == View.VISIBLE) {
            tachometer.resume()
        } else {
            speedometer.resume()
        }
        if (!checkPermission(ACCESS_FINE_LOCATION).granted) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION))
            return
        }
        gaugeDialInformationManager?.resume()
    }

    override fun onPause() {
        super.onPause()
        speedometer.pause()
        tachometer.pause()
        gaugeDialInformationManager?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        gaugeDialInformationManager?.apply {
            unregisterListener(listener)
            unbind(this@MainActivity)
            close()
        }
        gaugeDialInformationManager = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (permissionsRequestState(checkMultiplePermissions(permissions))) {
            PermissionRequestState.Accepted -> gaugeDialInformationManager?.resume()
            PermissionRequestState.PermanentDenied -> {
                launchMobileSettings()
                Toast.makeText(
                    this,
                    "I've opened for you mobile settings to accept the permission.",
                    Toast.LENGTH_LONG
                ).show()
            }

            is PermissionRequestState.ShouldShowRational -> {
                Toast.makeText(this, "Please accept the permission.", Toast.LENGTH_LONG).show()
                requestPermissions(arrayOf(ACCESS_FINE_LOCATION))
            }
        }

    }

    fun kmPerHourToMetersPerSecond(kmPerHour: Float): Float {
        val conversionFactor = 0.27778f
        return kmPerHour * conversionFactor
    }

    fun calculateRPMToKUnit(speed: Float, radius: Float): Float {
        return calculateRPM(speed, radius) / 1000
    }

    private fun calculateRPM(speed: Float, radius: Float): Float {
        return (speed * 60 / (2 * Math.PI * radius)).toFloat()
    }

    override fun onTwoFingerScroll(distance: Float) {
        if (tachometer.visibility != View.VISIBLE) {
            tachometer.visibility = View.VISIBLE
            tachometer.resume()
            speedometer.pause()
            speedometer.visibility = View.GONE
        } else {
            tachometer.pause()
            tachometer.visibility = View.GONE
            speedometer.visibility = View.VISIBLE
            speedometer.resume()
        }
    }
}