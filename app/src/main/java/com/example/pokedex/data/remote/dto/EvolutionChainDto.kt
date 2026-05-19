package com.example.pokedex.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO renvoyé par `GET /evolution-chain/{id}`.
 *
 * Structure **récursive** : chaque maillon ([ChainLinkDto]) contient le
 * Pokémon courant et la liste de ses évolutions directes. Pour trouver
 * le stade d'évolution d'un Pokémon donné, on parcourt l'arbre depuis
 * la racine et on retourne la profondeur où on le rencontre :
 *   - racine = stade 1 (forme de base)
 *   - 1er niveau = stade 2
 *   - 2e niveau = stade 3
 *
 * Exemple Pichu → Pikachu → Raichu :
 * ```json
 * {
 *   "id": 10,
 *   "chain": {
 *     "species": { "name": "pichu", "url": "..." },
 *     "evolves_to": [{
 *       "species": { "name": "pikachu", "url": "..." },
 *       "evolves_to": [{
 *         "species": { "name": "raichu", "url": "..." },
 *         "evolves_to": []
 *       }]
 *     }]
 *   }
 * }
 * ```
 *
 * Cas particuliers à anticiper côté mapper :
 *  - Eevee : `evolves_to` contient 8 branches (Aquali, Pyroli, Voltali…)
 *  - Pokémon sans évolution (Tauros, Lapras…) : racine seule, profondeur 1
 */
data class EvolutionChainDto(
    val id: Int,
    val chain: ChainLinkDto,
)

data class ChainLinkDto(
    val species: NamedResourceDto,
    @SerializedName("evolves_to") val evolvesTo: List<ChainLinkDto>,
)
