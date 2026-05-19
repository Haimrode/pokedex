package com.example.pokedex.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePokemonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favoritePokemon: FavoritePokemon)

    @Delete
    suspend fun delete(favoritePokemon: FavoritePokemon)

    @Query("DELETE FROM favorite_pokemon WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM favorite_pokemon ORDER BY id ASC")
    fun getAll(): Flow<List<FavoritePokemon>>

    @Query("SELECT * FROM favorite_pokemon WHERE id = :id LIMIT 1")
    fun getById(id: Int): Flow<FavoritePokemon?>

    @Query("SELECT COUNT(*) FROM favorite_pokemon WHERE id = :id")
    fun isFavorite(id: Int): Flow<Int>
}

