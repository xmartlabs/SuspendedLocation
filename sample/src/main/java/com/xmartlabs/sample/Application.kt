package com.xmartlabs.sample

import android.app.Application
import com.xmartlabs.suspendedlocation.places.SuspendedPlaces

class Application : Application() {

  override fun onCreate() {
    super.onCreate()

    SuspendedPlaces.initialize(this)
  }
}
