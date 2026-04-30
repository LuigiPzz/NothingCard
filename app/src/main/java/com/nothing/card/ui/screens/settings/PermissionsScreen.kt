package com.nothing.card.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nothing.card.ui.components.*
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.ui.theme.SpaceMonoFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Check camera permission status
    val hasCameraPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    Scaffold(
        containerColor = NothingBlack,
        topBar = {
            TopAppBar(
                title = { Text("Manage permissions", style = MaterialTheme.typography.headlineMedium, color = NothingWhite) },
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

            // Camera Permission Card
            SettingsGroupCard {
                SettingsItem(
                    title = "Camera",
                    subtitle = "Used to scan barcodes and QR codes from physical loyalty cards.",
                    statusText = if (hasCameraPermission.value) "Authorized" else "Denied",
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    isLast = true
                )
            }
            
            Text(
                text = "Used to scan barcodes and QR codes from physical loyalty cards.",
                fontFamily = SpaceMonoFamily,
                fontSize = 11.sp,
                color = NothingWhite.copy(alpha = 0.4f),
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}
