package com.example.pokedex.domain.model

/**
 * Modèle métier d'un Pokémon dans la **liste** du Pokédex.
 * Volontairement minimal : seulement ce que la liste affiche.
 * Pour le détail (stats, taille, poids, etc.) → voir [PokemonDetail].
 *
 * @property id        Numéro Pokédex (ex : 25 pour Pikachu)
 * @property name      Nom en minuscules tel que renvoyé par l'API ("pikachu")
 * @property types     Liste des types ("electric", "fire", ...). 1 ou 2 éléments.
 * @property spriteUrl URL de l'image officielle (officiel artwork de préférence)
 */
data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<String>,
    val spriteUrl: String
)
