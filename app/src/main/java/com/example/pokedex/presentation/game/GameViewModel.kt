package com.example.pokedex.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.domain.model.HintType
import com.example.pokedex.domain.model.PokemonRef
import com.example.pokedex.domain.usecase.GetAllPokemonNamesUseCase
import com.example.pokedex.domain.usecase.StartNewGameUseCase
import com.example.pokedex.domain.usecase.ValidateGuessUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel du Pokémondle.
 *
 * Responsabilités :
 *  - Charger les 1025 noms (autocomplétion + random) au démarrage
 *  - Tirer une cible et la garder cachée
 *  - Valider chaque tentative et l'ajouter à la grille
 *  - Gérer l'autocomplétion à la saisie
 *  - Gérer le cooldown des indices (5 tentatives entre chaque)
 *  - Détecter la victoire et permettre de relancer une partie
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val getAllPokemonNames: GetAllPokemonNamesUseCase,
    private val startNewGame: StartNewGameUseCase,
    private val validateGuess: ValidateGuessUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        bootstrap()
    }

    /**
     * Au démarrage : on charge les noms (1 requête légère) ET on tire
     * une première cible (3 requêtes combinées). Si l'un échoue on
     * affiche l'erreur, sinon on passe directement en jeu.
     */
    private fun bootstrap() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val namesResult = getAllPokemonNames()
            namesResult.fold(
                onSuccess = { names ->
                    startNewGame().fold(
                        onSuccess = { mystery ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    allNames = names,
                                    mystery = mystery,
                                )
                            }
                        },
                        onFailure = ::onLoadFailure,
                    )
                },
                onFailure = ::onLoadFailure,
            )
        }
    }

    fun onInputChange(input: String) {
        _uiState.update { state ->
            val filtered = if (input.isBlank()) emptyList()
            else state.allNames
                .filter { it.name.contains(input.trim().lowercase()) }
                .take(MAX_SUGGESTIONS)
            state.copy(currentInput = input, suggestions = filtered)
        }
    }

    fun onGuessSelected(ref: PokemonRef) {
        val state = _uiState.value
        if (state.mystery == null || state.isValidating || state.isWon || state.isGivenUp) return
        if (state.guesses.any { it.guess.id == ref.id }) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isValidating = true,
                    currentInput = "",
                    suggestions = emptyList(),
                )
            }
            validateGuess(ref.id, state.mystery).fold(
                onSuccess = { result ->
                    _uiState.update { current ->
                        current.copy(
                            isValidating = false,
                            guesses = current.guesses + result,
                            guessesSinceLastHint = current.guessesSinceLastHint + 1,
                            isWon = result.isExactMatch,
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isValidating = false,
                            error = err.message ?: "Erreur lors de la validation",
                        )
                    }
                },
            )
        }
    }

    fun onRequestHint() {
        _uiState.update { state ->
            if (!state.canRequestHint) return@update state
            val nextHint = HintType.entries.first { it !in state.revealedHints }
            state.copy(
                revealedHints = state.revealedHints + nextHint,
                guessesSinceLastHint = 0,
            )
        }
    }

    /**
     * Le joueur abandonne. On passe en état `isGivenUp` : l'UI affiche le
     * Pokémon mystère et propose de rejouer. Le score n'est pas archivé
     * (pas de "défaite" enregistrée — c'est juste un skip).
     */
    fun onGiveUp() {
        _uiState.update { state ->
            if (state.mystery == null || state.isWon) return@update state
            state.copy(isGivenUp = true)
        }
    }

    fun onRestart() {
        viewModelScope.launch {
            val cachedNames = _uiState.value.allNames
            _uiState.update {
                GameUiState(isLoading = true, allNames = cachedNames)
            }
            startNewGame().fold(
                onSuccess = { mystery ->
                    _uiState.update {
                        it.copy(isLoading = false, mystery = mystery)
                    }
                },
                onFailure = ::onLoadFailure,
            )
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun onLoadFailure(error: Throwable) {
        _uiState.update {
            it.copy(
                isLoading = false,
                error = error.message ?: "Erreur de chargement",
            )
        }
    }

    private companion object {
        const val MAX_SUGGESTIONS = 8
    }
}
