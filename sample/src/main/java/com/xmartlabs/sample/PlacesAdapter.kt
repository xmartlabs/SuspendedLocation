package com.xmartlabs.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.android.synthetic.main.auto_complete_places_item.view.*

class PlacesAdapter : ListAdapter<AutocompletePrediction, PlacesAdapter.PlacesViewHolder>(DiffCallback()) {
  companion object {
    class DiffCallback : DiffUtil.ItemCallback<AutocompletePrediction>() {
      override fun areItemsTheSame(oldItem: AutocompletePrediction, newItem: AutocompletePrediction):
          Boolean = oldItem.placeId == newItem.placeId

      override fun areContentsTheSame(oldItem: AutocompletePrediction, newItem: AutocompletePrediction):
          Boolean = oldItem.getPrimaryText(null) == newItem.getPrimaryText(null)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder =
      PlacesViewHolder(LayoutInflater.from(parent.context)
          .inflate(R.layout.auto_complete_places_item, parent, false))

  override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) = holder.bind(getItem(position))

  inner class PlacesViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
    var autocompletePrediction: AutocompletePrediction? = null

    fun bind(autocompletePrediction: AutocompletePrediction) {
      this.autocompletePrediction = autocompletePrediction
      itemView.tvTitle.text = autocompletePrediction.getPrimaryText(null)
      itemView.tvSubTitle.text = autocompletePrediction.getSecondaryText(null)
    }
  }
}
