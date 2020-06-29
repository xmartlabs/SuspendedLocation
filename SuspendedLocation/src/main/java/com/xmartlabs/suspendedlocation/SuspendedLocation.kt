package com.xmartlabs.suspendedlocation

import android.content.Context
import android.graphics.Bitmap
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
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
    private lateinit var placesClient: PlacesClient

    fun initialize(context: Context) {
        this.context = context
        locationServices = LocationServices.getFusedLocationProviderClient(context)
        if (Places.isInitialized()) {
            placesClient = Places.createClient(context)
        } else {
            throw PlacesNotInitializedException()
        }
    }

    suspend fun placesAutocomplete(
        autocompleteRequest: FindAutocompletePredictionsRequest,
        runnerContext: CoroutineContext
    ) = withContext(runnerContext) {
        suspendCoroutine<List<AutocompletePrediction>> { continuation ->
            placesClient.findAutocompletePredictions(autocompleteRequest)
                .addOnSuccessListener { autocomplete ->
                    continuation.resume(autocomplete.autocompletePredictions)
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    @RequiresPermission(
        anyOf = ["android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_WIFI_STATE"]
    )
    suspend fun findCurrentPlace(
        findCurrentPlaceRequest: FindCurrentPlaceRequest,
        runnerContext: CoroutineContext
    ) = withContext(runnerContext) {
        suspendCoroutine<List<PlaceLikelihood>> { continuation ->
            placesClient.findCurrentPlace(findCurrentPlaceRequest)
                .addOnSuccessListener { findCurrentPlaceResponse ->
                    continuation.resume(findCurrentPlaceResponse.placeLikelihoods)
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    suspend fun fetchPlace(
        fetchPlaceRequest: FetchPlaceRequest,
        runnerContext: CoroutineContext
    ) = withContext(runnerContext) {
        suspendCoroutine<Place> { continuation ->
            placesClient.fetchPlace(fetchPlaceRequest)
                .addOnSuccessListener { placeResponse ->
                    continuation.resume(placeResponse.place)
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    suspend fun fetchPhoto(
        fetchPhotoRequest: FetchPhotoRequest,
        runnerContext: CoroutineContext
    ) = withContext(runnerContext) {
        suspendCoroutine<Bitmap> { continuation ->
            placesClient.fetchPhoto(fetchPhotoRequest)
                .addOnSuccessListener { fetchPhotoResponse ->
                    continuation.resume(fetchPhotoResponse.bitmap)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
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
