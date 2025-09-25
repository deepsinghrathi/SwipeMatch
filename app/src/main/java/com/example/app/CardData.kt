package com.example.app

import com.corapana.swipematch.SwipeableCardItem

data class CardData(override val id: Int, val name: String, val imageUrl: String): SwipeableCardItem
