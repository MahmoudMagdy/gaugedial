package com.example.gaugedialinformationmanager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.UserHandle
import com.example.gaugedialinformationlib.IGaugeDialInformationListener
import com.example.gaugedialinformationlib.IGaugeDialInformationService
import com.example.gaugedialinformationmanager.model.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.Closeable

class GaugeDialInformationManager : Closeable {
    companion object {
        private const val PACKAGE_NAME = "com.example.gaugedialinformation"
        private const val CLASS_NAME = "$PACKAGE_NAME.GaugeDialInformationService"
        private val SERVICE_COMPONENT = ComponentName(PACKAGE_NAME, CLASS_NAME)
    }

    private val connection: ServiceConnection = createServiceConnection()
    private var mService: IGaugeDialInformationService? = null

    private val _connectionState = MutableStateFlow(ConnectionState.UNINITIALIZED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private fun createServiceConnection(): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                mService = IGaugeDialInformationService.Stub.asInterface(service)
                _connectionState.value = ConnectionState.CONNECTED
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                mService = null
                _connectionState.value = ConnectionState.DISCONECTED
            }

            override fun onBindingDied(name: ComponentName?) = onServiceDisconnected(name)
        }
    }

    fun bind(context: Context): Boolean {
        val intent = Intent()
        intent.component = SERVICE_COMPONENT
        return context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbind(context: Context) {
        context.unbindService(connection)
    }

    fun registerListener(listener: IGaugeDialInformationListener?): Boolean {
        if (connectionState.value != ConnectionState.CONNECTED) {
            return false
        }
        return mService?.registerListener(listener) ?: false
    }

    fun unregisterListener(listener: IGaugeDialInformationListener?): Boolean {
        if (connectionState.value != ConnectionState.CONNECTED) {
            return false
        }
        return mService?.unregisterListener(listener) ?: false
    }

    fun resume(): Boolean {
        if (connectionState.value != ConnectionState.CONNECTED) {
            return false
        }
        return mService?.resume() ?: false
    }

    fun pause(): Boolean {
        if (connectionState.value != ConnectionState.CONNECTED) {
            return false
        }
        return mService?.pause() ?: false
    }

    override fun close() {
        mService = null
    }

}