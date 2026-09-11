package com.call2cloud.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    audioFormat: String,
    audioBitrate: Int,
    wifiOnly: Boolean,
    autoBackup: Boolean,
    autoDeleteAfterUpload: Boolean,
    showNotifications: Boolean,
    driveAccountEmail: String?,
    onChangeGoogleAccount: () -> Unit,
    onUpdateAudioConfig: (String, Int) -> Unit,
    onUpdateWifiOnly: (Boolean) -> Unit,
    onUpdateAutoBackup: (Boolean) -> Unit,
    onUpdateAutoDelete: (Boolean) -> Unit,
    onUpdateShowNotifications: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Google Drive Account Section
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            driveAccountEmail ?: "No Account Connected",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            if (driveAccountEmail != null) "Uploads will be saved to this Google account" else "Sign in to back up phone calls",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    OutlinedButton(
                        onClick = onChangeGoogleAccount,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (driveAccountEmail != null) "Change" else "Connect")
                    }
                }
            }
        }
    }
}