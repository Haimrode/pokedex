package com.example.pokedex.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.usecase.GetFavoritesUseCase
import com.example.pokedex.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavorites: GetFavoritesUseCase
) : ViewModel() {

    private val _favoritesState = MutableStateFlow<UiState<List<Pokemon>>>(UiState.Loading)
    val favoritesState: StateFlow<UiState<List<Pokemon>>> = _favoritesState.asStateFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavorites()
                .catch { throwable ->
                    _favoritesState.value = UiState.Error(
                        throwable.message ?: "Impossible de charger les favoris"
                    )
                }
                .collect { favorites ->
                    _favoritesState.value = UiState.Success(favorites)
                }
        }
    }
}
