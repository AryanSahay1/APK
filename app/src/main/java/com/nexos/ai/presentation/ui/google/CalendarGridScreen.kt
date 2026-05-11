package com.nexos.ai.presentation.ui.google

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexos.ai.presentation.ui.components.PandaMascot
import com.nexos.ai.presentation.ui.components.PandaMotion
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Month-view calendar with tappable day boxes. Tapping a box opens a dialog where the user
 * fills in title / start time / duration / notes; pressing "Add to phone calendar" hands the
 * event off via [CalendarContract.Events.CONTENT_URI] to the system Calendar app (whichever
 * the user has installed — Google Calendar, Samsung Calendar, etc.).
 *
 * No SDK dependency: this works with any CalendarProvider-compliant app, with no SCOPED
 * permission grant required — the Insert intent surface handles the user confirmation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarGridScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var displayedMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var dayEditing by remember { mutableStateOf<Calendar?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PandaMascot(size = 28.dp, hasLeaf = false, motion = PandaMotion.Bouncing)
                        Spacer(Modifier.width(10.dp))
                        Text("Calendar", fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MonthHeader(
                month = displayedMonth,
                onPrev = {
                    displayedMonth = (displayedMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                },
                onNext = {
                    displayedMonth = (displayedMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                }
            )

            // Weekday labels (Sun-Sat)
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
                    Text(
                        d,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            val cells = remember(displayedMonth.timeInMillis) { buildMonthCells(displayedMonth) }
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(cells, key = { it.key }) { cell ->
                    DayBox(
                        cell = cell,
                        onClick = { c -> dayEditing = c }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Tap a day to add an event. NexOS hands the event off to your phone's Calendar app — we never store it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    dayEditing?.let { day ->
        AddEventDialog(
            day = day,
            onCancel = { dayEditing = null },
            onSubmit = { title, startHour, startMinute, durationMin, notes ->
                dayEditing = null
                launchCalendarInsert(
                    context = context,
                    day = day,
                    title = title,
                    startHour = startHour,
                    startMinute = startMinute,
                    durationMin = durationMin,
                    notes = notes
                )
            }
        )
    }
}

@Composable
private fun MonthHeader(month: Calendar, onPrev: () -> Unit, onNext: () -> Unit) {
    val label = remember(month.timeInMillis) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(month.time)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous month")
        }
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = "Next month")
        }
    }
}

private data class MonthCell(
    val key: String,
    val label: String,
    val date: Calendar?,
    val isToday: Boolean,
    val isCurrentMonth: Boolean
)

private fun buildMonthCells(reference: Calendar): List<MonthCell> {
    val month = (reference.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val firstDayOfWeek = month.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
    val daysInMonth = month.getActualMaximum(Calendar.DAY_OF_MONTH)

    val today = Calendar.getInstance()
    val cells = mutableListOf<MonthCell>()

    // Leading blanks (alignment to Sunday-first grid)
    val leading = firstDayOfWeek - 1
    repeat(leading) { i -> cells += MonthCell("blank-pre-$i", "", null, false, false) }

    for (d in 1..daysInMonth) {
        val cal = (month.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, d) }
        val isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        cells += MonthCell(
            key = "day-${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-$d",
            label = d.toString(),
            date = cal,
            isToday = isToday,
            isCurrentMonth = true
        )
    }
    // Trailing blanks so the grid is always a multiple of 7
    while (cells.size % 7 != 0) {
        cells += MonthCell("blank-post-${cells.size}", "", null, false, false)
    }
    return cells
}

@Composable
private fun DayBox(cell: MonthCell, onClick: (Calendar) -> Unit) {
    val baseColor = if (cell.isToday) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surface
    val textColor = if (cell.isToday) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (cell.date == null) MaterialTheme.colorScheme.background else baseColor)
            .border(
                width = if (cell.isToday) 0.dp else 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .let { mod -> if (cell.date != null) mod.clickable { onClick(cell.date) } else mod },
        contentAlignment = Alignment.Center
    ) {
        if (cell.date != null) {
            Text(
                cell.label,
                style = MaterialTheme.typography.titleSmall,
                color = textColor,
                fontWeight = if (cell.isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun AddEventDialog(
    day: Calendar,
    onCancel: () -> Unit,
    onSubmit: (title: String, hour: Int, minute: Int, durationMin: Int, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf(9) }
    var minute by remember { mutableStateOf(0) }
    var durationMin by remember { mutableStateOf(60) }
    var notes by remember { mutableStateOf("") }
    val dateLabel = remember(day.timeInMillis) {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(day.time)
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Event on $dateLabel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = "%02d".format(hour),
                        onValueChange = { v -> v.toIntOrNull()?.let { if (it in 0..23) hour = it } },
                        label = { Text("Hour") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = "%02d".format(minute),
                        onValueChange = { v -> v.toIntOrNull()?.let { if (it in 0..59) minute = it } },
                        label = { Text("Min") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = durationMin.toString(),
                        onValueChange = { v -> v.toIntOrNull()?.let { if (it in 5..720) durationMin = it } },
                        label = { Text("Min long") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth().height(96.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(title.ifBlank { "Untitled event" }, hour, minute, durationMin, notes) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Add to phone calendar") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}

private fun launchCalendarInsert(
    context: android.content.Context,
    day: Calendar,
    title: String,
    startHour: Int,
    startMinute: Int,
    durationMin: Int,
    notes: String
) {
    val start = (day.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, startHour)
        set(Calendar.MINUTE, startMinute)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val end = (start.clone() as Calendar).apply { add(Calendar.MINUTE, durationMin) }

    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.timeInMillis)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.timeInMillis)
        if (notes.isNotBlank()) putExtra(CalendarContract.Events.DESCRIPTION, notes)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}
