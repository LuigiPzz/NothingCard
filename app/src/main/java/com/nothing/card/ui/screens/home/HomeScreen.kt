package com.nothing.card.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.nothing.card.ui.components.*
import com.nothing.card.ui.theme.*
import com.nothing.card.data.local.entity.LoyaltyCard
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay

fun getCategoryIcon(category: String): ImageVector {
    return when (category.uppercase()) {
        "RETAIL" -> Icons.Default.ShoppingCart
        "FOOD" -> Icons.Default.Restaurant
        "TRAVEL" -> Icons.Default.Flight
        "HEALTH" -> Icons.Default.MedicalServices
        "ENTERTAINMENT" -> Icons.Default.Movie
        "SERVICES" -> Icons.Default.Build
        "PET" -> Icons.Default.Pets
        "ALL" -> Icons.Default.AllInclusive
        else -> Icons.Default.Label
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onCardClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val cards by viewModel.cards.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSortOrder by viewModel.sortOrder.collectAsState()

    var cardToDelete by remember { mutableStateOf<LoyaltyCard?>(null) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (cardToDelete != null) {
        NothingAlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = { Text("Delete this card?", color = NothingWhite, style = MaterialTheme.typography.titleLarge) },
            text = { Text("This loyalty card and its associated data will be permanently removed from your device.", color = NothingWhite.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        cardToDelete?.let { viewModel.deleteCard(it) }
                        cardToDelete = null
                    }
                ) {
                    Text("DELETE", color = NothingRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("CANCEL", color = NothingWhite.copy(alpha = 0.5f))
                }
            }
        )
    }

    Scaffold(
        containerColor = NothingBlack,
        topBar = {
            Column(modifier = Modifier.background(NothingBlack)) {
                // Custom Top Bar matching NothingPodcast layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 20.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cards",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = Ndot57Family,
                            fontWeight = FontWeight.Normal,
                            fontSize = 34.sp,
                            lineHeight = 40.sp,
                            letterSpacing = 1.sp
                        ),
                        color = NothingWhite,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { showOptionsSheet = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = NothingWhite)
                    }
                }

                SearchBar(
                    searchQuery = searchQuery,
                    cardCount = cards.size,
                    onSearchChange = { viewModel.onSearchQueryChanged(it) }
                )

                val selectedCategory by viewModel.selectedCategory.collectAsState()
                val categories by viewModel.activeCategories.collectAsState()

                if (categories.size > 1) { // Only show filter bar if there are categories other than "ALL"
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(categories) { _, cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.onCategoryChanged(cat) },
                            color = if (isSelected) NothingWhite else NothingWhite.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, if (isSelected) NothingWhite else NothingWhite.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(cat),
                                    contentDescription = cat,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) NothingBlack else NothingWhite
                                )
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) NothingBlack else NothingWhite,
                                        fontFamily = SpaceMonoFamily,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

                if (showOptionsSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showOptionsSheet = false },
                        sheetState = sheetState,
                        containerColor = Color(0xFF121212),
                        dragHandle = {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .size(width = 32.dp, height = 4.dp)
                                    .clip(CircleShape)
                                    .background(NothingWhite.copy(alpha = 0.2f))
                            )
                        }
                    ) {
                        OptionsContent(
                            currentSortOrder = currentSortOrder,
                            onSortOrderChanged = { viewModel.onSortOrderChanged(it) },
                            onSettingsClick = {
                                showOptionsSheet = false
                                onSettingsClick()
                            },
                            onDismiss = { showOptionsSheet = false }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = NothingWhite.copy(alpha = 0.1f),
                contentColor = NothingWhite,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (cards.isEmpty()) {
                EmptyState(searchQuery)
            } else {
                CardsList(
                    cards = cards, 
                    searchQuery = searchQuery, 
                    onCardClick = onCardClick,
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onDeleteCard = { cardToDelete = it }
                )
                BottomGradientOverlay()
            }
        }
    }
}

@Composable
private fun OptionsContent(
    currentSortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit,
    onSettingsClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
    ) {
        Text(
            text = "OPZIONI",
            style = MaterialTheme.typography.titleSmall,
            color = NothingWhite.copy(alpha = 0.5f),
            fontFamily = SpaceMonoFamily
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Surface(
            onClick = onSettingsClick,
            color = Color.Transparent,
            contentColor = NothingWhite
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Impostazioni App",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = NothingSerifFamily
                )
            }
        }
        
        HorizontalDivider(color = NothingWhite.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 16.dp))
        
        SortOptionsEntry(currentSortOrder, onSortOrderChanged, onDismiss)
    }
}

