package com.example.pokedex.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.local.ShinyEncounterStore
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.usecase.GetPokemonDetailUseCase
import com.example.pokedex.domain.usecase.IsFavoriteUseCase
import com.example.pokedex.domain.usecase.ObserveFavoriteUseCase
import com.example.pokedex.domain.usecase.ToggleFavoriteUseCase
import com.example.pokedex.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPokemonDetail: GetPokemonDetailUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    isFavoriteUseCase: IsFavoriteUseCase,
    observeFavoriteUseCase: ObserveFavoriteUseCase,
    private val shinyEncounterStore: ShinyEncounterStore,
    private val levelStore: com.example.pokedex.data.local.PokemonLevelStore,
    private val moveStore: com.example.pokedex.data.local.PokemonMoveStore
) : ViewModel() {

    // Récupération robuste de l'id depuis la nav, qu'il soit typé Int ou String dans la route.
    private val pokemonId: Int = savedStateHandle.get<Int>("id")
        ?: savedStateHandle.get<String>("id")?.toIntOrNull()
        ?: error("Argument 'id' manquant pour PokemonDetailViewModel")

    private val _uiState = MutableStateFlow<UiState<PokemonDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<PokemonDetail>> = _uiState.asStateFlow()

    val favoriteState: StateFlow<Boolean> = isFavoriteUseCase(pokemonId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    private val favoritePokemonState: StateFlow<Pokemon?> =
        observeFavoriteUseCase(pokemonId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    init {
        loadDetail()
    }

    // Level and selected moves
    private val _levelProgress = MutableStateFlow(levelStore.getProgress(pokemonId))
    val levelProgress: StateFlow<com.example.pokedex.data.local.PokemonLevelProgress> = _levelProgress.asStateFlow()

    private val _selectedMoves = MutableStateFlow<Set<String>>(moveStore.getSelectedMoves(pokemonId))
    val selectedMoves: StateFlow<Set<String>> = _selectedMoves.asStateFlow()

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getPokemonDetail(pokemonId).fold(
                onSuccess = { detail ->
                    val encounter = shinyEncounterStore.registerAttempt(pokemonId)
                    val pokemon = detail.pokemon
                    val shownSprite = if (encounter.isShiny) {
                        pokemon.shinySpriteUrl ?: pokemon.spriteUrl
                    } else {
                        pokemon.spriteUrl
                    }

                    _uiState.value = UiState.Success(
                        detail.copy(
                            pokemon = pokemon.copy(
                                spriteUrl = shownSprite,
                                isShiny = encounter.isShiny,
                                shinyAttempts = if (encounter.isShiny) encounter.attempts else null
                            )
                        )
                    )
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(
                        error.message ?: "Erreur lors du chargement du Pokémon"
                    )
                }
            )
        }
    }

    fun onToggleFavorite() {
        val pokemon = (_uiState.value as? UiState.Success)?.data?.pokemon ?: return
        viewModelScope.launch {
            toggleFavorite(pokemon, favoritePokemonState.value)
        }
    }

    fun setLevel(level: Int) {
        val progress = levelStore.setLevel(pokemonId, level)
        // update flow
        _levelProgress.value = progress
    }

    fun toggleMoveSelection(move: String) {
        val current = _selectedMoves.value.toMutableSet()
        if (current.contains(move)) {
            current.remove(move)
        } else if (current.size < 4) {
            current.add(move)
        }
        _selectedMoves.value = current
        moveStore.setSelectedMoves(pokemonId, current)
    }
}
