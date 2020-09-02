package com.xmartlabs.suspendedlocation.places

import java.lang.RuntimeException

/*
* Places SDK must be initialized before using its functions for more info follow up this link
* https://developers.google.com/places/android-sdk/start#maps_places_get_started-kotlin
*
*  */
class PlacesNotInitializedException : RuntimeException("Places not initialized")
