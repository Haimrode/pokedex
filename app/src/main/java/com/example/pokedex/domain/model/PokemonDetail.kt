package com.example.pokedex.domain.model

/**
 * Modèle métier de la **fiche détaillée** d'un Pokémon.
 * Compose un [Pokemon] de base + les informations spécifiques au détail.
 *
 * @property pokemon   Infos de base (réutilisé tel quel depuis la liste)
 * @property height    Taille en décimètres (unité PokéAPI). 7 = 0.7m.
 * @property weight    Poids en hectogrammes (unité PokéAPI). 60 = 6.0kg.
 * @property abilities Capacités ("static", "lightning-rod", ...)
 * @property stats     Statistiques de base
 */
data class PokemonDetail(
    val pokemon: Pokemon,
    val height: Int,
    val weight: Int,
    val abilities: List<String>,
    val stats: PokemonStats
)

/**
 * Statistiques de base d'un Pokémon.
 * Champs nommés explicitement (plutôt que Map<String, Int>) pour la sécurité type.
 */
data class PokemonStats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int
)
