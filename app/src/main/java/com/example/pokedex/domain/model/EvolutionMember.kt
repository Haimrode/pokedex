package com.example.pokedex.domain.model

/**
 * Un membre de la chaîne d'évolution d'un Pokémon, dans l'ordre canonique
 * (forme de base → évolutions successives). Pour les chaînes branchues
 * (Eevee → 8 évolutions), tous les membres apparaissent à plat.
 *
 * Utilisé par la fiche détail pour afficher "la famille" d'un Pokémon.
 */
data class EvolutionMember(
    val id: Int,
    val name: String,
    val spriteUrl: String,
)
