package com.example.nearhelp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.nearhelp.ui.incident.IncidentScreen

/** Rescue directions and real responder status are presented by the shared incident UI. */
@Composable
fun RescueNavigationScreen(onNavigateBack: () -> Unit, viewModel: RescueNavigationViewModel,
    incidentId: String = "", token: String? = null, modifier: Modifier = Modifier) {
    IncidentScreen(initialId = incidentId, onBack = onNavigateBack)
}
