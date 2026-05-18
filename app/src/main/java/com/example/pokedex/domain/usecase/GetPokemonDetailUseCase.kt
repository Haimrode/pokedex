package com.example.pokedex.domain.usecase

import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.repository.PokemonRepository
import javax.inject.Inject

/**
 * Use case : récupérer la fiche détaillée d'un Pokémon par son id.
 *
 * Voir [GetPokemonListUseCase] pour la justification du pattern.
 */
class GetPokemonDetailUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(id: Int): Result<PokemonDetail> =
        repository.getPokemonDetail(id)
}
