package com.nexos.ai.presentation.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.domain.model.WeatherCondition
import com.nexos.ai.presentation.viewmodel.WeatherStripUiState
import com.nexos.ai.presentation.viewmodel.WeatherStripViewModel

/**
 * Live weather strip pinned at the top of the Notes home — sits above the panda mascot per
 * the user's request: "always showing the real time update above panda on the main page".
 *
 * - Uses the device's coarse location when granted; falls back to last-saved city otherwise.
 * - Refreshes every 10 minutes from [WeatherStripViewModel]; tap to refresh on-demand.
 * - Shows a small inline "Use my location" button when permission isn't granted yet.
 *
 * Visually: a rounded surface chip with the weather emoji, current °C, condition label, and
 * the location source as a sub-line. Subtle, single-row, doesn't dominate the screen.
 */
@Composable
fun WeatherStrip(
    modifier: Modifier = Modifier,
    viewModel: WeatherStripViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> viewModel.refresh() }

    LaunchedEffect(Unit) {
        // Re-trigger a refresh on first composition in case the permission was granted
        // outside our launcher (e.g. via Settings).
        viewModel.refresh()
    }

    val snapshot = state.snapshot
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = snapshot?.currentCode?.emoji ?: WeatherCondition.Unknown.emoji,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when {
                    state.isLoading && snapshot == null -> "Reading weather…"
                    snapshot == null -> "Weather unavailable"
                    else -> "%.1f°C · %s".format(snapshot.currentTempC, snapshot.currentCode.label)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when {
                    snapshot != null && state.source == WeatherStripUiState.Source.DeviceLocation ->
                        "Live · ${snapshot.locationLabel}"
                    snapshot != null ->
                        "Saved city · ${snapshot.locationLabel}"
                    !state.hasLocationPermission ->
                        "Grant location to see live weather here"
                    else -> "Tap to retry"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!state.hasLocationPermission && snapshot != null) {
            TextButton(onClick = {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }) {
                Text("Use my location")
            }
        }
    }
}
