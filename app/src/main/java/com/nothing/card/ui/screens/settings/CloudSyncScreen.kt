package com.nothing.card.ui.screens.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.nothing.card.ui.components.NothingButton
import com.nothing.card.ui.components.NothingAlertDialog
import com.nothing.card.ui.components.NothingSnackbar
import com.nothing.card.ui.components.SettingsGroupCard
import com.nothing.card.ui.components.SettingsItem
import com.nothing.card.ui.components.SettingsSectionHeader
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.ui.theme.SpaceMonoFamily
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentAccount by viewModel.currentAccount.collectAsState()
    val isBackingUp by viewModel.isBackingUp.collectAsState()
    val isRestoring by viewModel.isRestoring.collectAsState()
    val showDuplicateDialog by viewModel.showDuplicateDialog.collectAsState()
    val pendingCards by viewModel.pendingCards.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            task.result?.let { account ->
                viewModel.onAccountSignedIn(account)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.syncEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = NothingBlack,
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                NothingSnackbar(data)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Cloud Sync", style = MaterialTheme.typography.headlineMedium, color = NothingWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NothingWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NothingBlack)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            SettingsSectionHeader(title = "GOOGLE DRIVE")
            SettingsGroupCard {
                SettingsItem(
                    title = "Backup Now",
                    subtitle = "Upload all local cards to Drive",
                    onClick = {
                        val account = currentAccount
                        if (account != null) {
                            viewModel.backupToDrive(account)
                        } else {
                            signInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    statusText = if (isBackingUp) "Backing up..." else null
                )
                SettingsItem(
                    title = "Restore Data",
                    subtitle = "Download cards from Drive",
                    onClick = {
                        val account = currentAccount
                        if (account != null) {
                            viewModel.restoreFromDrive(account)
                        } else {
                            signInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    isLast = true,
                    statusText = if (isRestoring) "Restoring..." else null
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (currentAccount != null) {
                Text(
                    text = "Connected as: ${currentAccount!!.email}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NothingWhite.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    if (showDuplicateDialog) {
        NothingAlertDialog(
            onDismissRequest = { viewModel.cancelRestore() },
            title = { Text("Duplicates Found", color = NothingWhite, style = MaterialTheme.typography.titleLarge) },
            text = { 
                Text(
                    "Some cards in the backup are already present on this device. Do you want to skip the duplicates and import only the new ${pendingCards.size} cards?",
                    color = NothingWhite.copy(alpha = 0.7f)
                ) 
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmRestore() }) {
                    Text("OK", color = NothingWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelRestore() }) {
                    Text("CANCEL", color = NothingWhite.copy(alpha = 0.5f))
                }
            }
        )
    }
}
