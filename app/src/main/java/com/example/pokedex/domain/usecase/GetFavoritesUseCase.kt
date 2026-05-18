package com.example.pokedex.domain.usecase

import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case : observer la liste des favoris.
 *
 * Retourne directement le [Flow] de [FavoriteRepository] sans `suspend` —
 * un Flow est par construction asynchrone, le ViewModel n'a qu'à le collect.
 */
class GetFavoritesUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    operator fun invoke(): Flow<List<Pokemon>> = repository.getFavorites()
}
