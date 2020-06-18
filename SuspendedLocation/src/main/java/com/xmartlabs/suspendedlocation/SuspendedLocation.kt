package com.xmartlabs.suspendedlocation

import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object SuspendedLocation {
    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context
    }

    @RequiresPermission(
        anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"]
    )
    suspend fun requestCurrentLocation(
        locationRequest: LocationRequest,
        runnerContext: CoroutineContext = Dispatchers.Default
    ): Location = withContext(runnerContext) {
        suspendCoroutine { continuation: Continuation<Location> ->
            LocationServices.getFusedLocationProviderClient(context)
                .requestLocationUpdates(
                    locationRequest,
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            continuation.resume(locationResult.lastLocation)
                        }
                    },
                    Looper.myLooper()
                )
        }
    }
}
