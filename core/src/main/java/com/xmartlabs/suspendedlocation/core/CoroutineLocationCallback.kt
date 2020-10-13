package com.xmartlabs.suspendedlocation.core

import android.location.Location
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class CoroutineLocationCallback(
    val scope: CoroutineScope,
    val continuation: Continuation<Location>,
    val runnerContext: CoroutineContext
) : LocationCallback() {
  override fun onLocationResult(locationResult: LocationResult) {
    runBlocking(runnerContext) {
      continuation.resume(locationResult.lastLocation)
    }
  }

  override fun onLocationAvailability(locationAvailability: LocationAvailability) {
    super.onLocationAvailability(locationAvailability)
    if (!locationAvailability.isLocationAvailable) {
      runBlocking(runnerContext) {
        continuation.resumeWithException(
            IOException("Location not available")
        )
      }
    }
  }
}
