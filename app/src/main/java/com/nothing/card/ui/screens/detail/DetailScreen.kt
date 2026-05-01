package com.nothing.card.ui.screens.detail

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nothing.card.ui.components.DotMatrixText
import com.nothing.card.ui.components.NothingButton
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.ui.theme.NothingRed
import com.nothing.card.ui.theme.SpaceMonoFamily
import com.nothing.card.ui.theme.NothingSerifFamily
import com.nothing.card.util.BarcodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    cardId: Long,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val card by viewModel.card.collectAsState()
    var showEditSheet by remember { mutableStateOf(false) }
    
    LaunchedEffect(cardId) {
        viewModel.loadCard(cardId)
    }

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        containerColor = NothingBlack,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = card?.name ?: "Card", 
                        style = MaterialTheme.typography.headlineMedium, 
                        color = NothingWhite
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NothingWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (card?.isFavorite == true) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (card?.isFavorite == true) Color.Yellow else NothingWhite
                        )
                    }
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NothingWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NothingBlack)
            )
        }
    ) { padding ->
        card?.let { currentCard ->
            val cardColor = remember(currentCard.colorHex) {
                try { 
                    val fullColor = if (currentCard.colorHex.startsWith("#")) currentCard.colorHex else "#${currentCard.colorHex}"
                    Color(android.graphics.Color.parseColor(fullColor)) 
                }
                catch (e: Exception) { NothingWhite }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Barcode Display with Dynamic Proportions
                val zxingFormat = remember(currentCard.barcodeFormat) {
                    BarcodeGenerator.mapToZXingFormat(currentCard.barcodeFormat)
                }
                val isQrCode = zxingFormat == com.google.zxing.BarcodeFormat.QR_CODE
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (isQrCode) 1.0f else 1.58f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(androidx.compose.foundation.BorderStroke(8.dp, cardColor), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val barcodeBitmap = remember(currentCard, zxingFormat) {
                        val width = if (isQrCode) 800 else 1000
                        val height = if (isQrCode) 800 else 400
                        BarcodeGenerator.generateBarcode(currentCard.cardNumber, zxingFormat, width, height)
                    }

                    barcodeBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Barcode",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp), // Inner padding for the barcode only
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    val isUrl = currentCard.cardNumber.startsWith("http://", ignoreCase = true) || 
                               currentCard.cardNumber.startsWith("https://", ignoreCase = true)
                    
                    if (!isUrl) {
                        DotMatrixText(text = "CARD NUMBER", fontSize = 12, color = MaterialTheme.colorScheme.secondary)
                        Text(text = currentCard.cardNumber, style = MaterialTheme.typography.displaySmall, color = NothingWhite)
                    }
                    
                    if (currentCard.ownerName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        DotMatrixText(text = "OWNER", fontSize = 12, color = MaterialTheme.colorScheme.secondary)
                        Text(text = currentCard.ownerName.uppercase(), style = MaterialTheme.typography.headlineMedium, color = NothingWhite)
                    }
                }
            }

            if (showEditSheet) {
                EditCardBottomSheet(
                    card = currentCard,
                    sheetState = sheetState,
                    onDismiss = { showEditSheet = false },
                    onSave = { name, number, color, owner ->
                        viewModel.updateCard(name, number, color, owner)
                        showEditSheet = false
                    },
                    onDelete = {
                        viewModel.deleteCard()
                        showEditSheet = false
                        onBack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardBottomSheet(
    card: com.nothing.card.data.local.entity.LoyaltyCard,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
    onDelete: () -> Unit
) {
    var editName by remember { mutableStateOf(card.name) }
    var editNumber by remember { mutableStateOf(card.cardNumber) }
    var editOwner by remember { mutableStateOf(card.ownerName) }
    var editColor by remember { mutableStateOf(card.colorHex) }

    val colorPresets = listOf(
        "#333333", // Nothing Black
        "#FF3131", // Nothing Red
        "#FF9800", // Orange
        "#FFEB3B", // Yellow
        "#4CAF50", // Green
        "#00BCD4", // Cyan
        "#2196F3", // Blue
        "#9C27B0", // Purple
        "#F48FB1"  // Nothing Pink
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NothingBlack,
        dragHandle = { BottomSheetDefaults.DragHandle(color = NothingWhite.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            DotMatrixText(text = "EDIT CARD", fontSize = 24)
            Spacer(modifier = Modifier.height(24.dp))

            // Inputs
            TextField(
                value = editName,
                onValueChange = { editName = it },
                label = { DotMatrixText(text = "STORE NAME", fontSize = 10) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = NothingWhite.copy(alpha = 0.05f),
                    unfocusedContainerColor = NothingWhite.copy(alpha = 0.05f),
                    focusedTextColor = NothingWhite,
                    unfocusedTextColor = NothingWhite
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = editNumber,
                onValueChange = { editNumber = it },
                label = { DotMatrixText(text = "CARD NUMBER", fontSize = 10) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = NothingWhite.copy(alpha = 0.05f),
                    unfocusedContainerColor = NothingWhite.copy(alpha = 0.05f),
                    focusedTextColor = NothingWhite,
                    unfocusedTextColor = NothingWhite
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = editOwner,
                onValueChange = { editOwner = it },
                label = { DotMatrixText(text = "OWNER NAME", fontSize = 10) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = NothingWhite.copy(alpha = 0.05f),
                    unfocusedContainerColor = NothingWhite.copy(alpha = 0.05f),
                    focusedTextColor = NothingWhite,
                    unfocusedTextColor = NothingWhite
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            DotMatrixText(text = "BRAND COLOR", fontSize = 12, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(colorPresets) { colorHex ->
                    val isSelected = editColor == colorHex
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(colorHex)))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) NothingWhite else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { editColor = colorHex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            NothingButton(
                text = "SAVE CHANGES",
                onClick = { onSave(editName, editNumber, editColor, editOwner) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = NothingRed)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DELETE CARD")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
