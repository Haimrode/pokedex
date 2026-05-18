package com.example.pokedex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FavoritePokemon::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(FavoritePokemonConverters::class)
abstract class PokedexDatabase : RoomDatabase() {
    abstract fun favoritePokemonDao(): FavoritePokemonDao
}

