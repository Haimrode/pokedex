package com.example.pokedex.data.remote.mapper

import com.example.pokedex.data.remote.dto.ChainLinkDto
import com.example.pokedex.data.remote.dto.EvolutionChainDto
import com.example.pokedex.data.remote.dto.PokemonDetailDto
import com.example.pokedex.data.remote.dto.PokemonSpeciesDto
import com.example.pokedex.domain.model.Generation
import com.example.pokedex.domain.model.PokemonGameData

/**
 * Frontière data → domain pour les données du Pokémondle.
 *
 * Combine les 3 DTOs (pokemon, species, evolution-chain) en un seul
 * [PokemonGameData] propre, prêt à consommer côté ViewModel / UI.
 *
 * Conventions :
 *  - hauteur en mètres (PokéAPI donne en décimètres)
 *  - poids en kg (PokéAPI donne en hectogrammes)
 *  - stade d'évolution = profondeur dans la chaîne d'évolution
 */
fun PokemonDetailDto.toGameData(
    species: PokemonSpeciesDto,
    chain: EvolutionChainDto,
): PokemonGameData {
    val sortedTypes = types.sortedBy { it.slot }
    val generation = Generation.fromApiName(species.generation.name)
        ?: Generation.fromPokedexId(id)
        ?: Generation.GEN_1

    return PokemonGameData(
        id = id,
        name = name.replaceFirstChar { it.uppercase() },
        spriteUrl = sprites.other?.officialArtwork?.frontDefault
            ?: sprites.frontDefault
            ?: "",
        type1 = sortedTypes.first().type.name,
        type2 = sortedTypes.getOrNull(1)?.type?.name,
        height = height / 10.0,
        weight = weight / 10.0,
        generation = generation,
        color = species.color.name,
        shape = species.shape?.name ?: "unknown",
        evolutionStage = findEvolutionStage(chain.chain, species.name) ?: 1,
    )
}

/**
 * Cherche récursivement le Pokémon dans la chaîne et renvoie sa profondeur.
 *
 * Profondeur 1 = forme de base (Pichu)
 * Profondeur 2 = 1re évolution  (Pikachu)
 * Profondeur 3 = forme finale   (Raichu)
 *
 * Tolère les chaînes branchues (Eevee → Aquali/Pyroli/Voltali/…) :
 * `firstNotNullOfOrNull` explore les branches en parallèle dans l'arbre.
 */
private fun findEvolutionStage(
    node: ChainLinkDto,
    targetName: String,
    depth: Int = 1,
): Int? {
    if (node.species.name == targetName) return depth
    return node.evolvesTo
        .firstNotNullOfOrNull { findEvolutionStage(it, targetName, depth + 1) }
}
