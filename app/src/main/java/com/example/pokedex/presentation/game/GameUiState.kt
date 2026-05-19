package com.example.pokedex.presentation.game

import com.example.pokedex.domain.model.GuessResult
import com.example.pokedex.domain.model.HintType
import com.example.pokedex.domain.model.PokemonGameData
import com.example.pokedex.domain.model.PokemonRef

/**
 * État complet de l'écran Pokémondle.
 *
 * Un seul [data class] regroupe tout : c'est le pattern "single source of
 * truth" recommandé avec Compose + StateFlow. L'UI observe ce flux et se
 * re-compose entièrement à chaque mise à jour (Compose ne re-render que
 * les nœuds dont les inputs ont changé, donc c'est efficace).
 *
 * **Champs dérivés** ([canRequestHint], [remainingCooldown]) : calculés
 * à la volée via `get()` pour qu'ils restent toujours cohérents avec les
 * champs sources, sans risque d'oublier de les mettre à jour.
 */
data class GameUiState(
    val isLoading: Boolean = true,
    val allNames: List<PokemonRef> = emptyList(),
    val mystery: PokemonGameData? = null,
    val guesses: List<GuessResult> = emptyList(),
    val currentInput: String = "",
    val suggestions: List<PokemonRef> = emptyList(),
    val isValidating: Boolean = false,
    val isWon: Boolean = false,
    val revealedHints: Set<HintType> = emptySet(),
    val guessesSinceLastHint: Int = 0,
    val error: String? = null,
) {
    /**
     * `true` quand le joueur a fait assez de tentatives depuis le dernier
     * indice ET qu'il reste des indices non révélés ET que la partie est
     * en cours.
     */
    val canRequestHint: Boolean
        get() = mystery != null &&
            !isWon &&
            guessesSinceLastHint >= HintType.COOLDOWN_GUESSES &&
            revealedHints.size < HintType.entries.size

    /** Tentatives manquantes avant que le prochain indice soit disponible. */
    val remainingCooldown: Int
        get() = (HintType.COOLDOWN_GUESSES - guessesSinceLastHint).coerceAtLeast(0)
}
