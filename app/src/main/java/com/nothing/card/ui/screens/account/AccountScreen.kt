package com.nothing.card.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.nothing.card.ui.components.*
import com.nothing.card.ui.screens.settings.SettingsViewModel
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.ui.theme.NothingSerifFamily
import com.nothing.card.ui.theme.SpaceMonoFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onSignOutSuccess: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentAccount by viewModel.currentAccount.collectAsState()
    
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Redirect to back if signed out
    LaunchedEffect(currentAccount) {
        if (currentAccount == null) {
            onSignOutSuccess()
        }
    }

    Scaffold(
        containerColor = NothingBlack,
        topBar = {
            TopAppBar(
                title = { },
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Avatar with Dot Matrix Initial
            val initial = currentAccount?.displayName?.firstOrNull()?.toString() ?: "U"
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                DotMatrixText(
                    text = initial.uppercase(),
                    fontSize = 64,
                    color = NothingWhite
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Name
            Text(
                text = currentAccount?.displayName ?: "User Name",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = NothingSerifFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = NothingWhite
            )

            // Email
            Text(
                text = currentAccount?.email ?: "user@example.com",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = SpaceMonoFamily,
                    color = NothingWhite.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(88.dp))

            // Sign Out Button
            Surface(
                onClick = { viewModel.signOut(googleSignInClient) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A1A1A),
                contentColor = NothingWhite
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Esci",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
