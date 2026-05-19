package com.example.pokedex.domain.usecase

import com.example.pokedex.domain.model.PokemonRef
import com.example.pokedex.domain.repository.GameRepository
import javax.inject.Inject

/**
 * Use case : récupère les 1025 noms du Pokédex en une requête légère.
 *
 * Alimente :
 *  - le tirage au sort de la cible (random sur la liste)
 *  - l'autocomplétion du champ de saisie côté UI
 */
class GetAllPokemonNamesUseCase @Inject constructor(
    private val repository: GameRepository
) {
    suspend operator fun invoke(): Result<List<PokemonRef>> =
        repository.getAllPokemonNames()
}
