package com.kursor.chroniclesofww2.connection.local

import android.app.Activity
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.kursor.chroniclesofww2.connection.Host
import com.kursor.chroniclesofww2.connection.interfaces.SERVICE_NAME
import com.kursor.chroniclesofww2.connection.interfaces.SERVICE_TYPE

class NsdDiscovery(
    val nsdManager: NsdManager,
    val discoveryListener: Listener,
) {

    constructor(activity: Activity, discoveryListener: Listener) : this(
        activity.getSystemService(Context.NSD_SERVICE) as NsdManager,
        discoveryListener
    )

    interface Listener {
        fun onResolveFailed(errorCode: Int)
        fun onHostFound(host: Host)
        fun onHostLost(host: Host)
        fun onDiscoveryFailed(errorCode: Int)

    }

    private val nsdDiscoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Service discovery success: $serviceInfo")
            when {
                serviceInfo.serviceType != SERVICE_TYPE -> {
                    Log.d(TAG, "Unknown Service Type: ${serviceInfo.serviceType}")
                }
                serviceInfo.serviceName == serviceName -> {
                    Log.d(TAG, "Same machine: $serviceName")
                }
                serviceInfo.serviceName.contains(SERVICE_NAME) -> {
                    nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            Log.e(TAG, "Resolve failed $errorCode")
                            discoveryListener.onResolveFailed(errorCode)
                        }

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            Log.e(TAG, "Resolve Succeeded. $serviceInfo")
                            if (serviceInfo.serviceName == serviceName) {
                                Log.d(TAG, "Same IP.")
                                return
                            }
                            discoveryListener.onHostFound(Host(serviceInfo))
                        }
                    })
                }
            }
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "service lost $serviceInfo")
            discoveryListener.onHostLost(Host(serviceInfo)) //TODO(Peer list remove)
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code: $errorCode")
            nsdManager.stopServiceDiscovery(this)
            discoveryListener.onDiscoveryFailed(errorCode)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code: $errorCode")
            nsdManager.stopServiceDiscovery(this)
            discoveryListener.onDiscoveryFailed(errorCode)
        }
    }

    private var serviceName = SERVICE_NAME


    fun startDiscovery() {
        Log.i(TAG, "Start discovery")
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, nsdDiscoveryListener)
    }

    fun stopDiscovery() {
        Log.i(TAG, "Stop discovery")
        try {
            nsdManager.stopServiceDiscovery(nsdDiscoveryListener)
        } catch (e: Exception) {
            Log.i(TAG, e.message!!)
        }
    }

    companion object {
        const val TAG = "NsdDiscovery"
    }
}