@Composable
private fun SortOptionsEntry(
    currentSortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    val sortOptions = remember {
        listOf(
            SortOrder.ALPHABETICAL_AZ to "Alfabetico (A-Z)",
            SortOrder.ALPHABETICAL_ZA to "Alfabetico (Z-A)",
            SortOrder.DATE_ADDED_NEWEST to "Aggiunti di recente",
            SortOrder.DATE_ADDED_OLDEST to "Meno recenti",
            SortOrder.MOST_USED to "Più utilizzati"
        )
    }
    val currentLabel = sortOptions.find { it.first == currentSortOrder }?.second ?: ""
    var showSortMenu by remember { mutableStateOf(false) }
    
    Box {
        Surface(
            onClick = { showSortMenu = true },
            color = Color.Transparent,
            contentColor = NothingWhite
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Ordinamento", style = MaterialTheme.typography.titleLarge, fontFamily = NothingSerifFamily)
                    Text(
                        text = currentLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NothingWhite.copy(alpha = 0.5f),
                        fontFamily = SpaceMonoFamily
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false },
            modifier = Modifier.background(Color(0xFF1E1E1E)).border(1.dp, NothingWhite.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        ) {
            sortOptions.forEach { (order, label) ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = label, 
                            color = if (order == currentSortOrder) NothingRed else NothingWhite,
                            fontFamily = SpaceMonoFamily
                        ) 
                    },
                    onClick = {
                        onSortOrderChanged(order)
                        showSortMenu = false
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchBar(searchQuery: String, cardCount: Int, onSearchChange: (String) -> Unit) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { 
            DotMatrixText(
                text = "SEARCH FROM $cardCount ${if (cardCount == 1) "CARD" else "CARDS"}...", 
                fontSize = 12, 
                color = NothingWhite.copy(alpha = 0.3f)
            ) 
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NothingWhite.copy(alpha = 0.5f)) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = NothingWhite.copy(alpha = 0.05f),
            unfocusedContainerColor = NothingWhite.copy(alpha = 0.05f),
            focusedTextColor = NothingWhite,
            unfocusedTextColor = NothingWhite,
            focusedIndicatorColor = NothingWhite.copy(alpha = 0.2f),
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
private fun EmptyState(searchQuery: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        DotMatrixText(
            text = if (searchQuery.isBlank()) "NO CARDS YET" else "NO MATCHES FOUND",
            fontSize = 14,
            color = NothingWhite.copy(alpha = 0.5f)
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CardsList(
    cards: List<LoyaltyCard>, 
    searchQuery: String, 
    onCardClick: (Long) -> Unit,
    onToggleFavorite: (LoyaltyCard) -> Unit,
    onDeleteCard: (LoyaltyCard) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        itemsIndexed(
            items = cards,
            key = { _, card -> card.id }
        ) { index, card ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(card.id) {
                delay(index * 35L) // Faster staggered entry
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300)) + 
                        scaleIn(initialScale = 0.92f, animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
            ) {
            var isDissolving by remember { mutableStateOf(false) }

            PixelDissolveContainer(
                isDissolving = isDissolving,
                onAnimationEnd = {
                    onDeleteCard(card)
                    isDissolving = false
                }
            ) {
                var showContextMenu by remember { mutableStateOf(false) }
                
                Box {
                    NothingCardItem(
                        title = card.name,
                        subtitle = if (searchQuery.isNotBlank() && card.cardNumber.contains(searchQuery)) card.cardNumber else card.ownerName,
                        colorHex = card.colorHex,
                        category = card.category,
                        isFavorite = card.isFavorite,
                        onClick = { onCardClick(card.id) },
                        onLongClick = { showContextMenu = true }
                    )

                    DropdownMenu(
                        expanded = showContextMenu,
                        onDismissRequest = { showContextMenu = false },
                        modifier = Modifier.background(Color(0xFF1E1E1E)).border(1.dp, NothingWhite.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(if (card.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(if (card.isFavorite) "Rimuovi dai Preferiti" else "Aggiungi ai Preferiti", fontFamily = SpaceMonoFamily) 
                                }
                            },
                            onClick = {
                                onToggleFavorite(card)
                                showContextMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = NothingRed, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Elimina Tessera", color = NothingRed, fontFamily = SpaceMonoFamily) 
                                }
                            },
                            onClick = {
                                isDissolving = true
                                showContextMenu = false
                            }
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun BoxScope.BottomGradientOverlay() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .align(Alignment.BottomCenter)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, NothingBlack),
                    startY = 0f
                )
            )
    )
}
