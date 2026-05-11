package com.nexos.ai.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.NoteAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexos.ai.presentation.ui.alarms.AlarmsScreen
import com.nexos.ai.presentation.ui.hub.AboutScreen
import com.nexos.ai.presentation.ui.hub.SuperAppHubScreen
import com.nexos.ai.presentation.ui.news.NewsScreen
import com.nexos.ai.presentation.ui.notes.EditNoteScreen
import com.nexos.ai.presentation.ui.notes.NoteDetailScreen
import com.nexos.ai.presentation.ui.notes.NoteListScreen
import com.nexos.ai.presentation.ui.onboarding.OnboardingScreen
import com.nexos.ai.presentation.ui.settings.SettingsScreen
import com.nexos.ai.presentation.ui.theme.NexosBackground
import com.nexos.ai.presentation.ui.theme.NexosPrimary
import com.nexos.ai.presentation.ui.voice.VoiceCaptureScreen

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

    Scaffold(
        containerColor = NexosBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = NexosBackground) {
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
                                selectedIconColor = NexosPrimary,
                                selectedTextColor = NexosPrimary,
                                indicatorColor = NexosBackground
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
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenAbout = { navController.navigate(Routes.ABOUT) }
                )
            }
            composable(Routes.ALARMS) {
                AlarmsScreen(onBack = { navController.popBackStack() })
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
}
