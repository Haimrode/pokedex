package com.example.pokedex.data.preload

import com.example.pokedex.data.cache.PokemonCache
import com.example.pokedex.domain.model.Generation
import com.example.pokedex.domain.usecase.GetPokemonListUseCase
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsable de précharger le Pokédex en mémoire au démarrage de l'application.
 * La méthode `preload` lance le fetch en arrière-plan (non bloquant).
 */
@Singleton
class PreloadManager @Inject constructor(
    private val getPokemonList: GetPokemonListUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Lance le préchargement en tâche de fond.
     * Précharge Gen 1 par défaut (151 Pokémons) — l'utilisateur peut
     * changer de génération à la demande sans recharger Gen 1.
     */
    fun preload(generation: Generation = Generation.GEN_1) {
        scope.launch {
            try {
                val result = getPokemonList(generation)
                result.onSuccess { list ->
                    PokemonCache.set(list)
                    Log.i("PreloadManager", "Preloaded ${list.size} pokemons into cache")
                }
                result.onFailure { t ->
                    Log.w("PreloadManager", "Preload failed: ${t.message}")
                }
                // On ignore l'erreur ici : le ViewModel se chargera de la récupération à la demande
            } catch (t: Throwable) {
                // ne pas crash l'app si le preload échoue
                Log.w("PreloadManager", "Preload threw: ${t.message}")
            }
        }
    }

    /** Permet d'effacer le cache si nécessaire. */
    fun clearCache() = PokemonCache.clear()
}

