package com.example.pokedex.di

import com.example.pokedex.data.repository.GameRepositoryImpl
import com.example.pokedex.domain.repository.GameRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt : lie l'interface [GameRepository] à son implémentation réseau.
 *
 * Même pattern que [PokemonRepositoryModule] — `@Binds` parce que l'impl
 * a déjà un `@Inject constructor`, donc Hilt n'a pas besoin d'une factory.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class GameRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGameRepository(
        impl: GameRepositoryImpl
    ): GameRepository
}
