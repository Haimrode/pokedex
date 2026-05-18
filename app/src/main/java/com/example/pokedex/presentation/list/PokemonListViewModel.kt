package com.example.pokedex.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.usecase.GetPokemonListUseCase
import com.example.pokedex.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val getPokemonList: GetPokemonListUseCase
) : ViewModel() {

    private val _allPokemons = MutableStateFlow<List<Pokemon>>(emptyList())
    private val _loadStatus = MutableStateFlow<UiState<Unit>>(UiState.Loading)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    val uiState: StateFlow<UiState<List<Pokemon>>> = combine(
        _loadStatus,
        _allPokemons,
        _searchQuery,
        _selectedType
    ) { loadStatus, all, query, type ->
        when (loadStatus) {
            UiState.Loading -> UiState.Loading
            is UiState.Error -> loadStatus
            is UiState.Success -> UiState.Success(applyFilters(all, query, type))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )

    val availableTypes: StateFlow<List<String>> = _allPokemons
        .map { list -> list.flatMap { it.types }.distinct().sorted() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        loadPokemons()
    }

    fun loadPokemons() {
        viewModelScope.launch {
            _loadStatus.value = UiState.Loading
            getPokemonList().fold(
                onSuccess = { list ->
                    _allPokemons.value = list
                    _loadStatus.value = UiState.Success(Unit)
                },
                onFailure = { error ->
                    _loadStatus.value = UiState.Error(
                        error.message ?: "Erreur lors du chargement du Pokédex"
                    )
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onTypeFilterChange(type: String?) {
        _selectedType.value = type
    }

    private fun applyFilters(
        all: List<Pokemon>,
        query: String,
        type: String?
    ): List<Pokemon> = all.filter { pokemon ->
        val matchesQuery = query.isBlank() ||
            pokemon.name.contains(query, ignoreCase = true)
        val matchesType = type == null || pokemon.types.contains(type)
        matchesQuery && matchesType
    }
}
