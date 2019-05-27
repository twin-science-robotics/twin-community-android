package com.twinscience.twin.lite.android.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager

class GPSHelper(context: Context) {

    private val locationManager: LocationManager = context
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager
    /**
     * Function to get latitude
     */
    var latitude: Double = 0.toDouble()
        private set
    /**
     * Function to get longitude
     */
    var longitude: Double = 0.toDouble()
        private set
    private var location: Location? = null

    // getting network status
    val isGPSenabled: Boolean
        get() {
            val isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            return isGPSEnabled || isNetworkEnabled
        }

    /**
     * {@importantNote: You have to check manifest permission to use this method. Unless this will return null}
     *
     * @param context
     * @return last known location.
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(context: Context): Location? {
        val providers = locationManager.getProviders(true)
        location = null
        for (i in providers.indices) {
            location = locationManager.getLastKnownLocation(providers[i])
            if (location != null)
                break
        }
        if (location != null) {
            latitude = location!!.latitude
            longitude = location!!.longitude
        }
        return location
    }
}