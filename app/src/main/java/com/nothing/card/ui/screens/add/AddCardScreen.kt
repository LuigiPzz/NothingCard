package com.nothing.card.ui.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nothing.card.ui.components.DotMatrixText
import com.nothing.card.ui.components.NothingButton
import com.nothing.card.ui.components.NothingAlertDialog
import com.nothing.card.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    cardNumber: String,
    barcodeFormat: String,
    initialName: String = "",
    initialColor: String = "#333333",
    onCardAdded: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddCardViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf(initialName) }
    var ownerName by remember { mutableStateOf("") }
    var cardNumberState by remember { mutableStateOf(cardNumber) }
    var barcodeFormatState by remember { mutableStateOf(barcodeFormat) }
    
    var selectedColorHex by remember { mutableStateOf(initialColor) }
    var selectedCategory by remember { mutableStateOf("") }

    val categories = listOf(
        "RETAIL", "FOOD", "TRAVEL", "HEALTH", "ENTERTAINMENT", "SERVICES", "PET", "OTHER"
    )
    
    val cardColor = remember(selectedColorHex) { 
        try { 
            val fullColor = if (selectedColorHex.startsWith("#")) selectedColorHex else "#$selectedColorHex"
            Color(android.graphics.Color.parseColor(fullColor)) 
        } 
        catch (e: Exception) { NothingBlack }
    }
    
    val colorPresets = listOf(
        "#800020", // Bordeaux
        "#FF3131", // Nothing Red
        "#FF9800", // Orange
        "#FFEB3B", // Yellow
        "#4CAF50", // Green
        "#1B5E20", // Dark Green
        "#00BCD4", // Cyan
        "#2196F3", // Blue
        "#004B91", // Aviation Blue
        "#9C27B0", // Purple
        "#F48FB1", // Nothing Pink
        "#795548", // Brown
        "#3E2723", // Dark Brown
        "#333333"  // Nothing Black
    )

    var isSaving by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = NothingBlack,
        topBar = {
            TopAppBar(
                title = { Text(text = "New Card", style = MaterialTheme.typography.headlineMedium, color = NothingWhite) },
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
                .padding(24.dp)
        ) {
            // Live Preview Card
            DotMatrixText(
                text = "PREVIEW",
                fontSize = 12,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.58f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                cardColor.copy(alpha = 0.8f),
                                cardColor.copy(alpha = 1.0f)
                            ),
                            center = Offset.Zero,
                            radius = 1000f
                        )
                    )
                    .border(androidx.compose.foundation.BorderStroke(1.dp, NothingWhite.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
            ) {
                // Dot Matrix Pattern Overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dotSize = 1.dp.toPx()
                    val gap = 8.dp.toPx()
                    val columns = (size.width / gap).toInt()
                    val rows = (size.height / gap).toInt()
                    
                    for (i in 0..columns) {
                        for (j in 0..rows) {
                            drawCircle(
                                color = NothingWhite.copy(alpha = 0.1f),
                                radius = dotSize / 2,
                                center = Offset(i * gap, j * gap)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (name.isBlank()) "Card name" else name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = NothingSerifFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (name.isBlank()) NothingWhite.copy(alpha = 0.3f) else NothingWhite
                        )
                    }

                    val isUrl = cardNumberState.startsWith("http://", ignoreCase = true) || 
                               cardNumberState.startsWith("https://", ignoreCase = true)
                    
                    if (!isUrl) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (cardNumberState.isBlank()) "NO NUMBER" else cardNumberState,
                            fontFamily = SpaceMonoFamily,
                            fontSize = 14.sp,
                            color = NothingWhite.copy(alpha = 0.5f),
                            letterSpacing = 2.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            if (ownerName.isNotBlank()) {
                                Text(
                                    text = ownerName,
                                    fontFamily = SpaceMonoFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NothingWhite,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = "LOYALTY CARD",
                                fontFamily = SpaceMonoFamily,
                                fontSize = 9.sp,
                                color = NothingWhite.copy(alpha = 0.4f),
                                letterSpacing = 1.sp
                            )
                        }
                        
                        // Refined Red Accent
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NothingRed)
                                    .border(1.dp, NothingWhite.copy(alpha = 0.2f), CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            DotMatrixText(
                text = "CARD DETAILS",
                fontSize = 12,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { DotMatrixText(text = "CARD NAME", fontSize = 10, color = NothingWhite.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = NothingWhite.copy(alpha = 0.05f),
                    unfocusedContainerColor = NothingWhite.copy(alpha = 0.05f),
                    focusedTextColor = NothingWhite,
                    unfocusedTextColor = NothingWhite,
                    focusedIndicatorColor = NothingWhite,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { DotMatrixText(text = "OWNER NAME", fontSize = 10, color = NothingWhite.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = NothingWhite.copy(alpha = 0.05f),
                    unfocusedContainerColor = NothingWhite.copy(alpha = 0.05f),
                    focusedTextColor = NothingWhite,
                    unfocusedTextColor = NothingWhite,
                    focusedIndicatorColor = NothingWhite,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = cardNumberState,
                onValueChange = { cardNumberState = it },
                label = { DotMatrixText(text = "CARD NUMBER", fontSize = 10, color = NothingWhite.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = SpaceMonoFamily,
                    fontSize = 16.sp,
                    color = NothingWhite
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = NothingWhite.copy(alpha = 0.05f),
                    unfocusedContainerColor = NothingWhite.copy(alpha = 0.05f),
                    focusedTextColor = NothingWhite,
                    unfocusedTextColor = NothingWhite,
                    focusedIndicatorColor = NothingWhite,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            DotMatrixText(text = "CARD COLOR", fontSize = 12, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(colorPresets) { hex ->
                    val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColorHex == hex) 2.dp else 0.dp,
                                color = if (selectedColorHex == hex) NothingWhite else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColorHex = hex }
                    )
                }
            }
            
            DotMatrixText(text = "CATEGORY (OPTIONAL)", fontSize = 12, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategory = if (isSelected) "" else cat },
                        color = if (isSelected) NothingWhite else NothingWhite.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, if (isSelected) NothingWhite else NothingWhite.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) NothingBlack else NothingWhite,
                                fontFamily = SpaceMonoFamily,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            NothingButton(
                text = if (isSaving) "SAVING..." else "CONFIRM & SAVE",
                onClick = {
                    scope.launch {
                        val normalizedNumber = cardNumberState.trim()
                        if (viewModel.checkIfCardExists(normalizedNumber)) {
                            showDuplicateDialog = true
                        } else {
                            isSaving = true
                            viewModel.saveCard(name, normalizedNumber, barcodeFormatState, selectedColorHex, ownerName, selectedCategory) {
                                onCardAdded()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && cardNumberState.isNotBlank() && !isSaving
            )
        }
    }

    if (showDuplicateDialog) {
        NothingAlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { DotMatrixText(text = "DUPLICATE CARD", fontSize = 18) },
            text = { 
                Text(
                    "A card with this number already exists. Do you want to add it anyway?",
                    color = NothingWhite.copy(alpha = 0.7f),
                    fontFamily = SpaceMonoFamily
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDuplicateDialog = false
                    isSaving = true
                    viewModel.saveCard(name, cardNumberState.trim(), barcodeFormatState, selectedColorHex, ownerName, selectedCategory) {
                        onCardAdded()
                    }
                }) {
                    Text("ADD ANYWAY", color = NothingRed, fontFamily = SpaceMonoFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateDialog = false }) {
                    Text("CANCEL", color = NothingWhite, fontFamily = SpaceMonoFamily)
                }
            }
        )
    }
}
