package com.example.pokedex.data.repository

import com.example.pokedex.data.local.PokemonNameLocalizer
import com.example.pokedex.data.remote.PokemonApi
import com.example.pokedex.data.remote.dto.ChainLinkDto
import com.example.pokedex.data.remote.mapper.toDomainDetail
import com.example.pokedex.data.remote.mapper.toDomainPokemon
import com.example.pokedex.domain.model.EvolutionMember
import com.example.pokedex.domain.model.Generation
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.repository.PokemonRepository
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
 * **Localisation FR** : chaque nom passé par `localizer.localize(id, fallback)`
 * avant de remonter au domain. L'UI voit donc "Bulbizarre" et plus "bulbasaur".
 *
 * **Gestion d'erreur** : on wrap dans [Result] via le helper [safeApiCall].
 * Le ViewModel décide quoi en faire (mapping vers UiState.Error, retry, etc.).
 */
@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val api: PokemonApi,
    private val localizer: PokemonNameLocalizer,
) : PokemonRepository {

    override suspend fun getPokemonList(generation: Generation): Result<List<Pokemon>> = safeApiCall {
        val listResponse = api.getPokemonList(
            limit = generation.count,
            offset = generation.offset,
        )
        coroutineScope {
            listResponse.results
                .map { entry ->
                    async {
                        val p = api.getPokemonDetail(entry.extractId()).toDomainPokemon()
                        p.copy(name = localizer.localize(p.id, p.name))
                    }
                }
                .awaitAll()
        }
    }

    override suspend fun getPokemonDetail(id: Int): Result<PokemonDetail> = safeApiCall {
        val detail = api.getPokemonDetail(id).toDomainDetail()
        detail.copy(
            pokemon = detail.pokemon.copy(
                name = localizer.localize(detail.pokemon.id, detail.pokemon.name)
            )
        )
    }

    /**
     * Pour récupérer la famille évolutive :
     *  1. /pokemon-species → URL de la chaîne d'évolution
     *  2. /evolution-chain → arbre récursif
     *  3. On l'aplatit en liste ordonnée (racine, puis évolutions DFS)
     *
     * Les sprites sont construits depuis l'id (URL prédictible PokéAPI),
     * pas besoin d'un 4e appel par membre. Les noms sont localisés via
     * [localizer] pour cohérence avec le reste de l'app.
     */
    override suspend fun getEvolutionFamily(pokemonId: Int): Result<List<EvolutionMember>> = safeApiCall {
        val species = api.getPokemonSpecies(pokemonId)
        val chain = api.getEvolutionChain(species.evolutionChain.extractId())
        flattenChain(chain.chain).map { (id, rawName) ->
            EvolutionMember(
                id = id,
                name = localizer.localize(id, rawName),
                spriteUrl = OFFICIAL_ARTWORK_BASE + id + ".png",
            )
        }
    }

    /**
     * Parcours DFS de l'arbre d'évolution. Pour les chaînes simples
     * (Bulbi → Herbi → Florizarre), résultat = [Bulbi, Herbi, Florizarre].
     * Pour Eevee = [Evoli, Aquali, Voltali, Pyroli, Mentali, Noctali, Phyllali, Givrali, Nymphali].
     */
    private fun flattenChain(node: ChainLinkDto): List<Pair<Int, String>> {
        val id = node.species.url.trimEnd('/').substringAfterLast('/').toInt()
        return listOf(id to node.species.name) +
            node.evolvesTo.flatMap { flattenChain(it) }
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

    private companion object {
        const val OFFICIAL_ARTWORK_BASE =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
    }
}
