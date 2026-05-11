package com.nexos.ai.presentation.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexos.ai.presentation.ui.components.PandaConfetti
import com.nexos.ai.presentation.ui.components.PandaMascot
import com.nexos.ai.presentation.ui.components.PandaMotion
import com.nexos.ai.util.DeepLinks

/**
 * The Hub: every Phase-4 + v1.1 integration in one screen. Tiles are grouped visually by
 * category (Information, Lifestyle, Transport, Food, Settings). Each tile routes to a
 * dedicated detail screen where possible, falling back to a public deep link.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAppHubScreen(
    onOpenAlarms: () -> Unit,
    onOpenWeather: () -> Unit,
    onOpenMaps: () -> Unit,
    onOpenGoogle: () -> Unit,
    onOpenUber: () -> Unit,
    onOpenSwiggy: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val context = LocalContext.current
    var rapidoPrompt by remember { mutableStateOf(false) }
    var zomatoPrompt by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PandaMascot(size = 30.dp, hasLeaf = true, motion = PandaMotion.Wave)
                        Spacer(Modifier.width(10.dp))
                        Text("Hub", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        val tiles = listOf(
            // — Information —
            HubTile("Weather", "Forecast + 7 days", Icons.Rounded.Cloud,
                accent = Color(0xFF4DA6FF), accentBg = Color(0x334DA6FF)) { onOpenWeather() },
            HubTile("Maps", "Find anything near you", Icons.Rounded.Map,
                accent = Color(0xFF34A853), accentBg = Color(0x3334A853)) { onOpenMaps() },
            HubTile("Google", "Gmail · Calendar · Drive", Icons.Rounded.Public,
                accent = Color(0xFFEA4335), accentBg = Color(0x33EA4335)) { onOpenGoogle() },
            HubTile("Alarms & reminders", "Natural-language scheduler", Icons.Rounded.Alarm,
                accent = Color(0xFF00E676), accentBg = Color(0x3300E676)) { onOpenAlarms() },

            // — Transport —
            HubTile("Uber", "Book a ride", Icons.Rounded.DirectionsCar,
                accent = Color(0xFF000000), accentBg = Color(0x331A1A1A)) { onOpenUber() },
            HubTile("Rapido", "Bike ride, fast", Icons.AutoMirrored.Rounded.DirectionsBike,
                accent = Color(0xFFFFC107), accentBg = Color(0x33FFC107)) {
                rapidoPrompt = true
            },

            // — Food —
            HubTile("Swiggy", "Delivery in minutes", Icons.Rounded.LocalDining,
                accent = Color(0xFFFC8019), accentBg = Color(0x33FC8019)) { onOpenSwiggy() },
            HubTile("Zomato", "Find food near you", Icons.Rounded.RestaurantMenu,
                accent = Color(0xFFE23744), accentBg = Color(0x33E23744)) {
                zomatoPrompt = true
            },

            // — Settings —
            HubTile("Settings", "AI, news, theme, floating button", Icons.Rounded.Settings,
                accent = Color(0xFF4DA6FF), accentBg = Color(0x334DA6FF)) { onOpenSettings() },
            HubTile("About & privacy", "How NexOS handles your data", Icons.Rounded.Info,
                accent = Color(0xFF9C7BFF), accentBg = Color(0x339C7BFF)) { onOpenAbout() }
        )
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Background panda confetti — 20 random variants scattered evenly across the
            // whole screen. Sits behind the tile grid; never receives touches.
            PandaConfetti(count = 20, seed = 7, pandaSize = 26.dp)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tiles, key = { it.title }) { tile -> TileCard(tile = tile) }
            }
        }
    }

    if (rapidoPrompt) {
        DeepLinkQuickPrompt(
            title = "Open Rapido",
            placeholder = "Destination address",
            onCancel = { rapidoPrompt = false },
            onConfirm = { value ->
                rapidoPrompt = false
                if (value.isNotBlank()) DeepLinks.launchRapido(context, value)
            }
        )
    }
    if (zomatoPrompt) {
        DeepLinkQuickPrompt(
            title = "Open Zomato",
            placeholder = "Search (e.g. biryani)",
            onCancel = { zomatoPrompt = false },
            onConfirm = { value ->
                zomatoPrompt = false
                if (value.isNotBlank()) DeepLinks.launchZomato(context, value)
            }
        )
    }
}

private data class HubTile(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val accentBg: Color = Color(0x3300E676),
    val onClick: () -> Unit
)

@Composable
private fun TileCard(tile: HubTile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .clickable(onClick = tile.onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tile.accentBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(tile.icon, contentDescription = null, tint = tile.accent, modifier = Modifier.size(24.dp))
        }
        Text(
            tile.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            tile.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeepLinkQuickPrompt(
    title: String,
    placeholder: String,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    "NexOS will hand off to the chosen app. If it's not installed, you'll be taken to its Play Store listing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = { onConfirm(input) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Open") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onCancel) { Text("Cancel") }
        }
    )
}
