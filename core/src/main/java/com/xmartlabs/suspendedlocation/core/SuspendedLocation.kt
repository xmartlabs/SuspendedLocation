package com.xmartlabs.suspendedlocation.core

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
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
import java.io.IOException
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SuspendedLocation {
    private lateinit var context: Context
    private lateinit var locationServices: FusedLocationProviderClient

    fun initialize(context: Context) {
        SuspendedLocation.context = context
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

                    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                        super.onLocationAvailability(locationAvailability)
                        if (!locationAvailability.isLocationAvailable) {
                            launch(runnerContext) {
                                this@callbackFlow.cancel(
                                    "Location not available",
                                    IOException("Location not available")
                                )
                            }
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

                        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                            super.onLocationAvailability(locationAvailability)
                            if (!locationAvailability.isLocationAvailable) {
                                launch(runnerContext) {
                                    continuation.resumeWithException(
                                        IOException("Location not available")
                                    )
                                }
                            }
                        }
                    },
                    locationServices.looper
                )
        }
    }

    suspend fun reverseGeocode(
        location: LatLong,
        maxResults: Int,
        runnerContext: CoroutineContext = Dispatchers.Default
    ): List<Address> = withContext(runnerContext) {
        suspendCoroutine { continuation: Continuation<List<Address>> ->
            try {
                continuation.resume(
                    Geocoder(context).getFromLocation(location.lat, location.long, maxResults)
                )
            } catch (io: IOException) {
                continuation.resumeWithException(io)
            } catch (illegalArgument: IllegalArgumentException) {
                continuation.resumeWithException(illegalArgument)
            }
        }
    }

    suspend fun geocode(
        locationName: String,
        maxResults: Int,
        runnerContext: CoroutineContext = Dispatchers.Default
    ): List<Address> = withContext(runnerContext) {
        suspendCoroutine { continuation: Continuation<List<Address>> ->
            try {
                continuation.resume(
                    Geocoder(context).getFromLocationName(locationName, maxResults)
                )
            } catch (io: IOException) {
                continuation.resumeWithException(io)
            }
        }
    }

    suspend fun geocode(
        locationName: String,
        startingPoint: LatLong,
        endPoint: LatLong,
        maxResults: Int,
        runnerContext: CoroutineContext = Dispatchers.Default
    ): List<Address> = withContext(runnerContext) {
        suspendCoroutine { continuation: Continuation<List<Address>> ->
            try {
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
            } catch (io: IOException) {
                continuation.resumeWithException(io)
            } catch (illegalArgument: IllegalArgumentException) {
                continuation.resumeWithException(illegalArgument)
            }
        }
    }
}
