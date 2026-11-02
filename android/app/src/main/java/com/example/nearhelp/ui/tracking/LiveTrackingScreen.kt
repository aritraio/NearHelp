package com.example.nearhelp.ui.tracking

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.nearhelp.ui.incident.IncidentScreen

/** Older callers share the production incident map, chat and arrival flow. */
@Composable
fun LiveTrackingScreen(onNavigateBack: () -> Unit, onNavigateToNavigation: () -> Unit = {},
    viewModel: LiveTrackingViewModel, incidentId: String = "", token: String? = null, modifier: Modifier = Modifier) {
    IncidentScreen(initialId = incidentId, onBack = onNavigateBack)
}
