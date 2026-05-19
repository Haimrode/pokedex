package com.example.pokedex.data.local

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonLevelStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getProgress(pokemonId: Int): PokemonLevelProgress {
        val totalXp = prefs.getInt(xpKey(pokemonId), 0).coerceAtLeast(0)
        return totalXp.toProgress()
    }

    fun gainExperience(pokemonId: Int, gainedXp: Int): PokemonLevelProgress {
        val current = getProgress(pokemonId)
        val updatedTotal = (current.totalXp + gainedXp).coerceAtLeast(0)
        prefs.edit { putInt(xpKey(pokemonId), updatedTotal) }
        return updatedTotal.toProgress()
    }

    /** Force le niveau d'un Pokémon (met à jour le total XP en conséquence). */
    fun setLevel(pokemonId: Int, level: Int): PokemonLevelProgress {
        val clamped = level.coerceAtLeast(1)
        val totalXp = (clamped - 1) * XP_PER_LEVEL
        prefs.edit { putInt(xpKey(pokemonId), totalXp) }
        return totalXp.toProgress()
    }

    private fun Int.toProgress(): PokemonLevelProgress {
        val level = 1 + (this / XP_PER_LEVEL)
        val xpInLevel = this % XP_PER_LEVEL
        val xpToNextLevel = XP_PER_LEVEL - xpInLevel
        val unlockedMoveCount = (level + 1).coerceAtMost(MAX_UNLOCKED_MOVES)
        return PokemonLevelProgress(
            totalXp = this,
            level = level,
            xpInLevel = xpInLevel,
            xpToNextLevel = xpToNextLevel,
            unlockedMoveCount = unlockedMoveCount
        )
    }

    companion object {
        private const val PREFS_NAME = "pokemon_levels"
        private const val XP_PER_LEVEL = 100
        private const val MAX_UNLOCKED_MOVES = 4

        private fun xpKey(pokemonId: Int): String = "xp_$pokemonId"
    }
}

data class PokemonLevelProgress(
    val totalXp: Int,
    val level: Int,
    val xpInLevel: Int,
    val xpToNextLevel: Int,
    val unlockedMoveCount: Int
)

