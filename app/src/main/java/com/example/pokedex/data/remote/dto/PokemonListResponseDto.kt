package com.example.pokedex.data.remote.dto

/**
 * Réponse de `GET /pokemon?limit=N&offset=M`.
 *
 * PokéAPI renvoie seulement nom + URL pour chaque entrée de liste —
 * il faut ensuite appeler `/pokemon/{id}` pour récupérer types, sprites, etc.
 *
 * Exemple :
 * ```json
 * {
 *   "count": 1302,
 *   "results": [
 *     { "name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon/1/" }
 *   ]
 * }
 * ```
 */
data class PokemonListResponseDto(
    val count: Int,
    val results: List<PokemonListItemDto>
)

data class PokemonListItemDto(
    val name: String,
    val url: String
) {
    /**
     * Extrait l'id Pokédex depuis l'URL : `.../pokemon/25/` → `25`.
     * On en a besoin parce que la liste ne contient pas l'id explicitement.
     */
    fun extractId(): Int =
        url.trimEnd('/').substringAfterLast('/').toInt()
}
