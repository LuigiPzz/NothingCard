package com.nothing.card.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.ui.theme.SpaceMonoFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = NothingBlack,
        topBar = {
            TopAppBar(
                title = { Text("About", style = MaterialTheme.typography.headlineMedium, color = NothingWhite) },
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
                .verticalScroll(rememberScrollState())
        ) {
            HorizontalDivider(color = NothingWhite.copy(alpha = 0.1f))
            
            AboutItem(label = "App", value = "Nothing Card")
            AboutItem(label = "Version", value = "1.0.0-beta")
            AboutItem(label = "Developer", value = "Lpzz")
            AboutItem(label = "Design", value = "Nothing OS Style")
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "© 2024 NOTHING CARD TEAM",
                fontFamily = SpaceMonoFamily,
                fontSize = 10.sp,
                color = NothingWhite.copy(alpha = 0.2f),
                modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun AboutItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = NothingWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                fontFamily = SpaceMonoFamily,
                fontSize = 14.sp,
                color = NothingWhite.copy(alpha = 0.5f)
            )
        }
        HorizontalDivider(color = NothingWhite.copy(alpha = 0.1f))
    }
}
