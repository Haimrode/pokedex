package com.example.pokedex.domain.model

/**
 * Toutes les informations affichables pour un Pokémon dans le Pokémondle.
 *
 * Combine 3 endpoints PokéAPI :
 *  - `/pokemon/{id}`           → id, name, types, height, weight, sprite
 *  - `/pokemon-species/{id}`   → generation, color, shape
 *  - `/evolution-chain/{id}`   → evolutionStage (1, 2 ou 3)
 *
 * Distinct de [Pokemon] (utilisé pour la liste du Pokédex) et de
 * [PokemonDetail] (fiche détail), pour ne pas couplér le jeu à ces écrans.
 */
data class PokemonGameData(
    val id: Int,
    val name: String,
    val spriteUrl: String,
    val type1: String,
    val type2: String?,
    val height: Double,
    val weight: Double,
    val generation: Generation,
    val color: String,
    val shape: String,
    val evolutionStage: Int,
)

/**
 * Référence légère d'un Pokémon (id + nom).
 *
 * Le Pokémondle charge **les 1025 noms d'un coup** (1 requête, ~50 KB) pour :
 *  - tirer au sort la cible
 *  - alimenter l'autocomplétion du champ de saisie
 *
 * Le détail complet n'est fetché que pour la cible et les guesses du joueur.
 */
data class PokemonRef(
    val id: Int,
    val name: String,
)
