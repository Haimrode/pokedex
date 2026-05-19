package com.example.pokedex.data.repository

import com.example.pokedex.data.remote.PokemonApi
import com.example.pokedex.data.remote.mapper.toDomainDetail
import com.example.pokedex.data.remote.mapper.toDomainPokemon
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.repository.PokemonRepository
import com.example.pokedex.data.cache.PokemonCache
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implémentation réseau de [PokemonRepository] : parle à PokéAPI via Retrofit.
 *
 * **Stratégie d'appel pour [getPokemonList]** :
 *  - PokéAPI sépare la liste (nom + url) et le détail (types, sprites, stats…).
 *  - Pour afficher types + image dans la liste, on doit donc fetch chaque détail.
 *  - On fait les N appels **en parallèle** via [coroutineScope] + [async] :
 *    100 requêtes lancées en même temps → temps total ≈ requête la plus lente.
 *  - [awaitAll] préserve l'ordre des entrées, donc la liste reste triée par id.
 *
 * **Gestion d'erreur** : on wrap dans [Result] via le helper [safeApiCall].
 * Le ViewModel décide quoi en faire (mapping vers UiState.Error, retry, etc.).
 */
@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val api: PokemonApi
) : PokemonRepository {

    override suspend fun getPokemonList(limit: Int): Result<List<Pokemon>> = safeApiCall {
        // Return cached list if available to speed up app launch / reduce network usage
        PokemonCache.get() ?: run {
            val listResponse = api.getPokemonList(limit = limit)
            coroutineScope {
                listResponse.results
                    .map { entry ->
                        async { api.getPokemonDetail(entry.extractId()).toDomainPokemon() }
                    }
                    .awaitAll().also { fetched ->
                        PokemonCache.set(fetched)
                    }
            }
        }
    }

    override suspend fun getPokemonDetail(id: Int): Result<PokemonDetail> = safeApiCall {
        api.getPokemonDetail(id).toDomainDetail()
    }

    /**
     * Wrap un appel API dans [Result], en relayant correctement les annulations.
     *
     * **Important** : on `throw` toujours [CancellationException] sans la convertir
     * en Result.failure — sinon l'annulation de la coroutine ne propage plus
     * (= leak, comportement bizarre). C'est un piège classique de [runCatching].
     */
    private suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }
}
