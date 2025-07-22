package com.teasoft.taxi.meter

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import kotlin.math.roundToInt

class LocationCalculator(private var locationManager: LocationManager) : LocationListener {
    private var lastLocationOfDistance: Location? = null
    private var lastLocation: Location? = null
    private var lastSpeed: Float = 0f
    private var totalDistance: Float = 0f
    private var checkDistance: Boolean = true
    override fun onLocationChanged(p0: Location) {
        Log.e("TAG_Location", "${p0.latitude}: ${p0.longitude}")
        Log.e("TAG_Location", "${p0.accuracy}")
        if (lastLocation == null) {
            lastLocation = p0
            if (checkDistance) {
                lastLocationOfDistance = p0
                checkDistance = false
            }
            Log.e("TAG_Location", "run: 1")

        } else {

            val distance = lastLocation!!.distanceTo(p0) / 1000f
            val timeDiff = (p0.time - lastLocation!!.time) / 1000f

            val speed = (distance / timeDiff) * 3600f
            lastSpeed = speed
            lastLocation = p0
            if (checkDistance) {
                totalDistance += lastLocationOfDistance!!.distanceTo(p0)

                lastLocationOfDistance = p0
                checkDistance = false
            }
        }

    }

    fun getSpeed(): Float {
        return lastSpeed
    }

    fun getDistance(): Long {
        Log.e("getDistance: ", (totalDistance).toString())
        return ((((totalDistance / 1000) * 10).roundToInt() / 10.0).toLong())
    }

    fun start() {
        lastLocation = null
        lastLocationOfDistance = null
        totalDistance = 0f

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1f, this)
    }

    fun stop() {
        Log.e("TAG_Location", "stop")

        locationManager.removeUpdates(this)
    }

    fun setCheck(b: Boolean) {
        checkDistance = b
    }
}