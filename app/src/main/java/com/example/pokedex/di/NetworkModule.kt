package com.example.pokedex.di

import com.example.pokedex.data.remote.PokemonApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Module Hilt : fournit toutes les dépendances liées au réseau.
 *
 * `@InstallIn(SingletonComponent::class)` = ces dépendances vivent au niveau
 * de l'application entière (créées une fois, partagées partout).
 *
 * `@Provides` = "voici comment construire cet objet", à appeler par Hilt
 * quand quelqu'un en demande un.
 *
 * `@Singleton` = ne pas re-construire à chaque injection — une seule instance.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    /**
     * Client HTTP partagé.
     * On y branche le HttpLoggingInterceptor pour voir les requêtes
     * dans le Logcat pendant le debug.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Instance Retrofit configurée pour PokéAPI + parsing JSON via Gson.
     */
    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /**
     * Implémentation concrète de [PokemonApi] générée par Retrofit.
     * C'est cette instance que [com.example.pokedex.data.repository.PokemonRepositoryImpl] recevra.
     */
    @Provides
    @Singleton
    fun providePokemonApi(retrofit: Retrofit): PokemonApi =
        retrofit.create(PokemonApi::class.java)
}
