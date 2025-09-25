package com.corapana.swipematch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class GenericCardViewModel<T : SwipeableCardItem>(
    private val initialData: List<T> = emptyList(),
    private val loadNextCard: suspend () -> T? = { null }, // suspend for async loading
    private val threshold: Int = 2 // trigger loading when cards <= threshold
) : ViewModel() {

    private val _cards = MutableStateFlow<List<T>>(emptyList())
    val cards: StateFlow<List<T>> = _cards

    init {
        viewModelScope.launch {
            loadInitialData()
        }
    }

    // Load initial data
    private suspend fun loadInitialData() {
        if (initialData.isNotEmpty()) {
            _cards.value = initialData
        } else {
            loadNextCard()?.let { _cards.value = listOf(it) }
        }
    }

    // Remove top card and check if we need to load more
    fun removeTopAndAddNew() {
        val current = _cards.value
        if (current.isNotEmpty()) {
            val updated = current.drop(1).toMutableList()
            _cards.value = updated

            // If cards running low, load next card
            viewModelScope.launch {
                if (_cards.value.size <= threshold) {
                    loadNextCard()?.let { _cards.value = _cards.value + it }
                }
            }
        }
    }

    // Add more cards manually
    fun addCards(newCards: List<T>) {
        _cards.value = _cards.value + newCards
    }
}


