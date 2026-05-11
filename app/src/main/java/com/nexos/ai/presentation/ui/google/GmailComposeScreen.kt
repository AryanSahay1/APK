package com.nexos.ai.presentation.ui.google

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexos.ai.presentation.ui.components.PandaMascot
import com.nexos.ai.presentation.ui.components.PandaMotion

/**
 * In-app Gmail composer. Fills the standard mailto: + extras and hands off to Gmail.
 *
 * Why compose in-app rather than open Gmail directly?
 *  - The user can compose without context-switching to Gmail (which has its own slow load).
 *  - Drafts that get interrupted by switching apps don't lose data — the in-app fields
 *    survive Android process death via rememberSaveable.
 *  - Gmail's compose UI hides the recipient field on small phones; ours is always visible.
 *
 * Hand-off uses `Intent.ACTION_SENDTO` with a `mailto:` URI so Gmail (and other mail apps
 * the user has installed) recognise the request. Setting `setPackage("com.google.android.gm")`
 * forces Gmail when installed; if absent, the Android chooser picks any mail client.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GmailComposeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var to by rememberSaveable { mutableStateOf("") }
    var cc by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PandaMascot(size = 28.dp, hasLeaf = false, motion = PandaMotion.Wave)
                        Spacer(Modifier.width(10.dp))
                        Text("New email", fontWeight = FontWeight.SemiBold)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = to,
                onValueChange = { to = it; error = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("To") },
                placeholder = { Text("name@example.com") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.AlternateEmail, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors()
            )
            OutlinedTextField(
                value = cc,
                onValueChange = { cc = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cc (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors()
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Subject") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors()
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                label = { Text("Message") },
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors()
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    val recipients = to.trim()
                    if (recipients.isBlank()) {
                        error = "Add at least one recipient"
                        return@Button
                    }
                    val sent = sendViaGmail(
                        context = context,
                        to = recipients,
                        cc = cc.trim().takeIf { it.isNotBlank() },
                        subject = subject.trim(),
                        body = body
                    )
                    if (sent) onBack() else error = "No email app installed"
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Send via Gmail", fontWeight = FontWeight.SemiBold)
            }

            Text(
                "NexOS prefills Gmail with everything you typed above. Tap send inside Gmail to actually deliver — we don't transmit your email through any NexOS server.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
    cursorColor = MaterialTheme.colorScheme.primary
)

private fun sendViaGmail(
    context: android.content.Context,
    to: String,
    cc: String?,
    subject: String,
    body: String
): Boolean {
    // Build a mailto: URI Gmail recognises. mailto:to?subject=…&cc=…&body=…
    val params = buildList {
        if (subject.isNotBlank()) add("subject=" + Uri.encode(subject))
        if (!cc.isNullOrBlank()) add("cc=" + Uri.encode(cc))
        if (body.isNotBlank()) add("body=" + Uri.encode(body))
    }
    val mailto = "mailto:" + Uri.encode(to) + if (params.isNotEmpty()) "?" + params.joinToString("&") else ""

    // Try Gmail first
    val gmailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(mailto)).apply {
        setPackage("com.google.android.gm")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(gmailIntent) }.onSuccess { return true }

    // Fall back to any mail handler
    val anyMail = Intent(Intent.ACTION_SENDTO, Uri.parse(mailto)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return runCatching { context.startActivity(anyMail) }.isSuccess
}
