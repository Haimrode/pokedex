package com.example.pokedex.data.repository

import com.example.pokedex.data.local.FavoritePokemon
import com.example.pokedex.data.local.FavoritePokemonDao
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.repository.FavoriteRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoritePokemonDao: FavoritePokemonDao
) : FavoriteRepository {

    override fun getFavorites(): Flow<List<Pokemon>> =
        favoritePokemonDao.getAll().map { favorites ->
            favorites.map { it.toDomain() }
        }

    override fun isFavorite(id: Int): Flow<Boolean> =
        favoritePokemonDao.isFavorite(id).map { count -> count > 0 }

    override fun observeFavorite(id: Int): Flow<Pokemon?> =
        favoritePokemonDao.getById(id).map { favorite ->
            favorite?.toDomain()
        }

    override suspend fun addFavorite(pokemon: Pokemon) {
        favoritePokemonDao.insert(pokemon.toEntity())
    }

    override suspend fun removeFavorite(id: Int) {
        favoritePokemonDao.deleteById(id)
    }

    private fun FavoritePokemon.toDomain(): Pokemon = Pokemon(
        id = id,
        name = name,
        types = types,
        spriteUrl = spriteUrl
    )

    private fun Pokemon.toEntity(): FavoritePokemon = FavoritePokemon(
        id = id,
        name = name,
        types = types,
        spriteUrl = spriteUrl
    )
}

