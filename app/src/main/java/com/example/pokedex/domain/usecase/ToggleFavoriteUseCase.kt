package com.example.pokedex.domain.usecase

import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.repository.FavoriteRepository
import javax.inject.Inject

/**
 * Use case : bascule l'état favori d'un Pokémon.
 *
 * Comportement : ajoute si absent, retire si présent. **C'est un toggle pur,
 * basé uniquement sur l'id** — peu importe que le sprite affiché soit normal
 * ou shiny au moment du clic, le user veut juste retirer son favori.
 *
 * Idempotent : retoggle deux fois revient à l'état initial.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    suspend operator fun invoke(pokemon: Pokemon, currentFavorite: Pokemon?) {
        if (currentFavorite == null) {
            repository.addFavorite(pokemon)
        } else {
            repository.removeFavorite(pokemon.id)
        }
    }
}
