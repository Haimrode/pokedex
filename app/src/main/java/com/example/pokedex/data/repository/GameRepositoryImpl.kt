package com.example.pokedex.data.repository

import com.example.pokedex.data.remote.PokemonApi
import com.example.pokedex.data.remote.mapper.toGameData
import com.example.pokedex.domain.model.PokemonGameData
import com.example.pokedex.domain.model.PokemonRef
import com.example.pokedex.domain.repository.GameRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implémentation réseau de [GameRepository].
 *
 * Deux opérations distinctes :
 *  - [getAllPokemonNames] : 1 seule requête légère (~50 KB) pour récupérer
 *    les 1025 noms — alimente le tirage au sort et l'autocomplétion.
 *  - [getGameData] : 3 endpoints combinés pour une fiche complète.
 *
 * **Optimisation parallèle de [getGameData]** :
 *   /pokemon et /pokemon-species sont indépendants → lancés en parallèle
 *   via `async`. /evolution-chain dépend de l'url contenue dans /species,
 *   donc séquentiel après. On gagne ~30 % vs 3 appels enchaînés.
 */
@Singleton
class GameRepositoryImpl @Inject constructor(
    private val api: PokemonApi
) : GameRepository {

    override suspend fun getAllPokemonNames(): Result<List<PokemonRef>> = safeApiCall {
        api.getPokemonList(limit = TOTAL_POKEMONS, offset = 0)
            .results
            .map { entry -> PokemonRef(id = entry.extractId(), name = entry.name) }
    }

    override suspend fun getGameData(id: Int): Result<PokemonGameData> = safeApiCall {
        coroutineScope {
            val pokemonDeferred = async { api.getPokemonDetail(id) }
            val speciesDeferred = async { api.getPokemonSpecies(id) }
            val pokemon = pokemonDeferred.await()
            val species = speciesDeferred.await()
            val chain = api.getEvolutionChain(species.evolutionChain.extractId())
            pokemon.toGameData(species, chain)
        }
    }

    /**
     * Voir le commentaire de [PokemonRepositoryImpl.safeApiCall] pour le
     * pourquoi du `throw CancellationException` et pas de `runCatching`.
     */
    private suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }

    private companion object {
        const val TOTAL_POKEMONS = 1025
    }
}
