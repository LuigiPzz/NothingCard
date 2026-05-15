package com.nothing.card.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothing.card.data.local.entity.LoyaltyCard
import com.nothing.card.data.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder {
    ALPHABETICAL_AZ,
    ALPHABETICAL_ZA,
    DATE_ADDED_NEWEST,
    DATE_ADDED_OLDEST,
    MOST_USED
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CardRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.MOST_USED)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val cards: StateFlow<List<LoyaltyCard>> = combine(
        _searchQuery,
        _sortOrder,
        _selectedCategory,
        repository.allCards
    ) { query, sort, category, allCards ->
        var filtered = if (query.isBlank()) {
            allCards
        } else {
            allCards.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.ownerName.contains(query, ignoreCase = true) ||
                it.cardNumber.contains(query)
            }
        }
        
        // Category Filter
        if (category != "ALL") {
            filtered = filtered.filter { it.category == category }
        }
        
        when (sort) {
            SortOrder.ALPHABETICAL_AZ -> filtered.sortedWith(compareByDescending<LoyaltyCard> { it.isFavorite }.thenBy { it.name.lowercase() })
            SortOrder.ALPHABETICAL_ZA -> filtered.sortedWith(compareByDescending<LoyaltyCard> { it.isFavorite }.thenByDescending { it.name.lowercase() })
            SortOrder.DATE_ADDED_NEWEST -> filtered.sortedWith(compareByDescending<LoyaltyCard> { it.isFavorite }.thenByDescending { it.createdAt })
            SortOrder.DATE_ADDED_OLDEST -> filtered.sortedWith(compareByDescending<LoyaltyCard> { it.isFavorite }.thenBy { it.createdAt })
            SortOrder.MOST_USED -> filtered.sortedWith(compareByDescending<LoyaltyCard> { it.isFavorite }.thenByDescending { it.usageCount })
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val activeCategories: StateFlow<List<String>> = repository.allCards
        .map { allCards ->
            val categoryUsage = allCards
                .filter { it.category.isNotBlank() }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.usageCount } }
            
            val sortedCategories = categoryUsage.entries
                .sortedWith(
                    compareByDescending<Map.Entry<String, Int>> { it.value }
                        .thenBy { it.key }
                )
                .map { it.key }
            
            listOf("ALL") + sortedCategories
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("ALL"))

    fun onCategoryChanged(category: String) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChanged(order: SortOrder) {
        _sortOrder.value = order
    }

    fun toggleFavorite(card: LoyaltyCard) {
        viewModelScope.launch {
            repository.updateCard(card.copy(isFavorite = !card.isFavorite))
        }
    }

    fun deleteCard(card: LoyaltyCard) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }
}
