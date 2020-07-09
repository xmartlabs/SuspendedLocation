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
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

object SuspendedPlaces {
    lateinit var context: Context
    private var placesClient: PlacesClient? = null

    fun initialize(context: Context) {
        SuspendedPlaces.context = context

        SuspendedLocation.initialize(context)

        if (Places.isInitialized()) {
            placesClient = Places.createClient(context)
        }
    }

    suspend fun placesAutocomplete(
        autocompleteRequest: FindAutocompletePredictionsRequest,
        runnerContext: CoroutineContext
    ): List<AutocompletePrediction> = placesClient.runWithPlaces { placesClient ->
        withContext(runnerContext) {
            placesClient.findAutocompletePredictions(autocompleteRequest)
                .taskToCoroutine()
                .autocompletePredictions
        }
    }

    @RequiresPermission(
        anyOf = ["android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_WIFI_STATE"]
    )
    suspend fun findCurrentPlace(
        findCurrentPlaceRequest: FindCurrentPlaceRequest,
        runnerContext: CoroutineContext
    ): List<PlaceLikelihood> = placesClient.runWithPlaces { placesClient ->
        withContext(runnerContext) {
            placesClient.findCurrentPlace(findCurrentPlaceRequest)
                .taskToCoroutine()
                .placeLikelihoods
        }
    }

    suspend fun fetchPlace(
        fetchPlaceRequest: FetchPlaceRequest,
        runnerContext: CoroutineContext
    ): Place = placesClient.runWithPlaces { placesClient ->
        withContext(runnerContext) {
            placesClient.fetchPlace(fetchPlaceRequest)
                .taskToCoroutine()
                .place
        }
    }

    suspend fun fetchPhoto(
        fetchPhotoRequest: FetchPhotoRequest,
        runnerContext: CoroutineContext
    ): Bitmap = placesClient.runWithPlaces { placesClient ->
        withContext(runnerContext) {
            placesClient.fetchPhoto(fetchPhotoRequest)
                .taskToCoroutine()
                .bitmap
        }
    }

    private inline fun <T> PlacesClient?.runWithPlaces(function: (PlacesClient) -> T): T =
        this?.let { function.invoke(it) } ?: throw PlacesNotInitializedException()
}