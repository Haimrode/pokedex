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
            // Première fois qu'on débloque un shiny → on l'affiche par défaut
            // (le user voit sa récompense). Il pourra le désactiver après.
            .apply {
                if (isShiny) putBoolean(displayKey(pokemonId), true)
            }
            .apply()

        return ShinyEncounter(isShiny = isShiny, attempts = attempts)
    }

    /** `true` si l'utilisateur a déjà débloqué le shiny pour ce Pokémon. */
    fun isShinyUnlocked(pokemonId: Int): Boolean =
        prefs.getBoolean(unlockedKey(pokemonId), false)

    /**
     * `true` si l'utilisateur veut afficher la version shiny actuellement.
     * Pertinent uniquement si [isShinyUnlocked] est `true`. Default = `true`
     * (on montre le shiny une fois débloqué).
     */
    fun isShinyDisplayed(pokemonId: Int): Boolean =
        prefs.getBoolean(displayKey(pokemonId), true)

    /** Bascule l'affichage shiny on/off (rien si pas encore débloqué). */
    fun setShinyDisplayed(pokemonId: Int, show: Boolean) {
        if (!isShinyUnlocked(pokemonId)) return
        prefs.edit().putBoolean(displayKey(pokemonId), show).apply()
    }

    /** Nombre de tentatives effectuées avant le déblocage. */
    fun attemptsFor(pokemonId: Int): Int =
        prefs.getInt(attemptsKey(pokemonId), 0)

    companion object {
        private const val PREFS_NAME = "shiny_encounters"

        private fun attemptsKey(pokemonId: Int): String = "attempts_$pokemonId"
        private fun unlockedKey(pokemonId: Int): String = "unlocked_$pokemonId"
        private fun displayKey(pokemonId: Int): String = "display_$pokemonId"
    }
}

data class ShinyEncounter(
    val isShiny: Boolean,
    val attempts: Int
)

