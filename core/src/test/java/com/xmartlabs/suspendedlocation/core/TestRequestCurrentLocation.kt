package com.xmartlabs.suspendedlocation.core

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Task
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkClass
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@ExperimentalCoroutinesApi
class TestRequestCurrentLocation {
  private val testDispatcher = TestCoroutineDispatcher()

  @MockK
  lateinit var context: Context

  @MockK
  lateinit var task: Task<Void>

  private lateinit var suspendedLocationImpl: SuspendedLocationImpl
  private lateinit var flowLocationCallback: FlowLocationCallback

  val locationResult1 = mockkClass(LocationResult::class)
  val locationResult2 = mockkClass(LocationResult::class)
  val locationResult3 = mockkClass(LocationResult::class)
  val locationResultError = mockkClass(LocationAvailability::class)

  val locationResultList = listOf(locationResult1, locationResult2, locationResult3)
  val location1 = mockkClass(Location::class)
  val location2 = mockkClass(Location::class)
  val location3 = mockkClass(Location::class)

  val locationServices = mockkClass(FusedLocationProviderClient::class)

  @Before
  fun setup() {
    MockKAnnotations.init(this, relaxUnitFun = true)
    every {
      locationServices.requestLocationUpdates(
          LocationRequest.create(),
          any(),
          any()
      )
    } returns task

    every {
      locationResult1.lastLocation
    } returns location1
    every {
      locationResult2.lastLocation
    } returns location2
    every {
      locationResult3.lastLocation
    } returns location3
    every {
      locationResultError.isLocationAvailable
    } returns false

    suspendedLocationImpl = SuspendedLocationImpl(context, locationServices)
  }

  @Test
  fun `returns one item correctly`() = runBlockingTest {
    val flow = callbackFlow<Location> {
      flowLocationCallback = FlowLocationCallback(this, testDispatcher)
      locationServices.requestLocationUpdates(
          LocationRequest.create(),
          flowLocationCallback,
          mockkClass(Looper::class)
      )
      flowLocationCallback.onLocationResult(locationResult1)
      awaitCancellation()
    }
    flow.first {
      assertTrue("Locations should be the same", locationResult1.lastLocation == it)
      true
    }
  }

  @Test
  fun `returns multiple item correctly`() = runBlockingTest {
    val flow = callbackFlow<Location> {
      flowLocationCallback = FlowLocationCallback(this, testDispatcher)
      locationServices.requestLocationUpdates(
          LocationRequest.create(),
          flowLocationCallback,
          mockkClass(Looper::class)
      )
      flowLocationCallback.onLocationResult(locationResult1)
      flowLocationCallback.onLocationResult(locationResult2)
      flowLocationCallback.onLocationResult(locationResult3)
      awaitCancellation()
    }
    flow.take(3).collectIndexed { index, value ->
      assertTrue("Locations should be the same", value == locationResultList[index].lastLocation)
    }
  }

  @Test(expected = IOException::class)
  fun `returns error correctly`() = runBlocking {
    val flow = callbackFlow<Location> {
      flowLocationCallback = FlowLocationCallback(this, testDispatcher)
      locationServices.requestLocationUpdates(
          LocationRequest.create(),
          flowLocationCallback,
          mockkClass(Looper::class)
      )
      flowLocationCallback.onLocationAvailability(locationAvailability = locationResultError)

      awaitClose()
    }
    flow.catch { throw it.cause!!.cause!! }.first()
    Unit
  }
}
