package com.xmartlabs.suspendedlocation.core

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkClass
import io.mockk.mockkConstructor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@ExperimentalCoroutinesApi
class TestReverseGeocode {
  private val testDispatcher = Dispatchers.IO

  @MockK
  lateinit var context: Context

  private lateinit var suspendedLocationImpl: SuspendedLocationImpl

  val address = mockkClass(Address::class)
  val address1 = mockkClass(Address::class)
  val address2 = mockkClass(Address::class)
  val address3 = mockkClass(Address::class)
  val address4 = mockkClass(Address::class)
  val expectedList = listOf(
      address,
      address1,
      address2,
      address3,
      address4
  )

  @Before
  fun setup() {
    MockKAnnotations.init(this, relaxUnitFun = true)

    every {
      address.featureName = "Something"
    }
    val locationServices = mockkClass(FusedLocationProviderClient::class)
    mockkConstructor(Geocoder::class)
    every { anyConstructed<Geocoder>().getFromLocation(50.0, 50.0, 5) } returns expectedList

    every { anyConstructed<Geocoder>().getFromLocation(50.0, 50.0, 1) } returns listOf(
        address
    )

    suspendedLocationImpl = SuspendedLocationImpl(context, locationServices)
  }

  @Test
  fun `returns one item correctly`() {
    runBlocking {
      val result = suspendedLocationImpl.reverseGeocode(
          LatLong(50.0, 50.0),
          1,
          testDispatcher
      )
      assert(result.size == 1 && result.first() == address)
    }
  }

  @Test
  fun `returns multiple items correctly`() {
    runBlocking {
      val result = suspendedLocationImpl.reverseGeocode(
          LatLong(50.0, 50.0),
          5,
          testDispatcher
      )
      assert(result.size == 5)
    }
  }

  @Test
  fun `returns multiple items in the same order`() {
    runBlocking {
      val result = suspendedLocationImpl.reverseGeocode(
          LatLong(50.0, 50.0),
          5,
          testDispatcher
      )
      assert(result.zip(expectedList).foldRight(true) { pair, acc ->
        acc && pair.first == pair.second
      })
    }
  }
}
