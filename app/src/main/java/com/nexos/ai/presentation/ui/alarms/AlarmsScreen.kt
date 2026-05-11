package com.nexos.ai.presentation.ui.alarms

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexos.ai.data.local.entity.Alarm
import com.nexos.ai.presentation.ui.components.EmptyState
import com.nexos.ai.presentation.viewmodel.AlarmsViewModel
import com.nexos.ai.presentation.ui.theme.NexosBackground
import com.nexos.ai.presentation.ui.theme.NexosBorder
import com.nexos.ai.presentation.ui.theme.NexosPrimary
import com.nexos.ai.presentation.ui.theme.NexosPrimarySoft
import com.nexos.ai.util.toFormattedDateTime
import com.nexos.ai.util.toRelativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    onBack: () -> Unit,
    viewModel: AlarmsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = NexosBackground,
        topBar = {
            TopAppBar(
                title = { Text("Alarms & Reminders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexosBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!state.canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ExactAlarmBanner(onOpen = {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching { context.startActivity(intent) }
                    viewModel.refreshExactPermission()
                })
            }

            InputArea(
                input = state.input,
                preview = state.parsePreview,
                parseError = state.parseError,
                onChange = viewModel::onInputChange,
                onSubmit = viewModel::submit
            )

            if (alarms.isEmpty()) {
                EmptyState(
                    title = "No reminders yet",
                    subtitle = "Try \"remind me at 8am tomorrow\" or \"in 30 minutes call mom\".",
                    icon = Icons.Rounded.Alarm
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmRow(
                            alarm = alarm,
                            onToggle = { value -> viewModel.toggle(alarm, value) },
                            onDelete = { viewModel.delete(alarm) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InputArea(
    input: String,
    preview: com.nexos.ai.util.NaturalLanguageTimeParser.Result?,
    parseError: String?,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. remind me at 8am tomorrow") },
            singleLine = false,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = NexosBackground,
                unfocusedContainerColor = NexosBackground,
                focusedIndicatorColor = NexosPrimary,
                unfocusedIndicatorColor = NexosBorder,
                cursorColor = NexosPrimary
            )
        )
        when {
            preview != null -> {
                Text(
                    "Fires at ${preview.triggerAtMillis.toFormattedDateTime()} · " +
                        "${preview.triggerAtMillis.toRelativeTime().lowercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexosPrimary
                )
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NexosPrimary, contentColor = NexosBackground
                    )
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Schedule reminder", fontWeight = FontWeight.SemiBold)
                }
            }
            parseError != null -> {
                Text(
                    parseError,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AlarmRow(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isPast = alarm.isFired || alarm.triggerAt <= now
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(NexosPrimarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Alarm, contentDescription = null, tint = NexosPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                alarm.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (alarm.isEnabled && !isPast) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                alarm.triggerAt.toFormattedDateTime() +
                    if (isPast) " · fired" else " · " + alarm.triggerAt.toRelativeTime().lowercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = alarm.isEnabled && !isPast,
            onCheckedChange = onToggle,
            enabled = !isPast,
            colors = SwitchDefaults.colors(
                checkedTrackColor = NexosPrimary,
                checkedThumbColor = NexosBackground,
                uncheckedTrackColor = NexosBorder
            )
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
private fun ExactAlarmBanner(onOpen: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, NexosBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text("Exact alarms not allowed yet",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        Text(
            "Without exact-alarm permission, reminders may fire within ±10 minutes of the time you set.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onOpen) { Text("Grant exact alarm permission") }
    }
}
