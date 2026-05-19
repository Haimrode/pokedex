package com.example.pokedex.data.cache

import com.example.pokedex.domain.model.Pokemon
import java.util.concurrent.atomic.AtomicReference

/**
 * Simple in-memory cache pour la liste des Pokémons.
 * Persistant seulement pendant la vie du process, destiné à accélérer
 * les lancements et éviter des fetchs réseau redondants.
 */
object PokemonCache {
    private val ref: AtomicReference<List<Pokemon>?> = AtomicReference(null)

    fun get(): List<Pokemon>? = ref.get()

    fun set(list: List<Pokemon>?) = ref.set(list)

    fun clear() = ref.set(null)
}

