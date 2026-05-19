package com.example.pokedex.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.local.ShinyEncounterStore
import com.example.pokedex.domain.model.EvolutionMember
import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.usecase.GetEvolutionFamilyUseCase
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
    private val getEvolutionFamily: GetEvolutionFamilyUseCase,
    isFavoriteUseCase: IsFavoriteUseCase,
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

    init {
        loadDetail()
    }

    // Level and selected moves
    private val _levelProgress = MutableStateFlow(levelStore.getProgress(pokemonId))
    val levelProgress: StateFlow<com.example.pokedex.data.local.PokemonLevelProgress> = _levelProgress.asStateFlow()

    private val _selectedMoves = MutableStateFlow<Set<String>>(moveStore.getSelectedMoves(pokemonId))
    val selectedMoves: StateFlow<Set<String>> = _selectedMoves.asStateFlow()

    /** Famille évolutive (Bulbi → Herbi → Florizarre, ou les 8 d'Évoli, etc.). */
    private val _evolutionFamily = MutableStateFlow<List<EvolutionMember>>(emptyList())
    val evolutionFamily: StateFlow<List<EvolutionMember>> = _evolutionFamily.asStateFlow()

    /** `true` si l'utilisateur a déjà débloqué le shiny pour ce Pokémon. */
    private val _isShinyUnlocked = MutableStateFlow(shinyEncounterStore.isShinyUnlocked(pokemonId))
    val isShinyUnlocked: StateFlow<Boolean> = _isShinyUnlocked.asStateFlow()

    /** `true` si l'utilisateur veut afficher la version shiny en ce moment. */
    private val _isShinyDisplayed = MutableStateFlow(shinyEncounterStore.isShinyDisplayed(pokemonId))
    val isShinyDisplayed: StateFlow<Boolean> = _isShinyDisplayed.asStateFlow()

    init {
        loadEvolutionFamily()
    }

    private fun loadEvolutionFamily() {
        viewModelScope.launch {
            getEvolutionFamily(pokemonId).onSuccess { members ->
                _evolutionFamily.value = members
            }
        }
    }

    /**
     * Bascule l'affichage shiny ↔ normal. Pertinent uniquement si le shiny
     * a été débloqué — sinon le toggle est silencieusement ignoré.
     */
    fun onToggleShinyDisplay() {
        if (!_isShinyUnlocked.value) return
        val next = !_isShinyDisplayed.value
        shinyEncounterStore.setShinyDisplayed(pokemonId, next)
        _isShinyDisplayed.value = next
        // Force un re-render du sprite affiché via une recomposition de l'état
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        val basePokemon = current.pokemon
        val newSprite = if (next) basePokemon.shinySpriteUrl ?: basePokemon.spriteUrl
                        else basePokemon.spriteUrl
        _uiState.value = UiState.Success(
            current.copy(
                pokemon = basePokemon.copy(
                    isShiny = next,
                )
            )
        )
        // Note : on garde spriteUrl/shinySpriteUrl tels quels — l'UI choisit
        // lequel afficher en lisant isShinyDisplayed depuis le VM.
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getPokemonDetail(pokemonId).fold(
                onSuccess = { detail ->
                    val encounter = shinyEncounterStore.registerAttempt(pokemonId)
                    val displayShiny = encounter.isShiny &&
                        shinyEncounterStore.isShinyDisplayed(pokemonId)

                    // Sync flow states (la 1re visite déclenche peut-être un unlock)
                    _isShinyUnlocked.value = encounter.isShiny
                    _isShinyDisplayed.value = displayShiny

                    // On garde spriteUrl ET shinySpriteUrl intacts → le UI choisira
                    // lequel afficher selon `isShiny` (toggle sans refetch).
                    _uiState.value = UiState.Success(
                        detail.copy(
                            pokemon = detail.pokemon.copy(
                                isShiny = displayShiny,
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
        // favoriteState (Boolean) est collecté par l'écran → sa valeur est fiable.
        // favoritePokemonState (Pokemon?) ne l'était pas → on l'a retiré du VM.
        val isCurrentlyFavorite = favoriteState.value
        viewModelScope.launch {
            toggleFavorite(pokemon, isCurrentlyFavorite)
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
