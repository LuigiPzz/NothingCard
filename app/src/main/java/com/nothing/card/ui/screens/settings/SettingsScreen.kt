package com.nothing.card.ui.screens.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.nothing.card.ui.components.*
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.ui.components.NothingSnackbar
import com.nothing.card.ui.theme.SpaceMonoFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToCloudSync: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPkPass: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentAccount by viewModel.currentAccount.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

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
        viewModel.syncEvent.collect { message: String ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
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
                title = { Text("Settings", style = MaterialTheme.typography.headlineMedium, color = NothingWhite) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ACCOUNT SECTION
            SettingsSectionHeader(title = "ACCOUNT")
            SettingsGroupCard {
                if (currentAccount != null) {
                    SettingsItem(
                        title = currentAccount!!.displayName ?: "User Account",
                        subtitle = currentAccount!!.email ?: "Signed in",
                        onClick = { showSignOutDialog = true },
                        statusText = "Sign out"
                    )
                } else {
                    SettingsItem(
                        title = "Google Account",
                        subtitle = "Sign in to sync your cards",
                        onClick = { signInLauncher.launch(googleSignInClient.signInIntent) },
                        statusText = "Sign in"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSectionHeader(title = "DATA & SYNC")
            SettingsGroupCard {
                SettingsItem(
                    title = "Cloud Sync",
                    subtitle = "Backup and sync via Google Drive",
                    onClick = onNavigateToCloudSync
                )
                SettingsItem(
                    title = "PKPASS",
                    subtitle = "Import/Export PKPASS files",
                    onClick = onNavigateToPkPass,
                    isLast = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSectionHeader(title = "INFO & SYSTEM")
            SettingsGroupCard {
                SettingsItem(
                    title = "Permissions",
                    subtitle = "App permissions status",
                    onClick = onNavigateToPermissions
                )
                SettingsItem(
                    title = "About",
                    subtitle = "Version and credits",
                    onClick = onNavigateToAbout,
                    isLast = true
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            SettingsSectionHeader(title = "ADVANCED")
            SettingsGroupCard {
                SettingsItem(
                    title = "Delete all data",
                    subtitle = "Remove all cards from this device",
                    onClick = { showDeleteDialog = true },
                    isLast = true
                )
            }

            // Dialogs
            if (showSignOutDialog) {
                NothingAlertDialog(
                    onDismissRequest = { showSignOutDialog = false },
                    title = { Text("Sign out?", color = NothingWhite, style = MaterialTheme.typography.titleLarge) },
                    text = { Text("You will no longer be able to sync your cards with Google Drive until you sign in again.", color = NothingWhite.copy(alpha = 0.7f)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.signOut(googleSignInClient)
                                showSignOutDialog = false
                            }
                        ) {
                            Text("SIGN OUT", color = NothingWhite, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSignOutDialog = false }) {
                            Text("CANCEL", color = NothingWhite.copy(alpha = 0.5f))
                        }
                    }
                )
            }

            if (showDeleteDialog) {
                NothingAlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete all cards?", color = NothingWhite, style = MaterialTheme.typography.titleLarge) },
                    text = { Text("This action cannot be undone. All your loyalty cards will be permanently removed from this device.", color = NothingWhite.copy(alpha = 0.7f)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteAllCards()
                                showDeleteDialog = false
                            }
                        ) {
                            Text("DELETE", color = NothingWhite, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("CANCEL", color = NothingWhite.copy(alpha = 0.5f))
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "CRAFTED FOR NOTHING ENTHUSIASTS.",
                fontFamily = SpaceMonoFamily,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = NothingWhite.copy(alpha = 0.3f),
                modifier = Modifier.padding(start = 16.dp, bottom = 48.dp)
            )
        }
    }
}
