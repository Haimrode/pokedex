package com.example.pokedex.domain.model

/**
 * Modèle métier de la **fiche détaillée** d'un Pokémon.
 * Compose un [Pokemon] de base + les informations spécifiques au détail.
 *
 * @property pokemon   Infos de base (réutilisé tel quel depuis la liste)
 * @property height    Taille en **mètres** (déjà converti depuis les décimètres de l'API)
 * @property weight    Poids en **kilogrammes** (déjà converti depuis les hectogrammes de l'API)
 * @property abilities Capacités ("static", "lightning-rod", ...)
 * @property stats     Statistiques de base
 */
data class PokemonDetail(
    val pokemon: Pokemon,
    val height: Double,
    val weight: Double,
    val abilities: List<String>,
    val stats: PokemonStats
)

/**
 * Statistiques de base d'un Pokémon (valeur max théorique : 255).
 */
data class PokemonStats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int
)
