package com.example.pokedex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_pokemon")
data class FavoritePokemon(
    @PrimaryKey val id: Int,
    val name: String,
    val types: List<String>,
    val spriteUrl: String
)

