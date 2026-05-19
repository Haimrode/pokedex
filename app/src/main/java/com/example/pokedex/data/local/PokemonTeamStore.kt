package com.example.pokedex.data.local

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonTeamStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTeamIds(): List<Int> = prefs.getString(TEAM_KEY, null)
        ?.split(',')
        ?.mapNotNull { it.toIntOrNull() }
        ?.distinct()
        ?.take(MAX_TEAM_SIZE)
        .orEmpty()

    fun setTeamIds(teamIds: List<Int>) {
        prefs.edit {
            putString(
                TEAM_KEY,
                teamIds.distinct().take(MAX_TEAM_SIZE).joinToString(",")
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "pokemon_team"
        private const val TEAM_KEY = "team_ids"
        const val MAX_TEAM_SIZE = 6
    }
}

