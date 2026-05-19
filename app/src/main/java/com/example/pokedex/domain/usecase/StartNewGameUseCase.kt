package com.example.pokedex.domain.usecase

import com.example.pokedex.domain.model.PokemonGameData
import com.example.pokedex.domain.repository.GameRepository
import javax.inject.Inject
import kotlin.random.Random

/**
 * Use case : démarre une nouvelle partie de Pokémondle.
 *
 * Tire un id aléatoire dans `1..1025` et fetch les données complètes du
 * Pokémon correspondant — il devient la cible mystère que le joueur doit
 * deviner.
 *
 * La logique du tirage est ici (et pas dans le ViewModel) pour respecter
 * Clean Archi : "comment on choisit la cible" = règle métier du jeu.
 */
class StartNewGameUseCase @Inject constructor(
    private val repository: GameRepository
) {
    suspend operator fun invoke(): Result<PokemonGameData> {
        val id = Random.nextInt(1, TOTAL_POKEMONS + 1)
        return repository.getGameData(id)
    }

    private companion object {
        const val TOTAL_POKEMONS = 1025
    }
}
