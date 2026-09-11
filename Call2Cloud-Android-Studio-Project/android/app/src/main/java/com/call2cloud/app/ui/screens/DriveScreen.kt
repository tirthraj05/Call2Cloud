package com.call2cloud.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveScreen(
    isConnected: Boolean,
    accountEmail: String?,
    accountName: String?,
    currentFolderName: String,
    onConnectGoogleDrive: () -> Unit,
    onChangeGoogleAccount: () -> Unit,
    onDisconnectGoogleDrive: () -> Unit,
    onUpdateFolderName: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Drive Backup") },
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
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        if (isConnected) "Connected Account" else "Connect Google Account",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (isConnected) (accountEmail ?: "") else "Minimum scope: drive.file",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!isConnected) {
                        Button(
                            onClick = onConnectGoogleDrive,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect Google Drive")
                        }
                    } else {
                        // Change Google Drive Account
                        Button(
                            onClick = onChangeGoogleAccount,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Google Account")
                        }

                        OutlinedButton(
                            onClick = onDisconnectGoogleDrive,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Disconnect Account", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}