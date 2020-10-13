package com.xmartlabs.suspendedlocation.core

import android.location.Location
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
internal class FlowLocationCallback(
    val producerScope: ProducerScope<Location>,
    val runnerContext: CoroutineContext
) : LocationCallback() {
  @Synchronized
  override fun onLocationResult(locationResult: LocationResult) {
    runBlocking(runnerContext) {
      producerScope.send(locationResult.lastLocation)
    }
  }

  override fun onLocationAvailability(locationAvailability: LocationAvailability) {
    super.onLocationAvailability(locationAvailability)
    runBlocking(runnerContext) {
      if (!locationAvailability.isLocationAvailable) {
        producerScope.cancel(
            "Location not available",
            IOException("Location not available")
        )
      }
    }
  }
}
