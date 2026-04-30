package com.nothing.card.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nothing.card.ui.components.DotMatrixText
import com.nothing.card.ui.components.NothingCardItem
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingRed
import com.nothing.card.ui.theme.NothingWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onCardClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val cards by viewModel.cards.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        containerColor = NothingBlack,
        topBar = {
            Column(modifier = Modifier.background(NothingBlack)) {
                TopAppBar(
                    title = { Text(text = "Cards", style = MaterialTheme.typography.headlineMedium, color = NothingWhite) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NothingBlack,
                        titleContentColor = NothingWhite
                    ),
                    actions = {
                        var showMenu by remember { mutableStateOf(false) }
                        var menuState by remember { mutableStateOf("MAIN") } // MAIN or SORT
                        
                        Box {
                            IconButton(onClick = { 
                                showMenu = true 
                                menuState = "MAIN" 
                            }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert, 
                                    contentDescription = "More", 
                                    tint = NothingWhite
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .background(NothingBlack)
                                    .border(1.dp, NothingWhite.copy(alpha = 0.1f))
                            ) {
                                if (menuState == "MAIN") {
                                    DropdownMenuItem(
                                        text = { DotMatrixText(text = "SORT", fontSize = 14) },
                                        onClick = { menuState = "SORT" },
                                        trailingIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NothingWhite) }
                                    )
                                    DropdownMenuItem(
                                        text = { DotMatrixText(text = "SETTINGS", fontSize = 14) },
                                        onClick = { 
                                            showMenu = false
                                            onSettingsClick()
                                        }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { DotMatrixText(text = "← BACK", fontSize = 12, color = MaterialTheme.colorScheme.secondary) },
                                        onClick = { menuState = "MAIN" }
                                    )
                                    Divider(color = NothingWhite.copy(alpha = 0.1f))
                                    
                                    val sortOptions = listOf(
                                        SortOrder.ALPHABETICAL_AZ to "A-Z",
                                        SortOrder.ALPHABETICAL_ZA to "Z-A",
                                        SortOrder.DATE_ADDED_NEWEST to "NEWEST",
                                        SortOrder.DATE_ADDED_OLDEST to "OLDEST",
                                        SortOrder.MOST_USED to "MOST USED"
                                    )
                                    
                                    sortOptions.forEach { (order, label) ->
                                        DropdownMenuItem(
                                            text = { DotMatrixText(text = label, fontSize = 14) },
                                            onClick = { 
                                                viewModel.onSortOrderChanged(order)
                                                showMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
                
                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { 
                        DotMatrixText(
                            text = "SEARCH FROM ${cards.size} ${if (cards.size == 1) "CARD" else "CARDS"}...", 
                            fontSize = 12, 
                            color = NothingWhite.copy(alpha = 0.3f)
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = NothingWhite.copy(alpha = 0.1f),
                contentColor = NothingWhite,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (cards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    DotMatrixText(
                        text = if (searchQuery.isBlank()) "NO CARDS YET" else "NO MATCHES FOUND",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
                ) {
                    items(cards) { card ->
                        val displaySubtitle = when {
                            searchQuery.isNotBlank() && card.cardNumber.contains(searchQuery) -> card.cardNumber
                            else -> card.ownerName
                        }
                        
                        NothingCardItem(
                            title = card.name,
                            subtitle = displaySubtitle,
                            colorHex = card.colorHex,
                            isFavorite = card.isFavorite,
                            onClick = { onCardClick(card.id) }
                        )
                    }
                }

                // Deep Black Overlay at the bottom
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
        }
    }
}
}
