package com.example.nearhelp.ui.map

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng

/** Map marker model representing an emergency facility or volunteer responder. */
data class MapPinItem(
  val id: String,
  val title: String,
  val snippet: String,
  val position: LatLng,
  val hueColor: Float = BitmapDescriptorFactory.HUE_RED,
  val isEmergencyIncident: Boolean = false,
)
