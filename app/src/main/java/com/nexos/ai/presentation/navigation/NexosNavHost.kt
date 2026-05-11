package com.nexos.ai.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexos.ai.presentation.ui.notes.EditNoteScreen
import com.nexos.ai.presentation.ui.notes.NoteDetailScreen
import com.nexos.ai.presentation.ui.notes.NoteListScreen
import com.nexos.ai.presentation.ui.onboarding.OnboardingScreen
import com.nexos.ai.presentation.ui.settings.SettingsScreen
import com.nexos.ai.presentation.ui.voice.VoiceCaptureScreen

object Routes {
    const val NOTES = "noteList"
    const val NOTE_DETAIL = "noteDetail/{noteId}"
    const val EDIT_NOTE = "editNote/{noteId}"
    const val SETTINGS = "settings"
    const val ONBOARDING = "onboarding"
    const val VOICE = "voice"

    fun noteDetail(id: Long) = "noteDetail/$id"
    fun editNote(id: Long = -1L) = "editNote/$id"
}

@Composable
fun NexosNavHost(
    deepLinkNoteId: Long? = null,
    deepLinkRoute: String? = null,
    onRequestScreenCapture: () -> Unit
) {
    val navController = rememberNavController()

    LaunchedEffect(deepLinkNoteId, deepLinkRoute) {
        when {
            deepLinkNoteId != null -> navController.navigate(Routes.noteDetail(deepLinkNoteId))
            deepLinkRoute == "voice" -> navController.navigate(Routes.VOICE)
            deepLinkRoute == "requestCapture" -> onRequestScreenCapture()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.NOTES,
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
