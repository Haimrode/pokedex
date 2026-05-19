package com.example.pokedex.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ShinyEncounterStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun registerAttempt(pokemonId: Int): ShinyEncounter {
        val attemptsKey = attemptsKey(pokemonId)
        val unlockedKey = unlockedKey(pokemonId)

        val alreadyUnlocked = prefs.getBoolean(unlockedKey, false)
        if (alreadyUnlocked) {
            return ShinyEncounter(
                isShiny = true,
                attempts = prefs.getInt(attemptsKey, 1)
            )
        }

        val attempts = prefs.getInt(attemptsKey, 0) + 1
        val isShiny = Random.nextInt(10) == 0

        prefs.edit()
            .putInt(attemptsKey, attempts)
            .putBoolean(unlockedKey, isShiny)
            .apply()

        return ShinyEncounter(isShiny = isShiny, attempts = attempts)
    }

    companion object {
        private const val PREFS_NAME = "shiny_encounters"

        private fun attemptsKey(pokemonId: Int): String = "attempts_$pokemonId"
        private fun unlockedKey(pokemonId: Int): String = "unlocked_$pokemonId"
    }
}

data class ShinyEncounter(
    val isShiny: Boolean,
    val attempts: Int
)

