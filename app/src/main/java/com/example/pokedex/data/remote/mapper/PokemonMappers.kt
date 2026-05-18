package com.example.pokedex.data.remote.mapper

import com.example.pokedex.data.remote.dto.PokemonDetailDto
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.model.PokemonStats

/**
 * Frontière entre la couche `data` et la couche `domain`.
 *
 * Les DTOs portent la structure brute de PokéAPI (avec ses verbosités, ses
 * sous-objets imbriqués, ses nullabilités). Les modèles `domain` sont propres
 * et minimaux. C'est ici que la conversion a lieu.
 *
 * Règle : **le reste du code ne doit jamais voir un DTO**.
 */

/** Extrait un [Pokemon] (modèle liste) depuis un détail brut PokéAPI. */
fun PokemonDetailDto.toDomainPokemon(): Pokemon = Pokemon(
    id = id,
    name = name,
    types = types
        .sortedBy { it.slot } // garde l'ordre type1, type2
        .map { it.type.name },
    spriteUrl = pickBestSpriteUrl()
)

/** Construit un [PokemonDetail] complet depuis le DTO.
 *
 *  Conversions d'unités PokéAPI → unités SI :
 *  - height : décimètres → mètres (`/10`)
 *  - weight : hectogrammes → kilogrammes (`/10`)
 *
 *  Le domain expose donc des valeurs directement utilisables, ce qui
 *  protège le ViewModel et l'UI des conventions de PokéAPI.
 */
fun PokemonDetailDto.toDomainDetail(): PokemonDetail = PokemonDetail(
    pokemon = toDomainPokemon(),
    height = height / 10.0,
    weight = weight / 10.0,
    abilities = abilities
        .sortedBy { it.slot }
        .map { it.ability.name },
    stats = mapStats()
)

/**
 * Choisit le meilleur sprite disponible.
 * Priorité : artwork officiel (haute résolution) > sprite par défaut > placeholder vide.
 */
private fun PokemonDetailDto.pickBestSpriteUrl(): String =
    sprites.other?.officialArtwork?.frontDefault
        ?: sprites.frontDefault
        ?: ""

/**
 * PokéAPI renvoie les stats dans un List<StatDto> avec un nom textuel pour chacune.
 * On les indexe par nom puis on construit notre [PokemonStats] type-safe.
 * Si une stat manque (improbable), on tombe à 0 — meilleur que de crasher.
 */
private fun PokemonDetailDto.mapStats(): PokemonStats {
    val byName: Map<String, Int> = stats.associate { it.stat.name to it.baseStat }
    return PokemonStats(
        hp = byName["hp"] ?: 0,
        attack = byName["attack"] ?: 0,
        defense = byName["defense"] ?: 0,
        specialAttack = byName["special-attack"] ?: 0,
        specialDefense = byName["special-defense"] ?: 0,
        speed = byName["speed"] ?: 0
    )
}
