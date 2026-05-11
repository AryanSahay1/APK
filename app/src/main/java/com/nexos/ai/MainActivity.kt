package com.nexos.ai

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexos.ai.presentation.navigation.NexosDestinations
import com.nexos.ai.presentation.ui.notes.EditNoteScreen
import com.nexos.ai.presentation.ui.notes.NoteDetailScreen
import com.nexos.ai.presentation.ui.notes.NoteListScreen
import com.nexos.ai.presentation.ui.settings.SettingsScreen
import com.nexos.ai.presentation.ui.theme.NexosTheme
import com.nexos.ai.service.ScreenshotService
import com.nexos.ai.util.NavBridge
import com.nexos.ai.util.NexosActions
import com.nexos.ai.util.ScreenCaptureBridge
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity host. Owns the Compose nav graph and the [MediaProjectionManager]
 * permission launcher, because the screen-capture intent can only be launched
 * from an Activity context.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var screenCaptureBridge: ScreenCaptureBridge
    @Inject lateinit var navBridge: NavBridge

    private lateinit var projectionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        projectionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val svcIntent = Intent(this, ScreenshotService::class.java).apply {
                    putExtra(NexosActions.EXTRA_MEDIA_PROJECTION_RESULT_CODE, result.resultCode)
                    putExtra(NexosActions.EXTRA_MEDIA_PROJECTION_DATA, result.data)
                }
                ContextCompat.startForegroundService(this, svcIntent)
                screenCaptureBridge.onPermissionGranted()
            } else {
                screenCaptureBridge.onPermissionDenied()
            }
        }
        screenCaptureBridge.setLauncher {
            val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projectionLauncher.launch(mpm.createScreenCaptureIntent())
        }

        setContent {
            NexosTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NexosNavHost(
                        onRequestScreenCapturePermission = screenCaptureBridge::requestPermission
                    )
                }
            }
        }

        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    override fun onDestroy() {
        screenCaptureBridge.clearLauncher()
        super.onDestroy()
    }

    private fun handleDeepLink(intent: Intent?) {
        val noteId = intent?.getLongExtra(NexosActions.EXTRA_NOTE_ID, -1L) ?: -1L
        if (noteId > 0) navBridge.publishDeepLink(noteId)
    }
}

@Composable
private fun NexosNavHost(
    onRequestScreenCapturePermission: () -> Unit
) {
    val navController = rememberNavController()
    val bridge = hiltViewModel<NavBridgeViewModel>()
    val pendingNoteId by bridge.pendingDeepLinkNoteId.collectAsStateWithLifecycle()

    LaunchedEffect(pendingNoteId) {
        val id = pendingNoteId ?: return@LaunchedEffect
        navController.navigate(NexosDestinations.noteDetail(id))
        bridge.consumePendingDeepLink()
    }

    NavHost(
        navController = navController,
        startDestination = NexosDestinations.NOTE_LIST
    ) {
        composable(NexosDestinations.NOTE_LIST) {
            NoteListScreen(
                onNoteClick = { id -> navController.navigate(NexosDestinations.noteDetail(id)) },
                onSettingsClick = { navController.navigate(NexosDestinations.SETTINGS) },
                onNewNoteClick = {
                    navController.navigate(NexosDestinations.editNote(NexosDestinations.NEW_NOTE_ID))
                },
                onRequestScreenCapturePermission = onRequestScreenCapturePermission
            )
        }
        composable(NexosDestinations.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = NexosDestinations.NOTE_DETAIL_ROUTE,
            arguments = listOf(navArgument(NexosDestinations.ARG_NOTE_ID) { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong(NexosDestinations.ARG_NOTE_ID) ?: return@composable
            NoteDetailScreen(
                noteId = id,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(NexosDestinations.editNote(id)) }
            )
        }
        composable(
            route = NexosDestinations.EDIT_NOTE_ROUTE,
            arguments = listOf(navArgument(NexosDestinations.ARG_NOTE_ID) { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong(NexosDestinations.ARG_NOTE_ID)
                ?: NexosDestinations.NEW_NOTE_ID
            EditNoteScreen(
                noteId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
