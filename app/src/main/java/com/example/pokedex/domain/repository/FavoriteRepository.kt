package com.example.pokedex.domain.repository

import com.example.pokedex.domain.model.Pokemon
import kotlinx.coroutines.flow.Flow

/**
 * Contrat d'accès aux favoris **locaux** (Room).
 *
 * Implémenté par Pierre dans `data/repository/FavoriteRepositoryImpl`.
 *
 * Les méthodes de lecture retournent des [Flow] : la liste des favoris
 * et l'état "est favori ou pas" sont **réactifs** — toute modif en BDD
 * pousse automatiquement la nouvelle valeur vers les ViewModels qui
 * collectent ce Flow. C'est ce qui permet à l'onglet Favoris de se
 * mettre à jour tout seul quand on toggle depuis l'écran Détail.
 */
interface FavoriteRepository {

    /** Émet la liste des Pokémons favoris à chaque changement en BDD. */
    fun getFavorites(): Flow<List<Pokemon>>

    /** Émet `true`/`false` selon que le Pokémon [id] est favori. */
    fun isFavorite(id: Int): Flow<Boolean>

    /** Ajoute [pokemon] aux favoris. Idempotent : déjà favori = no-op. */
    suspend fun addFavorite(pokemon: Pokemon)

    /** Retire le favori d'id [id]. Si pas favori = no-op. */
    suspend fun removeFavorite(id: Int)
}
