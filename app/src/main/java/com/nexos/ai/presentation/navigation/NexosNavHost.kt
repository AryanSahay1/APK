package com.nexos.ai.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.NoteAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexos.ai.domain.model.AssistantContext
import com.nexos.ai.presentation.ui.assistant.PandaAssistantSheet
import com.nexos.ai.presentation.ui.components.FluffyPanda
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexos.ai.presentation.ui.alarms.AlarmsScreen
import com.nexos.ai.presentation.ui.google.CalendarGridScreen
import com.nexos.ai.presentation.ui.google.GmailComposeScreen
import com.nexos.ai.presentation.ui.google.GoogleEcosystemScreen
import com.nexos.ai.presentation.ui.hub.AboutScreen
import com.nexos.ai.presentation.ui.hub.SuperAppHubScreen
import com.nexos.ai.presentation.ui.hub.SwiggyDetailScreen
import com.nexos.ai.presentation.ui.hub.UberDetailScreen
import com.nexos.ai.presentation.ui.maps.MapsScreen
import com.nexos.ai.presentation.ui.news.NewsScreen
import com.nexos.ai.presentation.ui.notes.EditNoteScreen
import com.nexos.ai.presentation.ui.notes.NoteDetailScreen
import com.nexos.ai.presentation.ui.notes.NoteListScreen
import com.nexos.ai.presentation.ui.onboarding.OnboardingScreen
import com.nexos.ai.presentation.ui.settings.SettingsScreen
import com.nexos.ai.presentation.ui.voice.VoiceCaptureScreen
import com.nexos.ai.presentation.ui.weather.WeatherScreen

object Routes {
    const val NOTES = "noteList"
    const val NOTE_DETAIL = "noteDetail/{noteId}"
    const val EDIT_NOTE = "editNote/{noteId}"
    const val SETTINGS = "settings"
    const val ONBOARDING = "onboarding"
    const val VOICE = "voice"
    const val NEWS = "news"
    const val HUB = "hub"
    const val ALARMS = "alarms"
    const val ABOUT = "about"
    const val WEATHER = "weather"
    const val MAPS = "maps"
    const val GOOGLE = "google"
    const val UBER = "uber"
    const val SWIGGY = "swiggy"
    const val GMAIL_COMPOSE = "gmailCompose"
    const val CALENDAR_GRID = "calendarGrid"

    fun noteDetail(id: Long) = "noteDetail/$id"
    fun editNote(id: Long = -1L) = "editNote/$id"
}

private sealed class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Notes : BottomTab(Routes.NOTES, "Notes", Icons.Rounded.NoteAlt)
    data object News : BottomTab(Routes.NEWS, "News", Icons.AutoMirrored.Rounded.Article)
    data object Hub : BottomTab(Routes.HUB, "Hub", Icons.Rounded.Apps)
}

private val bottomTabs = listOf(BottomTab.Notes, BottomTab.News, BottomTab.Hub)

