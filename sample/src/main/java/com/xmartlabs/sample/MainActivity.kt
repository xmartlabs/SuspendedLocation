package com.xmartlabs.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.root.*

class MainActivity : AppCompatActivity() {
  companion object {
    const val REQUEST_LOCATION_PERMISSION_CODE = 15
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.root)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setDisplayShowHomeEnabled(true)
    btnLocations.setOnClickListener {
      checkLocationPermissions {
        goToLocationFragment()
      }
    }
  }

  private fun checkLocationPermissions(action: () -> Unit) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      action.invoke()
    } else {
      ActivityCompat.requestPermissions(
          this,
          arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
          REQUEST_LOCATION_PERMISSION_CODE
      )
    }
  }

  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray
  ) {
    when (requestCode) {
      REQUEST_LOCATION_PERMISSION_CODE -> {
        // If request is cancelled, the result arrays are empty.
        if ((grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
          goToLocationFragment()
        } else {
          Toast.makeText(this, "You must accept permissions", Toast.LENGTH_SHORT).show()
        }
        return
      }
      else -> Unit
    }
  }

  private fun goToLocationFragment() = supportFragmentManager.beginTransaction()
      .add(R.id.flRoot, LocationFragment(), LocationFragment::javaClass.name)
      .addToBackStack(LocationFragment::javaClass.name)
      .commit()
}
