package com.example.pokedex.presentation.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.local.PokemonTeamStore
import com.example.pokedex.data.local.PokemonLevelProgress
import com.example.pokedex.data.local.PokemonLevelStore
import com.example.pokedex.domain.battle.BattleEngine
import com.example.pokedex.domain.battle.BattleCombatant
import com.example.pokedex.domain.battle.BattleFighter
import com.example.pokedex.domain.battle.BattleFighterState
import com.example.pokedex.domain.battle.BattleTurn
import com.example.pokedex.domain.battle.BattleSide
import com.example.pokedex.domain.battle.TeamBattleEngine
import com.example.pokedex.domain.battle.TeamBattleState
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.usecase.GetFavoritesUseCase
import com.example.pokedex.domain.usecase.GetPokemonListUseCase
import com.example.pokedex.domain.usecase.GetPokemonDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class GardenViewModel @Inject constructor(
    private val getFavorites: GetFavoritesUseCase,
    private val getPokemonList: GetPokemonListUseCase,
    private val getPokemonDetail: GetPokemonDetailUseCase,
    private val levelStore: PokemonLevelStore,
    private val teamStore: PokemonTeamStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(GardenUiState(isLoading = true))
    val uiState: StateFlow<GardenUiState> = _uiState.asStateFlow()

    init {
        observeFavorites()
    }

    fun onSelectFirst(id: Int?) {
        _uiState.value = _uiState.value.copy(selectedFirstId = id, errorMessage = null)
        if (id != null) loadMovePool(id)
    }

    fun onSelectSecond(id: Int?) {
        _uiState.value = _uiState.value.copy(selectedSecondId = id, errorMessage = null)
        if (id != null) loadMovePool(id)
    }

    fun onToggleMoveSelection(pokemonId: Int, move: String) {
        val state = _uiState.value
        val current = state.selectedMovesByPokemonId[pokemonId]?.toMutableSet() ?: mutableSetOf()
        if (current.contains(move)) {
            current.remove(move)
        } else if (current.size < 4) {
            current.add(move)
        } else {
            _uiState.value = state.copy(errorMessage = "Maximum 4 attaques par Pokemon.")
            return
        }
        _uiState.value = state.copy(
            selectedMovesByPokemonId = state.selectedMovesByPokemonId + (pokemonId to current),
            errorMessage = null
        )
    }

    fun onModeChange(mode: DuelMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode, errorMessage = null)
    }

    fun onToggleTeamMember(pokemonId: Int) {
        val current = _uiState.value.selectedTeamIds
        val updated = if (pokemonId in current) {
            current.filterNot { it == pokemonId }
        } else if (current.size < PokemonTeamStore.MAX_TEAM_SIZE) {
            current + pokemonId
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = "Tu peux choisir au maximum 6 Pokemon pour l'equipe.")
            return
        }

        teamStore.setTeamIds(updated)
        _uiState.value = _uiState.value.copy(selectedTeamIds = updated, errorMessage = null)
    }

    fun onStartTeamBattle() {
        viewModelScope.launch {
            startTeamBattle()
        }
    }

    fun onPlayBattleMove(moveName: String) {
        val battle = _uiState.value.battleState ?: return
        if (!battle.awaitingPlayerMove || battle.winner != null) return

        val nextBattle = TeamBattleEngine.performPlayerTurn(battle, moveName)
        _uiState.value = _uiState.value.copy(battleState = nextBattle, errorMessage = null)

        if (nextBattle.winner != null) {
            awardTeamBattleXp(nextBattle.winner)
        }
    }

    fun onResetTeamBattle() {
        _uiState.value = _uiState.value.copy(battleState = null, encounterLog = emptyList(), errorMessage = null)
    }

    fun onStartEncounter() {
        startEncounter(randomSelection = false)
    }

    fun onRandomEncounter() {
        startEncounter(randomSelection = true)
    }

    fun clearJournal() {
        _uiState.value = _uiState.value.copy(encounterLog = emptyList(), errorMessage = null)
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavorites().collect { favorites ->
                val current = _uiState.value
                val firstStillValid = current.selectedFirstId?.let { selected ->
                    favorites.any { it.id == selected }
                } ?: false
                val secondStillValid = current.selectedSecondId?.let { selected ->
                    favorites.any { it.id == selected }
                } ?: false

                val validIds = favorites.map { it.id }.toSet()
                val cleanedAvailableMoves = current.availableMovesByPokemonId
                    .filterKeys { id -> id in validIds }
                val cleanedSelectedMoves = current.selectedMovesByPokemonId
                    .filterKeys { id -> id in validIds }

                val levelProgressByPokemonId = favorites.associate { pokemon ->
                    pokemon.id to levelStore.getProgress(pokemon.id)
                }
                val selectedTeamIds = teamStore.getTeamIds().filter { id -> id in validIds }

                val newFirstId = if (firstStillValid) current.selectedFirstId else favorites.firstOrNull()?.id
                val newSecondId = if (secondStillValid) current.selectedSecondId
                else favorites.drop(1).firstOrNull()?.id

                _uiState.value = current.copy(
                    isLoading = false,
                    favorites = favorites,
                    selectedFirstId = newFirstId,
                    selectedSecondId = newSecondId,
                    selectedTeamIds = selectedTeamIds,
                    levelProgressByPokemonId = levelProgressByPokemonId,
                    availableMovesByPokemonId = cleanedAvailableMoves,
                    selectedMovesByPokemonId = cleanedSelectedMoves
                )

                newFirstId?.let { loadMovePool(it, forceRefresh = true) }
                newSecondId?.let { loadMovePool(it, forceRefresh = true) }
            }
        }
    }

    private suspend fun startTeamBattle() = coroutineScope {
        val state = _uiState.value
        val selectedTeam = state.favorites.filter { it.id in state.selectedTeamIds }
        if (selectedTeam.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Choisis au moins un Pokemon pour commencer le combat.")
            return@coroutineScope
        }

        val enemyPool = getPokemonList(limit = 150).getOrElse { error ->
            _uiState.value = state.copy(errorMessage = error.message ?: "Impossible de charger les adversaires.")
            return@coroutineScope
        }

        val enemyCandidates = enemyPool.filterNot { it.id in state.selectedTeamIds }
        if (enemyCandidates.size < selectedTeam.size) {
            _uiState.value = state.copy(errorMessage = "Pas assez de Pokemon pour creer l'equipe adverse.")
            return@coroutineScope
        }

        val enemyTeamPokemons = enemyCandidates.shuffled(Random.Default).take(selectedTeam.size)
        val playerCombatantsDeferred = async { buildCombatants(selectedTeam) }
        val enemyCombatantsDeferred = async { buildCombatants(enemyTeamPokemons) }

        val playerCombatants = playerCombatantsDeferred.await()
        val enemyCombatants = enemyCombatantsDeferred.await()

        val battle = TeamBattleEngine.startBattle(playerCombatants, enemyCombatants)
        _uiState.value = state.copy(battleState = battle, errorMessage = null)
    }

    private suspend fun buildCombatants(pokemons: List<Pokemon>): List<BattleCombatant> = coroutineScope {
        pokemons.map { pokemon ->
            async {
                val detail = getPokemonDetail(pokemon.id).getOrThrow()
                val progress = levelStore.getProgress(pokemon.id)
                buildCombatant(pokemon, detail, progress)
            }
        }.map { it.await() }
    }

    private fun buildCombatant(
        pokemon: Pokemon,
        detail: PokemonDetail,
        progress: PokemonLevelProgress
    ): BattleCombatant {
        val levelBonus = progress.level - 1
        return BattleCombatant(
            id = pokemon.id,
            name = pokemon.name,
            spriteUrl = pokemon.spriteUrl,
            isShiny = pokemon.spriteUrl.contains("shiny", ignoreCase = true),
            level = progress.level,
            types = detail.pokemon.types,
            maxHp = (detail.stats.hp * 4 + (levelBonus * 10)).coerceIn(90, 420),
            attack = (detail.stats.attack + (levelBonus * 3)).coerceIn(10, 300),
            defense = (detail.stats.defense + (levelBonus * 3)).coerceIn(10, 300),
            speed = (detail.stats.speed + (levelBonus * 2)).coerceIn(10, 280),
            moves = detail.moves.distinct().take(progress.unlockedMoveCount).ifEmpty { listOf("charge") }
        )
    }

    private fun awardTeamBattleXp(winner: BattleSide) {
        val state = _uiState.value
        val playerReward = if (winner == BattleSide.PLAYER) 60 else 30

        val updatedProgress = state.selectedTeamIds.associateWith { id ->
            levelStore.gainExperience(id, playerReward)
        }

        val currentBattle = state.battleState
        val updatedState = state.copy(
            levelProgressByPokemonId = state.levelProgressByPokemonId + updatedProgress,
            battleState = currentBattle?.copy(log = currentBattle.log + "XP distribuee a l'equipe du joueur.")
        )
        _uiState.value = updatedState
    }

    private fun startEncounter(randomSelection: Boolean) {
        viewModelScope.launch {
            val state = _uiState.value
            val favorites = state.favorites
            if (state.selectedMode == DuelMode.LOCAL && favorites.size < 2) {
                _uiState.value = state.copy(
                    errorMessage = "Il faut au moins 2 favoris pour lancer une rencontre."
                )
                return@launch
            }

            val firstFavorite: Pokemon
            val secondFavorite: Pokemon

            if (state.selectedMode == DuelMode.LOCAL) {
                val selection = resolveSelection(state, randomSelection)
                if (selection == null) {
                    _uiState.value = state.copy(
                        errorMessage = "Choisis deux Pokemons differents."
                    )
                    return@launch
                }
                firstFavorite = selection.first
                secondFavorite = selection.second
            } else {
                val firstId = state.selectedFirstId
                val local = firstId?.let { id -> favorites.firstOrNull { it.id == id } }
                if (local == null) {
                    _uiState.value = state.copy(errorMessage = "Choisis ton Pokemon pour le mode en ligne.")
                    return@launch
                }
                firstFavorite = local
                secondFavorite = Pokemon(
                    id = Random.nextInt(from = 1, until = 152),
                    name = "adversaire",
                    types = emptyList(),
                    spriteUrl = ""
                )
            }

            _uiState.value = state.copy(
                isBattleRunning = true,
                errorMessage = null,
                selectedFirstId = firstFavorite.id,
                selectedSecondId = secondFavorite.id,
                currentBattle = null,
                encounterLog = state.encounterLog +
                    if (state.selectedMode == DuelMode.ONLINE) {
                        "-- Match en ligne: ${firstFavorite.name} vs joueur distant --"
                    } else {
                        "-- Nouvelle rencontre: ${firstFavorite.name} vs ${secondFavorite.name} --"
                    }
            )

            val firstDetailDeferred = async { getPokemonDetail(firstFavorite.id) }
            val secondDetailDeferred = async { getPokemonDetail(secondFavorite.id) }
            val firstDetailResult = firstDetailDeferred.await()
            val secondDetailResult = secondDetailDeferred.await()

            if (firstDetailResult.isFailure || secondDetailResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isBattleRunning = false,
                    errorMessage = "Impossible de charger les details pour le combat."
                )
                return@launch
            }

            val firstDetail = firstDetailResult.getOrThrow()
            val secondDetail = secondDetailResult.getOrThrow()
            val secondResolved = if (state.selectedMode == DuelMode.ONLINE) {
                secondDetail.pokemon.copy(spriteUrl = secondDetail.pokemon.spriteUrl)
            } else {
                secondFavorite
            }

            val firstFighter = toFighter(firstFavorite, firstDetail)
            val secondFighter = toFighter(secondResolved, secondDetail)

            _uiState.value = _uiState.value.copy(
                currentBattle = GardenBattleState(
                    first = BattleFighterState(firstFighter, firstFighter.maxHp),
                    second = BattleFighterState(secondFighter, secondFighter.maxHp),
                    winnerId = null
                )
            )

            val result = BattleEngine.runDuel(firstFighter, secondFighter)
            playBattle(result.turns)
            val winnerId = result.winnerId
            val updatedVictories = _uiState.value.victoriesByPokemonId.toMutableMap()
            updatedVictories[winnerId] = (updatedVictories[winnerId] ?: 0) + 1

            awardExperience(
                state = state,
                firstId = firstFavorite.id,
                secondId = if (state.selectedMode == DuelMode.LOCAL) secondFavorite.id else null,
                winnerId = winnerId
            )

            _uiState.value = _uiState.value.copy(
                isBattleRunning = false,
                currentBattle = _uiState.value.currentBattle?.copy(winnerId = winnerId),
                victoriesByPokemonId = updatedVictories,
                encounterLog = _uiState.value.encounterLog + "Vainqueur: ${nameFor(winnerId)}"
            )
        }
    }

    private suspend fun playBattle(turns: List<BattleTurn>) {
        turns.forEach { turn ->
            delay(500)
            val state = _uiState.value
            val battle = state.currentBattle ?: return@forEach
            val updatedBattle = if (turn.defenderId == battle.first.fighter.id) {
                battle.copy(first = battle.first.copy(currentHp = turn.defenderRemainingHp))
            } else {
                battle.copy(second = battle.second.copy(currentHp = turn.defenderRemainingHp))
            }

            _uiState.value = state.copy(
                currentBattle = updatedBattle,
                encounterLog = state.encounterLog +
                    "Tour ${turn.turnNumber}: ${nameFor(turn.attackerId)} utilise ${turn.moveName} " +
                    "(${turn.effectivenessLabel}) et inflige ${turn.damage} degats"
            )
        }
    }

    private fun nameFor(id: Int): String {
        val battle = _uiState.value.currentBattle
        val fromBattle = when (id) {
            battle?.first?.fighter?.id -> battle.first.fighter.name
            battle?.second?.fighter?.id -> battle.second.fighter.name
            else -> null
        }
        return fromBattle ?: _uiState.value.favorites.firstOrNull { it.id == id }?.name ?: "Pokemon #$id"
    }

    private fun resolveSelection(
        state: GardenUiState,
        randomSelection: Boolean
    ): Pair<Pokemon, Pokemon>? {
        val favorites = state.favorites
        if (randomSelection) {
            val picks = favorites.shuffled(Random.Default).take(2)
            return if (picks.size == 2) picks[0] to picks[1] else null
        }

        val firstId = state.selectedFirstId ?: return null
        val secondId = state.selectedSecondId ?: return null
        if (firstId == secondId) return null

        val first = favorites.firstOrNull { it.id == firstId } ?: return null
        val second = favorites.firstOrNull { it.id == secondId } ?: return null
        return first to second
    }

    private fun toFighter(favorite: Pokemon, detail: PokemonDetail): BattleFighter = BattleFighter(
        id = favorite.id,
        name = favorite.name,
        spriteUrl = favorite.spriteUrl,
        isShiny = favorite.spriteUrl.contains("shiny", ignoreCase = true),
        types = detail.pokemon.types,
        maxHp = computeScaledStat(detail.stats.hp, favorite.id, 4, 90, 340),
        attack = computeScaledStat(detail.stats.attack, favorite.id, 2, 10, 260),
        defense = computeScaledStat(detail.stats.defense, favorite.id, 2, 10, 260),
        speed = computeScaledStat(detail.stats.speed, favorite.id, 1, 10, 240),
        moves = resolveMoves(favorite.id, detail.moves)
    )

    private fun resolveMoves(pokemonId: Int, fallbackMoves: List<String>): List<String> {
        val progress = _uiState.value.levelProgressByPokemonId[pokemonId]
            ?: levelStore.getProgress(pokemonId)
        val unlockedMoves = fallbackMoves.distinct().take(progress.unlockedMoveCount)
        val selected = _uiState.value.selectedMovesByPokemonId[pokemonId]
            .orEmpty()
            .filter { it in unlockedMoves }
        if (selected.isNotEmpty()) return selected
        return unlockedMoves.ifEmpty { fallbackMoves.take(1) }.ifEmpty { listOf("charge") }
    }

    private fun loadMovePool(pokemonId: Int, forceRefresh: Boolean = false) {
        val state = _uiState.value
        if (!forceRefresh && state.availableMovesByPokemonId.containsKey(pokemonId)) return

        viewModelScope.launch {
            getPokemonDetail(pokemonId).onSuccess { detail ->
                val progress = levelStore.getProgress(pokemonId)
                val moves = detail.moves.distinct().take(progress.unlockedMoveCount).ifEmpty { listOf("charge") }
                val currentSelected = _uiState.value.selectedMovesByPokemonId[pokemonId].orEmpty()
                val selected = currentSelected.intersect(moves.toSet())
                val latest = _uiState.value
                _uiState.value = latest.copy(
                    availableMovesByPokemonId = latest.availableMovesByPokemonId + (pokemonId to moves),
                    selectedMovesByPokemonId = latest.selectedMovesByPokemonId + (pokemonId to (if (selected.isEmpty()) moves.take(4).toSet() else selected))
                )
            }
        }
    }

    private fun awardExperience(
        state: GardenUiState,
        firstId: Int,
        secondId: Int?,
        winnerId: Int
    ) {
        val isLocalDuel = state.selectedMode == DuelMode.LOCAL
        val firstReward = if (winnerId == firstId) 70 else 35
        val updatedFirst = levelStore.gainExperience(firstId, firstReward)
        val updatedSecond = if (isLocalDuel && secondId != null) {
            val secondReward = if (winnerId == secondId) 70 else 35
            secondId to levelStore.gainExperience(secondId, secondReward)
        } else {
            null
        }

        val latest = _uiState.value
        _uiState.value = latest.copy(
            levelProgressByPokemonId = buildMap {
                putAll(latest.levelProgressByPokemonId)
                put(firstId, updatedFirst)
                updatedSecond?.let { (id, progress) -> put(id, progress) }
            }
        )

        loadMovePool(firstId, forceRefresh = true)
        secondId?.let { if (isLocalDuel) loadMovePool(it, forceRefresh = true) }
    }

    private fun computeScaledStat(
        baseStat: Int,
        pokemonId: Int,
        bonusPerLevel: Int,
        minValue: Int,
        maxValue: Int
    ): Int {
        val level = _uiState.value.levelProgressByPokemonId[pokemonId]?.level
            ?: levelStore.getProgress(pokemonId).level
        return (baseStat + ((level - 1) * bonusPerLevel)).coerceIn(minValue, maxValue)
    }
}

enum class DuelMode {
    LOCAL,
    ONLINE
}

data class GardenUiState(
    val isLoading: Boolean = false,
    val favorites: List<Pokemon> = emptyList(),
    val selectedTeamIds: List<Int> = emptyList(),
    val selectedMode: DuelMode = DuelMode.LOCAL,
    val selectedFirstId: Int? = null,
    val selectedSecondId: Int? = null,
    val levelProgressByPokemonId: Map<Int, PokemonLevelProgress> = emptyMap(),
    val availableMovesByPokemonId: Map<Int, List<String>> = emptyMap(),
    val selectedMovesByPokemonId: Map<Int, Set<String>> = emptyMap(),
    val battleState: TeamBattleState? = null,
    val currentBattle: GardenBattleState? = null,
    val isBattleRunning: Boolean = false,
    val encounterLog: List<String> = emptyList(),
    val victoriesByPokemonId: Map<Int, Int> = emptyMap(),
    val errorMessage: String? = null
)

data class GardenBattleState(
    val first: BattleFighterState,
    val second: BattleFighterState,
    val winnerId: Int? = null
)

