package com.nexos.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.nexos.ai.presentation.navigation.NexosNavHost
import com.nexos.ai.presentation.ui.theme.NexosTheme
import com.nexos.ai.service.FloatingButtonService
import com.nexos.ai.service.ScreenshotService
import com.nexos.ai.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var projectionLauncher: ActivityResultLauncher<Intent>
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* result irrelevant — notification helper checks at point of use */ }

        // Android 13+ — request runtime permission for POST_NOTIFICATIONS so save confirmations work
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        projectionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                val intent = ScreenshotService.startIntent(this, result.resultCode, data)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
                else startService(intent)

                // If we were opened from the floating button for capture, fire one now via the receiver.
                val nav = this.intent?.getStringExtra(Constants.EXTRA_NAV_ROUTE)
                if (nav == "requestCapture") {
                    FloatingButtonService.requestScreenshot(this)
                }
            }
        }

        setContent {
            NexosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NexosNavHost(
                        deepLinkNoteId = intent?.getLongExtra(Constants.EXTRA_NOTE_ID, -1L)?.takeIf { it > 0 },
                        deepLinkRoute = intent?.getStringExtra(Constants.EXTRA_NAV_ROUTE),
                        onRequestScreenCapture = { requestScreenCapture() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    fun requestScreenCapture() {
        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projectionLauncher.launch(manager.createScreenCaptureIntent())
    }
}
