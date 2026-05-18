package com.example.pokedex.data.local

import androidx.room.TypeConverter

class FavoritePokemonConverters {

    @TypeConverter
    fun fromTypes(types: List<String>): String = types.joinToString(separator = ",")

    @TypeConverter
    fun toTypes(types: String): List<String> =
        if (types.isBlank()) emptyList() else types.split(",").map { it.trim() }.filter { it.isNotBlank() }
}

