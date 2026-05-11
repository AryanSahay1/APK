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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexos.ai.presentation.ui.theme.NexosBackground
import com.nexos.ai.presentation.ui.theme.NexosBorder
import com.nexos.ai.presentation.ui.theme.NexosPrimary
import com.nexos.ai.presentation.ui.theme.NexosPrimarySoft
import com.nexos.ai.util.DeepLinks

/**
 * The Hub: every Phase-4 super-app integration in one screen. Each tile launches a public
 * deep link with a Play Store fallback when the target app isn't installed.
 *
 * No tile here calls any paid API — they all use the public URI schemes the destination apps
 * advertise. NexOS does not transmit user data to any server in service of these tiles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAppHubScreen(
    onOpenAlarms: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val context = LocalContext.current
    var promptKind by remember { mutableStateOf<HubPromptKind?>(null) }
    var input by remember { mutableStateOf("") }

    Scaffold(
        containerColor = NexosBackground,
        topBar = {
            TopAppBar(
                title = { Text("Hub", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexosBackground)
            )
        }
    ) { padding ->
        val tiles = listOf(
            HubTile("Alarms & reminders", "Natural language scheduler", Icons.Rounded.Alarm) { onOpenAlarms() },
            HubTile("Uber", "Book a ride to anywhere", Icons.Rounded.DirectionsCar) {
                promptKind = HubPromptKind.Uber
            },
            HubTile("Rapido", "Bike ride, fast", Icons.AutoMirrored.Rounded.DirectionsBike) {
                promptKind = HubPromptKind.Rapido
            },
            HubTile("Zomato", "Find food near you", Icons.Rounded.RestaurantMenu) {
                promptKind = HubPromptKind.Zomato
            },
            HubTile("Swiggy", "Delivery in minutes", Icons.Rounded.LocalDining) {
                promptKind = HubPromptKind.Swiggy
            },
            HubTile("Settings", "AI, news, floating button", Icons.Rounded.Settings) { onOpenSettings() },
            HubTile("About & privacy", "How NexOS handles your data", Icons.Rounded.Info) { onOpenAbout() }
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            items(tiles, key = { it.title }) { tile -> TileCard(tile = tile) }
        }
    }

    val current = promptKind
    if (current != null) {
        DeepLinkPromptDialog(
            kind = current,
            value = input,
            onValueChange = { input = it },
            onCancel = {
                promptKind = null
                input = ""
            },
            onConfirm = {
                when (current) {
                    HubPromptKind.Uber -> DeepLinks.launchUber(context, input.ifBlank { "home" })
                    HubPromptKind.Rapido -> DeepLinks.launchRapido(context, input.ifBlank { "home" })
                    HubPromptKind.Zomato -> DeepLinks.launchZomato(context, input)
                    HubPromptKind.Swiggy -> DeepLinks.launchSwiggy(context, input)
                }
                promptKind = null
                input = ""
            }
        )
    }
}

private data class HubTile(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

private enum class HubPromptKind { Uber, Rapido, Zomato, Swiggy }

@Composable
private fun TileCard(tile: HubTile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, NexosBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = tile.onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NexosPrimarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(tile.icon, contentDescription = null, tint = NexosPrimary, modifier = Modifier.size(22.dp))
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
private fun DeepLinkPromptDialog(
    kind: HubPromptKind,
    value: String,
    onValueChange: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val (title, hint) = when (kind) {
        HubPromptKind.Uber -> "Book Uber" to "Destination address"
        HubPromptKind.Rapido -> "Book Rapido" to "Destination address"
        HubPromptKind.Zomato -> "Open Zomato" to "Search query (e.g. biryani)"
        HubPromptKind.Swiggy -> "Open Swiggy" to "Search query (e.g. pizza)"
    }
    AlertDialog(
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
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(hint) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = NexosPrimary,
                        unfocusedIndicatorColor = NexosBorder,
                        cursorColor = NexosPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = NexosPrimary, contentColor = NexosBackground)
            ) { Text("Open") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}
