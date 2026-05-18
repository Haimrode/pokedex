package com.example.pokedex.di

import com.example.pokedex.data.repository.PokemonRepositoryImpl
import com.example.pokedex.domain.repository.PokemonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt : indique à Hilt que **quand on demande `PokemonRepository`,
 * il faut injecter `PokemonRepositoryImpl`**.
 *
 * Pourquoi `@Binds` plutôt que `@Provides` ?
 *   - L'impl est déjà annotée `@Inject constructor`, donc Hilt sait la construire.
 *   - `@Binds` ne fait que dire "interface = impl", sans code de construction.
 *   - Plus rapide à la compilation que `@Provides`.
 *
 * Pourquoi `abstract class` ?
 *   - Hilt l'exige pour les modules `@Binds`. La méthode aussi est `abstract`.
 *   - Il n'y a aucune implémentation à fournir : Hilt génère tout.
 *
 * Pierre fera un module équivalent `FavoriteRepositoryModule` pour son côté.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PokemonRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPokemonRepository(
        impl: PokemonRepositoryImpl
    ): PokemonRepository
}
