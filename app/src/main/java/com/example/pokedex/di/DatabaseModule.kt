package com.example.pokedex.di

import android.content.Context
import androidx.room.Room
import com.example.pokedex.data.local.FavoritePokemonDao
import com.example.pokedex.data.local.PokedexDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "pokedex_database"

    @Provides
    @Singleton
    fun providePokedexDatabase(
        @ApplicationContext context: Context
    ): PokedexDatabase = Room.databaseBuilder(
        context,
        PokedexDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Provides
    fun provideFavoritePokemonDao(database: PokedexDatabase): FavoritePokemonDao =
        database.favoritePokemonDao()
}

