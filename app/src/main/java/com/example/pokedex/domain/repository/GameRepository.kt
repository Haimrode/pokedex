package com.example.pokedex.domain.repository

import com.example.pokedex.domain.model.PokemonGameData
import com.example.pokedex.domain.model.PokemonRef

/**
 * Contrat d'accès aux données nécessaires au Pokémondle.
 *
 * Séparé de [PokemonRepository] parce que :
 *  - les données nécessaires sont différentes (combine 3 endpoints)
 *  - les responsabilités sont différentes (jeu vs encyclopédie)
 *  - on peut faire évoluer / tester / mocker les deux indépendamment
 */
interface GameRepository {

    /**
     * Récupère **uniquement les noms** des 1025 Pokémons en une requête.
     * Utilisé pour le tirage au sort et l'autocomplétion.
     */
    suspend fun getAllPokemonNames(): Result<List<PokemonRef>>

    /**
     * Récupère **toutes** les informations affichables d'un Pokémon pour
     * le jeu (combine /pokemon, /pokemon-species et /evolution-chain).
     */
    suspend fun getGameData(id: Int): Result<PokemonGameData>
}
