package com.xmartlabs.suspendedlocation.core

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class SuspendedLocationImpl(
    internal var context: Context,
    internal var locationServices: FusedLocationProviderClient
) {

  /**
   * A function that returns a [Flow] that emits your current [Location] given a [LocationRequest]
   *
   * @param locationRequest The [LocationRequest] to be called.
   * @param runnerContext The context in which the function will be executed.
   *
   * @throws IOException if the location is not available
   */
  @ExperimentalCoroutinesApi
  @RequiresPermission(
      anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"]
  )
  internal fun requestCurrentLocation(
      locationRequest: LocationRequest,
      runnerContext: CoroutineContext = Dispatchers.Default
  ): Flow<Location> = callbackFlow<Location> {
    locationServices
        .requestLocationUpdates(
            locationRequest,
            FlowLocationCallback(this, coroutineContext),
            locationServices.looper
        )
    awaitClose { cancel() }
  }.flowOn(runnerContext)

  /**
   * A function that returns your current [Location]
   *
   * @param runnerContext The context in which the function will be executed.
   *
   * @throws IOException if the location is not available
   */
  @RequiresPermission(
      anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"]
  )
  internal suspend fun requestCurrentLocation(
      runnerContext: CoroutineContext = Dispatchers.Default
  ): Location = withContext(runnerContext) {
    suspendCoroutine { continuation: Continuation<Location> ->
      locationServices
          .requestLocationUpdates(
              LocationRequest().setInterval(1),
              CoroutineLocationCallback(this, continuation, runnerContext),
              locationServices.looper
          )
    }
  }

  /**
   * A function that returns a [List] of [Address] given a latitude and longitude
   *
   * @param locationName The name of the location to be requested.
   * @param maxResults Indicates the maximum amount of [Address] that will be returned.
   * @param runnerContext The context in which the function will be executed.
   *
   *
   * @throws IllegalArgumentException if latitude is
   * less than -90 or greater than 90
   * @throws IllegalArgumentException if longitude is
   * less than -180 or greater than 180
   * @throws IOException if the network is unavailable or any other
   * I/O problem occurs
   */
  internal suspend fun reverseGeocode(
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

  /**
   * A function that returns a [List] of [Address] given a location name
   *
   * @param locationName The name of the location to be requested.
   * @param maxResults Indicates the maximum amount of [Address] that will be returned.
   * @param runnerContext The context in which the function will be executed.
   *
   * @throws IllegalArgumentException if locationName is null
   * @throws IOException if the network is unavailable or any other
   * I/O problem occurs
   */
  internal suspend fun geocode(
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

  /**
   * A function that returns a [List] of [Address] given a location name an starting and end point
   *
   * @param locationName The name of the location to be requested.
   * @param startingPoint The starting point represented by a [LatLong].
   * @param endPoint The ending point represented by a [LatLong].
   * @param maxResults Indicates the maximum amount of [Address] that will be returned.
   * @param runnerContext The context in which the function will be executed.
   *
   * @throws IllegalArgumentException if locationName is null
   * @throws IllegalArgumentException if any latitude is
   * less than -90 or greater than 90
   * @throws IllegalArgumentException if any longitude is
   * less than -180 or greater than 180
   * @throws IOException if the network is unavailable or any other
   * I/O problem occurs
   */
  internal suspend fun geocode(
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
