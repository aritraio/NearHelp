package com.example.nearhelp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.nearhelp.ui.incident.IncidentScreen

/** Compatibility destination; all SOS creation uses the same medical-only flow. */
@Composable
fun CrisisDispatchScreen(onCancel: () -> Unit, onDispatch: (String, String) -> Unit,
    onNavigateToMap: () -> Unit, onNavigateToAssistant: () -> Unit, modifier: Modifier = Modifier,
    initialCategory: String = "medical", defaultStreetAddress: String = "", defaultSubAddress: String = "") {
    IncidentScreen(createMode = true, onBack = onCancel)
}
