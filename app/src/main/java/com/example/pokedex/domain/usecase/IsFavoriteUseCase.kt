package com.example.pokedex.domain.usecase

import com.example.pokedex.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case : observer en réactif si un Pokémon est dans les favoris.
 *
 * Retourne un [Flow] : l'écran détail s'abonne et son icône ❤️ se met à jour
 * automatiquement quand l'user appuie sur le bouton (ou si la BDD change
 * depuis ailleurs).
 */
class IsFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    operator fun invoke(id: Int): Flow<Boolean> = repository.isFavorite(id)
}
