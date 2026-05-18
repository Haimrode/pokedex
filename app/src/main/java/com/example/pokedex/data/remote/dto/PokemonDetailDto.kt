package com.example.pokedex.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Réponse de `GET /pokemon/{id}`. Très riche — on prend seulement ce dont on a besoin.
 *
 * Les annotations [SerializedName] sont indispensables pour deux raisons :
 *  1. Mapper le snake_case du JSON vers le camelCase Kotlin (front_default → frontDefault)
 *  2. Gérer les noms avec tiret invalides en Kotlin (official-artwork, special-attack)
 */
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: SpritesDto,
    val types: List<TypeSlotDto>,
    val abilities: List<AbilitySlotDto>,
    val stats: List<StatDto>
)

data class SpritesDto(
    @SerializedName("front_default") val frontDefault: String?,
    val other: OtherSpritesDto?
)

data class OtherSpritesDto(
    @SerializedName("official-artwork") val officialArtwork: OfficialArtworkDto?
)

data class OfficialArtworkDto(
    @SerializedName("front_default") val frontDefault: String?
)

data class TypeSlotDto(
    val slot: Int,
    val type: NamedResourceDto
)

data class AbilitySlotDto(
    val ability: NamedResourceDto,
    @SerializedName("is_hidden") val isHidden: Boolean,
    val slot: Int
)

data class StatDto(
    @SerializedName("base_stat") val baseStat: Int,
    val effort: Int,
    val stat: NamedResourceDto
)

/**
 * Référence générique vers une autre ressource PokéAPI.
 * Utilisée partout : types, abilities, stats, etc.
 */
data class NamedResourceDto(
    val name: String,
    val url: String
)
