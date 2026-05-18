package com.example.pokedex.domain.usecase

import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.repository.FavoriteRepository
import javax.inject.Inject

/**
 * Use case : bascule l'état favori d'un Pokémon.
 *
 * Le caller (typiquement le bouton ❤️ de l'écran détail) lui passe
 * l'état actuel — le use case décide d'add ou de remove en conséquence.
 *
 * Idempotent : retoggle deux fois revient à l'état initial.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    suspend operator fun invoke(pokemon: Pokemon, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            repository.removeFavorite(pokemon.id)
        } else {
            repository.addFavorite(pokemon)
        }
    }
}
