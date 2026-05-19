package com.example.pokedex.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Source locale des noms français des Pokémons.
 *
 * Charge `assets/fr_names.json` (généré une fois depuis PokéAPI via un
 * script Node) en mémoire au premier accès. Le map est gardé en singleton
 * Hilt pour le reste de la vie de l'app.
 *
 * **Pourquoi un asset plutôt que /pokemon-species/{id} × 1025 au démarrage ?**
 *  - 1025 requêtes en parallèle au lancement = ~20-30s + rate limit risk
 *  - L'asset embarqué pèse ~18 KB, gratuit et instantané
 *  - Aucun appel réseau requis pour l'autocomplétion
 *
 * Utilisé par les repositories pour remplacer les noms EN renvoyés par
 * PokéAPI par leur équivalent FR avant remontée au domain.
 */
@Singleton
class PokemonNameLocalizer @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val names: Map<Int, String> by lazy { loadFromAssets() }

    /**
     * Renvoie le nom FR du Pokémon [id], ou [fallback] capitalisé si absent.
     * Le fallback est typiquement le nom EN renvoyé par PokéAPI.
     */
    fun localize(id: Int, fallback: String): String =
        names[id] ?: fallback.replaceFirstChar { it.uppercase() }

    private fun loadFromAssets(): Map<Int, String> {
        val raw = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        val json = JSONObject(raw)
        return buildMap(json.length()) {
            json.keys().forEach { key ->
                put(key.toInt(), json.getString(key))
            }
        }
    }

    private companion object {
        const val ASSET_NAME = "fr_names.json"
    }
}
