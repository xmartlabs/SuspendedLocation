package com.xmartlabs.suspendedlocation.places

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.RequiresPermission
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.xmartlabs.suspendedlocation.core.SuspendedLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class SuspendedPlacesImpl(
    internal var context: Context,
    internal var placesClient: PlacesClient? = null
){

  /**
   * A function that returns a [List] of [AutocompletePrediction] given a [FindAutocompletePredictionsRequest]
   *
   * @param autocompleteRequest The [FindAutocompletePredictionsRequest] to be made.
   * @param runnerContext The context in which the function will be executed.
   */
  internal suspend fun placesAutocomplete(
      autocompleteRequest: FindAutocompletePredictionsRequest,
      runnerContext: CoroutineContext
  ): List<AutocompletePrediction> = placesClient?.runWithPlaces(runnerContext) {
    findAutocompletePredictions(autocompleteRequest)
  }?.autocompletePredictions ?: listOf()

  /**
   * A function that returns a [List] of [PlaceLikelihood] given a [FindCurrentPlaceRequest]
   *
   * @param findCurrentPlaceRequest The [FindCurrentPlaceRequest] to be made.
   * @param runnerContext The context in which the function will be executed.
   */
  @RequiresPermission(
      anyOf = ["android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_WIFI_STATE"]
  )
  internal suspend fun findCurrentPlace(
      findCurrentPlaceRequest: FindCurrentPlaceRequest,
      runnerContext: CoroutineContext
  ): List<PlaceLikelihood> = placesClient?.runWithPlaces(runnerContext) {
    findCurrentPlace(findCurrentPlaceRequest)
  }?.placeLikelihoods ?: listOf()

  /**
   * A function that returns a [Place] given a [FetchPlaceRequest]
   *
   * @param fetchPlaceRequest The [FetchPlaceRequest] to be made.
   * @param runnerContext The context in which the function will be executed.
   */
  internal suspend fun fetchPlace(
      fetchPlaceRequest: FetchPlaceRequest,
      runnerContext: CoroutineContext
  ): Place? = placesClient?.runWithPlaces(runnerContext) {
    fetchPlace(fetchPlaceRequest)
  }?.place

  /**
   * A function that returns a [Bitmap] given a [FetchPhotoRequest]
   *
   * @param fetchPhotoRequest The [FetchPhotoRequest] to be made.
   * @param runnerContext The context in which the function will be executed.
   */
  internal suspend fun fetchPhoto(
      fetchPhotoRequest: FetchPhotoRequest,
      runnerContext: CoroutineContext
  ): Bitmap? = placesClient?.runWithPlaces(runnerContext) {
    fetchPhoto(fetchPhotoRequest)
  }?.bitmap

  private suspend inline fun <T> PlacesClient.runWithPlaces(
      runnerContext: CoroutineContext = Dispatchers.Default,
      crossinline function: PlacesClient.() -> Task<T>
  ): T = withContext(runnerContext) {
    with(this@runWithPlaces, function)
        .taskToCoroutine()
  }
}
