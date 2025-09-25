package com.example.app

// Simulated local database (in-memory)
object FakeCardDatabase {
    private val cardList = listOf(
        CardData(1,"Alice", "https://randomuser.me/api/portraits/women/1.jpg"),
        CardData(2,"Bob", "https://randomuser.me/api/portraits/men/2.jpg"),
        CardData(3,"Clara", "https://randomuser.me/api/portraits/women/3.jpg"),
        CardData(4,"Dave", "https://randomuser.me/api/portraits/men/4.jpg")
    )

    fun getCards(): List<CardData> = cardList
}

object CardDataGenerator {
    private var counter = 5

    fun generateNextCard(): CardData {
        val gender = if (counter % 2 == 0) "men" else "women"
        val id = counter
        val name = "User $counter"
        val imageUrl = "https://randomuser.me/api/portraits/$gender/${counter % 100}.jpg"
        counter++
        return CardData(id, name, imageUrl)
    }

}

