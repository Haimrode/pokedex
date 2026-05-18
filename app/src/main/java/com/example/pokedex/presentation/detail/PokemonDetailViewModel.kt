package com.example.pokedex.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.usecase.GetPokemonDetailUseCase
import com.example.pokedex.domain.usecase.IsFavoriteUseCase
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
    isFavoriteUseCase: IsFavoriteUseCase
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

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getPokemonDetail(pokemonId).fold(
                onSuccess = { detail ->
                    _uiState.value = UiState.Success(detail)
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
            toggleFavorite(pokemon, favoriteState.value)
        }
    }
}
