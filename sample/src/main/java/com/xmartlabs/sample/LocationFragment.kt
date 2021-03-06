package com.xmartlabs.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationRequest
import com.xmartlabs.sample.databinding.LocationFragmentBinding
import com.xmartlabs.suspendedlocation.core.LatLong
import com.xmartlabs.suspendedlocation.core.SuspendedLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch

class LocationFragment : Fragment() {
  companion object {
    const val NUMBER_OF_REQUEST = 5
    const val INTERVAL = 300L
  }

  lateinit var viewBinding: LocationFragmentBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
      LocationFragmentBinding.inflate(layoutInflater, container, false).let {
        viewBinding = it
        it.root
      }

  @SuppressLint("MissingPermission")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    with(viewBinding) {
      btnGeocoding.isEnabled = false
      btnReverseGeocoding.isEnabled = false

      addCurrentLocation()
      addCurrentLocationMultipleTries()
      addGeocoding()
      addReverseGeocoding()
    }
  }

  @SuppressLint("MissingPermission")
  private fun LocationFragmentBinding.addCurrentLocationMultipleTries() {
    btnCurrentLocationMultiple.setOnClickListener {
      lifecycleScope.launch() {
        SuspendedLocation.requestCurrentLocation(
            LocationRequest().setNumUpdates(NUMBER_OF_REQUEST).setInterval(INTERVAL),
            Dispatchers.IO
        ).collectIndexed { index, location ->
          Toast.makeText(
              context,
              "${index + 1} / 5 - Lat: ${location.latitude} Long: ${location.longitude}",
              Toast.LENGTH_SHORT
          ).show()
        }
      }
    }
  }

  @SuppressLint("MissingPermission")
  private fun LocationFragmentBinding.addCurrentLocation() {
    btnCurrentLocationSingle.setOnClickListener {
      lifecycleScope.launch() {
        val location = SuspendedLocation.requestCurrentLocation(Dispatchers.IO)
        Toast.makeText(context, "Lat: ${location.latitude} Long: ${location.longitude}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun LocationFragmentBinding.addGeocoding() {
    etGeocoding.addTextChangedListener { text ->
      btnGeocoding.isEnabled = text?.isNotEmpty() ?: false
    }

    btnGeocoding.setOnClickListener {
      lifecycleScope.launch() {
        val location = SuspendedLocation.geocode(etGeocoding.text.toString(), 1, Dispatchers.IO).firstOrNull()
        Toast.makeText(context, location?.getAddressLine(0), Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun LocationFragmentBinding.addReverseGeocoding() {
    etGeocodingLat.addTextChangedListener { text ->
      btnReverseGeocoding.isEnabled = (text?.isNotEmpty()
          ?: false) && etGeocodingLng.text.isNotEmpty()
    }
    etGeocodingLng.addTextChangedListener { text ->
      btnReverseGeocoding.isEnabled = (text?.isNotEmpty()
          ?: false) && etGeocodingLat.text.isNotEmpty()
    }

    btnReverseGeocoding.setOnClickListener {
      lifecycleScope.launch {
        val location = SuspendedLocation.reverseGeocode(
            LatLong(
                etGeocodingLat.text.toString().toDouble(),
                etGeocodingLng.text.toString().toDouble()
            ),
            1,
            Dispatchers.IO
        ).firstOrNull()
        Toast.makeText(context, location?.getAddressLine(0), Toast.LENGTH_SHORT).show()
      }
    }
  }
}
