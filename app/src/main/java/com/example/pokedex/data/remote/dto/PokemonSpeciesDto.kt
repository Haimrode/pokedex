package com.example.pokedex.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO renvoyé par `GET /pokemon-species/{id}`.
 *
 * `/pokemon/{id}` donne les stats de combat (types, hauteur, poids, stats…).
 * `/pokemon-species/{id}` complète avec les infos "encyclopédiques" qu'on
 * utilise pour les colonnes du Pokémondle : génération, couleur, forme,
 * et l'URL de la chaîne d'évolution (à parser dans un second appel).
 *
 * Champs non utilisés (~30 sur l'endpoint) volontairement omis : Gson les
 * ignore silencieusement, c'est ce qu'on veut.
 *
 * Exemple Pikachu :
 * ```json
 * {
 *   "id": 25,
 *   "name": "pikachu",
 *   "color":   { "name": "yellow",     "url": "..." },
 *   "shape":   { "name": "quadruped",  "url": "..." },
 *   "habitat": { "name": "forest",     "url": "..." },   // null en Gen 6+
 *   "generation": { "name": "generation-i", "url": ".../generation/1/" },
 *   "evolution_chain": { "url": ".../evolution-chain/10/" },
 *   "is_legendary": false,
 *   "is_mythical":  false
 * }
 * ```
 */
data class PokemonSpeciesDto(
    val id: Int,
    val name: String,
    val color: NamedResourceDto,
    val shape: NamedResourceDto?,
    val habitat: NamedResourceDto?,
    val generation: NamedResourceDto,
    @SerializedName("evolution_chain") val evolutionChain: EvolutionChainRefDto,
    @SerializedName("is_legendary") val isLegendary: Boolean,
    @SerializedName("is_mythical") val isMythical: Boolean,
)

/**
 * Référence vers la chaîne d'évolution. Contrairement à [NamedResourceDto],
 * cet objet ne contient **que** `url` — il faut donc un type dédié.
 *
 * Pour récupérer l'id de la chaîne : `url = ".../evolution-chain/10/"` → 10.
 */
data class EvolutionChainRefDto(
    val url: String,
) {
    fun extractId(): Int =
        url.trimEnd('/').substringAfterLast('/').toInt()
}
