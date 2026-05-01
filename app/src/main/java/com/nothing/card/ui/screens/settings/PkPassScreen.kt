package com.nothing.card.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nothing.card.ui.components.*
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.ui.components.NothingSnackbar
import com.nothing.card.ui.theme.SpaceMonoFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PkPassScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // In a real app, we'd read the file and call viewModel.importCards(uri)
            // For now, we'll simulate a successful import message from the ViewModel
            scope.launch {
                snackbarHostState.showSnackbar("Importing cards from file...")
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
                title = { Text("Backup & Restore", style = MaterialTheme.typography.headlineMedium, color = NothingWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NothingWhite)
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

            SettingsSectionHeader(title = "MANAGE FILES")
            SettingsGroupCard {
                SettingsItem(
                    title = "Import cards",
                    subtitle = "Load .pkpass or .json files",
                    onClick = { importLauncher.launch("*/*") }
                )
                SettingsItem(
                    title = "Export cards",
                    subtitle = "Save all cards as .pkpass backup",
                    onClick = { viewModel.exportToPkPass() },
                    isLast = true
                )
            }
            
            Text(
                text = "Use PKPASS to migrate your cards to other wallet apps or to create a local backup.",
                fontFamily = SpaceMonoFamily,
                fontSize = 11.sp,
                color = NothingWhite.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}
