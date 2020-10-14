package com.xmartlabs.suspendedlocation.core

import android.content.Context
import android.location.Address
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import kotlin.coroutines.CoroutineContext

public object SuspendedLocation {
  private lateinit var suspendedLocationImpl: SuspendedLocationImpl

  public fun initialize(context: Context) {
    val locationServices = LocationServices.getFusedLocationProviderClient(context)
    suspendedLocationImpl = SuspendedLocationImpl(context, locationServices)
  }

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
  public fun requestCurrentLocation(
      locationRequest: LocationRequest,
      runnerContext: CoroutineContext = Dispatchers.Default
  ): Flow<Location> = suspendedLocationImpl.requestCurrentLocation(locationRequest, runnerContext)

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
  public suspend fun requestCurrentLocation(
      runnerContext: CoroutineContext = Dispatchers.Default
  ): Location = suspendedLocationImpl.requestCurrentLocation(runnerContext)

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
  public suspend fun reverseGeocode(
      location: LatLong,
      maxResults: Int,
      runnerContext: CoroutineContext = Dispatchers.Default
  ): List<Address> = suspendedLocationImpl.reverseGeocode(location, maxResults, runnerContext)

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
  public suspend fun geocode(
      locationName: String,
      maxResults: Int,
      runnerContext: CoroutineContext = Dispatchers.Default
  ): List<Address> = suspendedLocationImpl.geocode(locationName, maxResults, runnerContext)

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
  public suspend fun geocode(
      locationName: String,
      startingPoint: LatLong,
      endPoint: LatLong,
      maxResults: Int,
      runnerContext: CoroutineContext = Dispatchers.Default
  ): List<Address> = suspendedLocationImpl.geocode(
      locationName,
      startingPoint,
      endPoint,
      maxResults,
      runnerContext
  )
}
