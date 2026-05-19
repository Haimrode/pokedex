package com.example.pokedex.data.remote

import com.example.pokedex.data.remote.dto.EvolutionChainDto
import com.example.pokedex.data.remote.dto.PokemonDetailDto
import com.example.pokedex.data.remote.dto.PokemonListResponseDto
import com.example.pokedex.data.remote.dto.PokemonSpeciesDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Client Retrofit pour PokéAPI v2.
 *
 * Tu n'écris **que cette interface** : Retrofit génère l'implémentation
 * concrète à la compilation. Chaque méthode = un endpoint HTTP.
 *
 * Documentation officielle de l'API : https://pokeapi.co/docs/v2
 */
interface PokemonApi {

    /**
     * Liste paginée des Pokémons.
     * Le DTO renvoyé ne contient que name + url ; il faut appeler
     * [getPokemonDetail] pour récupérer types, sprites, stats, etc.
     */
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): PokemonListResponseDto

    /**
     * Fiche détaillée d'un Pokémon donné.
     * @param id Numéro Pokédex (1 = Bulbasaur, 25 = Pikachu, etc.)
     */
    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") id: Int): PokemonDetailDto

    /**
     * Fiche "espèce" — infos encyclopédiques (couleur, forme, génération,
     * habitat, légendaire/mythique, url de la chaîne d'évolution).
     * Utilisé pour le Pokémondle pour comparer les Pokémons sur ces axes.
     *
     * @param id Numéro Pokédex — mêmes ids que [getPokemonDetail].
     */
    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): PokemonSpeciesDto

    /**
     * Chaîne d'évolution complète (récursive).
     * L'id n'est PAS le numéro Pokédex mais l'id de la chaîne, obtenu via
     * `species.evolutionChain.extractId()`.
     */
    @GET("evolution-chain/{id}")
    suspend fun getEvolutionChain(@Path("id") id: Int): EvolutionChainDto
}
