package com.xmartlabs.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.xmartlabs.suspendedlocation.places.SuspendedPlaces
import kotlinx.android.synthetic.main.places_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlacesFragment : Fragment() {
  val adapter = PlacesAdapter()
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
      layoutInflater.inflate(R.layout.places_fragment, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    etSearch.addTextChangedListener {
      btnSearch.isEnabled = it?.isNotEmpty() ?: false
    }

    btnSearch.setOnClickListener {
      lifecycleScope.launch {
        val placesList = SuspendedPlaces.placesAutocomplete(
            FindAutocompletePredictionsRequest.builder().setQuery(etSearch.text.toString()).build(),
            Dispatchers.IO
        )
        adapter.submitList(placesList)
      }
    }
  }
}
