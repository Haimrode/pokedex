package com.example.pokedex.domain.usecase

import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.repository.FavoriteRepository
import javax.inject.Inject

/**
 * Use case : bascule l'état favori d'un Pokémon.
 *
 * Le caller passe `isFavorite` (un Boolean issu d'un flow réellement
 * collecté) — pas un Pokemon nullable, parce qu'un flow non-collecté peut
 * reste à sa valeur initiale et fausser la décision.
 *
 * Idempotent : retoggle deux fois revient à l'état initial.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    suspend operator fun invoke(pokemon: Pokemon, isFavorite: Boolean) {
        if (isFavorite) {
            repository.removeFavorite(pokemon.id)
        } else {
            repository.addFavorite(pokemon)
        }
    }
}
