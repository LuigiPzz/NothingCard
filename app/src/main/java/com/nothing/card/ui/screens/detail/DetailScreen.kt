package com.nothing.card.ui.screens.detail

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nothing.card.ui.components.*
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.ui.theme.NothingRed
import com.nothing.card.ui.theme.SpaceMonoFamily
import com.nothing.card.ui.theme.SpaceGroteskFamily
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.nothing.card.util.BarcodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    cardId: Long,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // Find Activity to control window brightness
    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val originalBrightness = activity?.window?.attributes?.screenBrightness ?: -1f
        
        // Boost brightness to max
        activity?.window?.attributes = activity?.window?.attributes?.apply {
            screenBrightness = 1.0f
        }
        
        onDispose {
            // Restore original brightness
            activity?.window?.attributes = activity?.window?.attributes?.apply {
                screenBrightness = originalBrightness
            }
        }
    }

    val card by viewModel.card.collectAsState(null)
    var showEditSheet by remember { mutableStateOf(false) }
    
    LaunchedEffect(cardId) {
        viewModel.loadCard(cardId)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        NothingAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this card?", color = NothingWhite, style = MaterialTheme.typography.titleLarge) },
            text = { Text("This loyalty card and its associated data will be permanently removed from your device. This action cannot be undone.", color = NothingWhite.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCard()
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("DELETE", color = NothingRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", color = NothingWhite.copy(alpha = 0.5f))
                }
            }
        )
    }

    Scaffold(
        containerColor = NothingBlack,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = card?.name ?: "Card", 
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = SpaceGroteskFamily),
                        color = NothingWhite
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NothingWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (card?.isFavorite == true) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (card?.isFavorite == true) Color(0xFFFFD700) else NothingWhite
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Screen-wide Dot Matrix Background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotSize = 1.dp.toPx()
                val gap = 16.dp.toPx()
                val columns = (size.width / gap).toInt()
                val rows = (size.height / gap).toInt()
                
                for (i in 0..columns) {
                    for (j in 0..rows) {
                        drawCircle(
                            color = NothingWhite.copy(alpha = 0.05f),
                            radius = dotSize / 2,
                            center = Offset(i * gap, j * gap)
                        )
                    }
                }
            }

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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    val zxingFormat = remember(currentCard.barcodeFormat) {
                        BarcodeGenerator.mapToZXingFormat(currentCard.barcodeFormat)
                    }
                    val isQrCode = zxingFormat == com.google.zxing.BarcodeFormat.QR_CODE

                    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                    val barcodeBitmap by produceState<Bitmap?>(initialValue = null, currentCard) {
                        value = currentCard.let { 
                            val width = if (isQrCode) 512 else 1000
                            val height = if (isQrCode) 512 else 400
                            val bitmap = BarcodeGenerator.generateBarcode(it.cardNumber, zxingFormat, width, height)
                            if (bitmap != null) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            }
                            bitmap
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (isQrCode) 1.0f else 1.58f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(androidx.compose.foundation.BorderStroke(4.dp, cardColor), RoundedCornerShape(16.dp))
                            .border(androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f)), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (barcodeBitmap != null) {
                            Image(
                                bitmap = barcodeBitmap!!.asImageBitmap(),
                                contentDescription = "Barcode",
                                modifier = Modifier.fillMaxSize(0.9f),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            CircularProgressIndicator(color = NothingBlack, strokeWidth = 1.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        val isUrl = currentCard.cardNumber.startsWith("http://", ignoreCase = true) || 
                                   currentCard.cardNumber.startsWith("https://", ignoreCase = true)
                        
                        if (!isUrl) {
                            DotMatrixText(text = "CARD NUMBER", fontSize = 12, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = currentCard.cardNumber, 
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = SpaceMonoFamily,
                                    fontSize = 24.sp,
                                    letterSpacing = 1.sp
                                ), 
                                color = NothingWhite
                            )
                        }
                        
                        if (currentCard.ownerName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(32.dp))
                            DotMatrixText(text = "OWNER", fontSize = 12, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = currentCard.ownerName, 
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold
                                ), 
                                color = NothingWhite
                            )
                        }

                        if (currentCard.category.isNotBlank()) {
                            Spacer(modifier = Modifier.height(32.dp))
                            DotMatrixText(text = "CATEGORY", fontSize = 12, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = currentCard.category,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = SpaceMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = NothingWhite
                            )
                        }
                    }
                }

            if (showEditSheet) {
                EditCardBottomSheet(
                    card = currentCard,
                    sheetState = sheetState,
                    onDismiss = { showEditSheet = false },
                    onSave = { name, number, color, owner, category ->
                        viewModel.updateCard(name, number, color, owner, category)
                        showEditSheet = false
                    },
                    onDelete = {
                        showEditSheet = false
                        showDeleteDialog = true
                    }
                )
            }
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
    onSave: (String, String, String, String, String) -> Unit,
    onDelete: () -> Unit
) {
    var editName by remember { mutableStateOf(card.name) }
    var editNumber by remember { mutableStateOf(card.cardNumber) }
    var editOwner by remember { mutableStateOf(card.ownerName) }
    var editColor by remember { mutableStateOf(card.colorHex) }
    var editCategory by remember { mutableStateOf(card.category) }
    
    val categories = listOf("RETAIL", "FOOD", "TRAVEL", "HEALTH", "ENTERTAINMENT", "SERVICES", "PET", "CHILD")

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

            Spacer(modifier = Modifier.height(24.dp))
            DotMatrixText(text = "CATEGORY (OPTIONAL)", fontSize = 12, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = editCategory == cat
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { editCategory = if (isSelected) "" else cat },
                        color = if (isSelected) NothingWhite else NothingWhite.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, if (isSelected) NothingWhite else NothingWhite.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) NothingBlack else NothingWhite,
                                fontFamily = SpaceMonoFamily,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            NothingButton(
                text = "SAVE CHANGES",
                onClick = { onSave(editName, editNumber, editColor, editOwner, editCategory) },
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
