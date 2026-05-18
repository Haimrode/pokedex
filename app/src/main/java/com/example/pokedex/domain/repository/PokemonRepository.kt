package com.example.pokedex.domain.repository

import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.model.PokemonDetail

/**
 * Contrat d'accès aux données Pokémon distantes (PokéAPI).
 *
 * **Interface dans `domain/`**, **implémentation dans `data/repository/`**.
 * Cette inversion permet :
 *   - de tester les ViewModels avec un fake repository sans réseau
 *   - de changer de source de données sans toucher au reste du code
 *   - de respecter la règle Clean Archi : domain ne dépend de rien
 *
 * Les méthodes retournent [Result] de Kotlin pour propager les erreurs
 * réseau de manière explicite (pas d'exception silencieuse).
 * La transformation Result → UiState se fait dans le ViewModel.
 */
interface PokemonRepository {

    /** Récupère les [limit] premiers Pokémons (par défaut 100). */
    suspend fun getPokemonList(limit: Int = 100): Result<List<Pokemon>>

    /** Récupère la fiche détaillée d'un Pokémon par son [id] Pokédex. */
    suspend fun getPokemonDetail(id: Int): Result<PokemonDetail>
}
