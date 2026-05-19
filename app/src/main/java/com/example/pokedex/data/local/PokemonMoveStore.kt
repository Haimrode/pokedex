package com.example.pokedex.data.local

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonMoveStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSelectedMoves(pokemonId: Int): Set<String> = prefs.getString(movesKey(pokemonId), null)
        ?.split('|')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        .orEmpty()

    fun setSelectedMoves(pokemonId: Int, moves: Set<String>) {
        prefs.edit {
            putString(movesKey(pokemonId), moves.joinToString("|") )
        }
    }

    fun clear(pokemonId: Int) {
        prefs.edit { remove(movesKey(pokemonId)) }
    }

    private fun movesKey(pokemonId: Int) = "moves_$pokemonId"

    companion object {
        private const val PREFS_NAME = "pokemon_moves"
    }
}

