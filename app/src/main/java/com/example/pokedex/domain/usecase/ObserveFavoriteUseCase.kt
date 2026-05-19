package com.example.pokedex.domain.usecase

import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.repository.FavoriteRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    operator fun invoke(id: Int): Flow<Pokemon?> = repository.observeFavorite(id)
}