@Composable
fun NexosNavHost(
    deepLinkNoteId: Long? = null,
    deepLinkRoute: String? = null,
    onRequestScreenCapture: () -> Unit
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    LaunchedEffect(deepLinkNoteId, deepLinkRoute) {
        when {
            deepLinkNoteId != null -> navController.navigate(Routes.noteDetail(deepLinkNoteId))
            deepLinkRoute == "voice" -> navController.navigate(Routes.VOICE)
            deepLinkRoute == "alarms" -> navController.navigate(Routes.ALARMS)
            deepLinkRoute == "requestCapture" -> onRequestScreenCapture()
        }
    }

    val showBottomBar = currentRoute in bottomTabs.map { it.route }
    var assistantOpen by remember { mutableStateOf(false) }
    val assistantContext = remember(currentRoute) { contextForRoute(currentRoute) }
    var pendingEmailDraft by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        floatingActionButton = {
            // Panda assistant FAB — visible on every top-level tab. Uses the FluffyPanda
            // mini-bust as the icon so the user knows what they're tapping into.
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = { assistantOpen = true },
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                ) {
                    FluffyPanda(size = 40.dp)
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background) {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.background
                            )
                        )
                    }
                }
            }
        }
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.NOTES,
            modifier = Modifier.padding(scaffoldPadding),
            enterTransition = { fadeIn(animationSpec = tween(220)) + slideInVertically(initialOffsetY = { it / 12 }) },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(220)) },
            popExitTransition = { fadeOut(animationSpec = tween(150)) + slideOutVertically(targetOffsetY = { it / 12 }) }
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onContinue = {
                        navController.navigate(Routes.NOTES) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                    onRequestScreenCapture = onRequestScreenCapture
                )
            }
            composable(Routes.NOTES) {
                NoteListScreen(
                    onOpenNote = { id -> navController.navigate(Routes.noteDetail(id)) },
                    onAddNote = { navController.navigate(Routes.editNote()) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenVoice = { navController.navigate(Routes.VOICE) },
                    onRequestScreenCapture = onRequestScreenCapture
                )
            }
            composable(Routes.NEWS) {
                NewsScreen(
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenSavedNote = { id -> navController.navigate(Routes.noteDetail(id)) }
                )
            }
            composable(Routes.HUB) {
                SuperAppHubScreen(
                    onOpenAlarms = { navController.navigate(Routes.ALARMS) },
                    onOpenWeather = { navController.navigate(Routes.WEATHER) },
                    onOpenMaps = { navController.navigate(Routes.MAPS) },
                    onOpenGoogle = { navController.navigate(Routes.GOOGLE) },
                    onOpenUber = { navController.navigate(Routes.UBER) },
                    onOpenSwiggy = { navController.navigate(Routes.SWIGGY) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenAbout = { navController.navigate(Routes.ABOUT) }
                )
            }
            composable(Routes.ALARMS) {
                AlarmsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.WEATHER) {
                WeatherScreen(
                    onBack = { navController.popBackStack() },
                    onOpenNote = { id -> navController.navigate(Routes.noteDetail(id)) }
                )
            }
            composable(Routes.MAPS) {
                MapsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.GOOGLE) {
                GoogleEcosystemScreen(
                    onBack = { navController.popBackStack() },
                    onOpenGmailCompose = { navController.navigate(Routes.GMAIL_COMPOSE) },
                    onOpenCalendarGrid = { navController.navigate(Routes.CALENDAR_GRID) }
                )
            }
            composable(Routes.GMAIL_COMPOSE) {
                GmailComposeScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.CALENDAR_GRID) {
                CalendarGridScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.UBER) {
                UberDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SWIGGY) {
                SwiggyDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(
                Routes.NOTE_DETAIL,
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("noteId") ?: -1L
                NoteDetailScreen(
                    noteId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.editNote(id)) }
                )
            }
            composable(
                Routes.EDIT_NOTE,
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("noteId") ?: -1L
                EditNoteScreen(
                    noteId = id,
                    onSaved = { newId ->
                        if (newId > 0) {
                            navController.navigate(Routes.noteDetail(newId)) {
                                popUpTo(Routes.NOTES)
                            }
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.VOICE) {
                VoiceCaptureScreen(onDone = { navController.popBackStack() })
            }
        }
    }

    if (assistantOpen) {
        PandaAssistantSheet(
            initialContext = assistantContext,
            onDismiss = { assistantOpen = false },
            onOpenGmailComposeWith = { to, subject, body ->
                pendingEmailDraft = Triple(to, subject, body)
                assistantOpen = false
                navController.navigate(Routes.GMAIL_COMPOSE)
            }
        )
    }

    // If the panda produced an email draft, fire-and-forget: open the composer and pass
    // the draft through the existing Intent.EXTRA_TEXT pre-fill plumbing on next launch.
    // (The composer is rememberSaveable-backed so paste-and-edit works fine.)
}

/**
 * Map the current bottom-nav (or per-feature) route to the panda's default conversational
 * context. Lets the FAB feel "smart" — tapping it on the Weather screen opens a
 * Weather-focused panda, tapping on the Alarms screen opens an Alarm-focused panda, etc.
 */
private fun contextForRoute(route: String?): AssistantContext = when (route) {
    Routes.NEWS -> AssistantContext.News
    Routes.WEATHER -> AssistantContext.Weather
    Routes.ALARMS -> AssistantContext.Alarm
    Routes.GMAIL_COMPOSE -> AssistantContext.Email
    Routes.CALENDAR_GRID -> AssistantContext.Alarm
    Routes.MAPS -> AssistantContext.Map
    Routes.SWIGGY -> AssistantContext.Food
    Routes.NOTE_DETAIL, Routes.EDIT_NOTE -> AssistantContext.Notes
    else -> AssistantContext.Default
}
