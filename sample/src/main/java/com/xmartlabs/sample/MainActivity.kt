package com.xmartlabs.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.btnLocations
import kotlinx.android.synthetic.main.root.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.root)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setDisplayShowHomeEnabled(true)
    btnLocations.setOnClickListener { goToLocationFragment() }
    btnPlaces.setOnClickListener { goToPlacesFragment() }
  }

  fun goToPlacesFragment() = supportFragmentManager.beginTransaction()
      .add(R.id.flRoot, PlacesFragment(), PlacesFragment::javaClass.name)
      .addToBackStack(PlacesFragment::javaClass.name)
      .commit()

  fun goToLocationFragment() = supportFragmentManager.beginTransaction()
      .add(R.id.flRoot, LocationFragment(), LocationFragment::javaClass.name)
      .addToBackStack(LocationFragment::javaClass.name)
      .commit()
}
