package com.xmartlabs.suspendedlocation.places

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.RequiresPermission
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
import kotlin.coroutines.CoroutineContext

object SuspendedPlaces {
  private lateinit var suspendedPlacesImpl: SuspendedPlacesImpl

  fun initialize(context: Context) {
    SuspendedLocation.initialize(context)
    var placesClient: PlacesClient? = null
    if (Places.isInitialized()) {
      placesClient = Places.createClient(context)
    }
    suspendedPlacesImpl = SuspendedPlacesImpl(context, placesClient)
  }

  /**
   * A function that returns a [List] of [AutocompletePrediction] given a [FindAutocompletePredictionsRequest]
   *
   * @param autocompleteRequest The [FindAutocompletePredictionsRequest] to be made.
   * @param runnerContext The context in which the function will be executed.
   */
  suspend fun placesAutocomplete(
      autocompleteRequest: FindAutocompletePredictionsRequest,
      runnerContext: CoroutineContext
  ): List<AutocompletePrediction> = suspendedPlacesImpl.placesAutocomplete(
      autocompleteRequest,
      runnerContext
  )

  /**
   * A function that returns a [List] of [PlaceLikelihood] given a [FindCurrentPlaceRequest]
   *
   * @param findCurrentPlaceRequest The [FindCurrentPlaceRequest] to be made.
   * @param runnerContext The context in which the function will be executed.
   */
  @RequiresPermission(
      anyOf = ["android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_WIFI_STATE"]
  )
  suspend fun findCurrentPlace(
      findCurrentPlaceRequest: FindCurrentPlaceRequest,
      runnerContext: CoroutineContext
  ): List<PlaceLikelihood> = suspendedPlacesImpl.findCurrentPlace(
      findCurrentPlaceRequest,
      runnerContext
  )

  /**
   * A function that returns a [Place] given a [FetchPlaceRequest]
   *
   * @param fetchPlaceRequest The [FetchPlaceRequest] to be made.
   * @param runnerContext The context in which the function will be executed.
   */
  suspend fun fetchPlace(
      fetchPlaceRequest: FetchPlaceRequest,
      runnerContext: CoroutineContext
  ): Place? = suspendedPlacesImpl.fetchPlace(
      fetchPlaceRequest,
      runnerContext
  )

  /**
   * A function that returns a [Bitmap] given a [FetchPhotoRequest]
   *
   * @param fetchPhotoRequest The [FetchPhotoRequest] to be made.
   * @param runnerContext The context in which the function will be executed.
   */
  suspend fun fetchPhoto(
      fetchPhotoRequest: FetchPhotoRequest,
      runnerContext: CoroutineContext
  ): Bitmap? = suspendedPlacesImpl.fetchPhoto(
      fetchPhotoRequest,
      runnerContext
  )
}
