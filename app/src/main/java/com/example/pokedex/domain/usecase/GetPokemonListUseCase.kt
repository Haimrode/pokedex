package com.example.pokedex.domain.usecase

import com.example.pokedex.domain.model.Generation
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.repository.PokemonRepository
import javax.inject.Inject

/**
 * Use case : récupérer la liste paginée du Pokédex.
 *
 * Un *use case* représente **une seule action métier**. Il sert d'intermédiaire
 * entre le ViewModel et le Repository pour :
 *   - garder le ViewModel léger et testable (injecter des fakes par cas d'usage)
 *   - réutiliser la même action depuis plusieurs ViewModels si besoin
 *   - centraliser une éventuelle logique métier (validation, transformation…)
 *
 * Pour l'instant on délègue simplement au repository — c'est OK.
 * Si plus tard on veut filtrer, trier, cacher, etc., ça se fera ici.
 *
 * **L'opérateur `invoke`** permet d'appeler l'instance comme une fonction :
 * `getPokemonList()` au lieu de `getPokemonList.execute()`. Idiomatique Kotlin.
 */
class GetPokemonListUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(
        generation: Generation = Generation.GEN_1
    ): Result<List<Pokemon>> =
        repository.getPokemonList(generation)
}
