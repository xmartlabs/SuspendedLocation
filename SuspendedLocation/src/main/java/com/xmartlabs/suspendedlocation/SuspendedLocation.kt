package com.xmartlabs.suspendedlocation

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object SuspendedLocation {
    private lateinit var context: Context
    private lateinit var locationServices: FusedLocationProviderClient

    fun initialize(context: Context) {
        this.context = context
        locationServices = LocationServices.getFusedLocationProviderClient(context)
    }

    @ExperimentalCoroutinesApi
    @RequiresPermission(
        anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"]
    )
    fun requestCurrentLocation(
        locationRequest: LocationRequest,
        runnerContext: CoroutineContext = Dispatchers.Default
    ): Flow<Location> = callbackFlow<Location> {
        locationServices
            .requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    @Synchronized
                    override fun onLocationResult(locationResult: LocationResult) {
                        launch(runnerContext) {
                            this@callbackFlow.send(locationResult.lastLocation)
                        }
                    }
                },
                locationServices.looper
            )
        awaitClose { cancel() }
    }.flowOn(runnerContext)

    @RequiresPermission(
        anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"]
    )
    suspend fun requestCurrentLocation(
        runnerContext: CoroutineContext = Dispatchers.Default
    ): Location = withContext(runnerContext) {
        suspendCoroutine { continuation: Continuation<Location> ->
            locationServices
                .requestLocationUpdates(
                    LocationRequest().setInterval(1),
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            launch(runnerContext) {
                                continuation.resume(locationResult.lastLocation)
                            }
                        }
                    },
                    locationServices.looper
                )
        }
    }

    @WorkerThread
    suspend fun reverseGeocode(
        location: LatLong,
        maxResults: Int,
        runnerContext: CoroutineContext = Dispatchers.Default
    ): List<Address> = withContext(runnerContext) {
        suspendCoroutine { continuation: Continuation<List<Address>> ->
            continuation.resume(
                Geocoder(context).getFromLocation(location.lat, location.long, maxResults)
            )
        }
    }

    @WorkerThread
    suspend fun geocode(
        locationName: String,
        maxResults: Int,
        runnerContext: CoroutineContext = Dispatchers.Default
    ): List<Address> = withContext(runnerContext) {
        suspendCoroutine { continuation: Continuation<List<Address>> ->
            continuation.resume(
                Geocoder(context).getFromLocationName(locationName, maxResults)
            )
        }
    }

    @WorkerThread
    suspend fun geocode(
        locationName: String,
        startingPoint: LatLong,
        endPoint: LatLong,
        maxResults: Int,
        runnerContext: CoroutineContext = Dispatchers.Default
    ): List<Address> = withContext(runnerContext) {
        suspendCoroutine { continuation: Continuation<List<Address>> ->
            continuation.resume(
                Geocoder(context).getFromLocationName(
                    locationName,
                    maxResults,
                    startingPoint.lat,
                    startingPoint.long,
                    endPoint.lat,
                    endPoint.long
                )
            )
        }
    }
}
