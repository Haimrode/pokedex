package com.example.pokedex.domain.usecase

import com.example.pokedex.domain.model.EvolutionMember
import com.example.pokedex.domain.repository.PokemonRepository
import javax.inject.Inject

/**
 * Use case : récupère la famille évolutive d'un Pokémon.
 *
 * Utilisé par la fiche détail pour afficher la lignée (Bulbi → Herbi →
 * Florizarre, ou les 8 évolutions d'Évoli, ou juste lui-même si solo).
 */
class GetEvolutionFamilyUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(pokemonId: Int): Result<List<EvolutionMember>> =
        repository.getEvolutionFamily(pokemonId)
}
