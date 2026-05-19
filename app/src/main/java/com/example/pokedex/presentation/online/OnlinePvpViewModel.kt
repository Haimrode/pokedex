package com.example.pokedex.presentation.online

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.local.PokemonLevelProgress
import com.example.pokedex.data.local.PokemonLevelStore
import com.example.pokedex.data.online.OnlinePacket
import com.example.pokedex.data.online.OnlinePvpTransport
import com.example.pokedex.data.online.RoomInviteCodec
import com.example.pokedex.domain.battle.BattleCombatant
import com.example.pokedex.domain.battle.BattleSide
import com.example.pokedex.domain.battle.PvpBattleEngine
import com.example.pokedex.domain.battle.PvpBattleState
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.domain.usecase.GetFavoritesUseCase
import com.example.pokedex.domain.usecase.GetPokemonDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.google.gson.Gson

private val gson = Gson()

enum class OnlineRole { HOST, GUEST }

enum class OnlineConnectionStatus { IDLE, HOSTING, WAITING_FOR_GUEST, CONNECTED, BATTLE_READY, BATTLE_RUNNING, FINISHED, ERROR }

data class OnlinePvpUiState(
    val favorites: List<Pokemon> = emptyList(),
    val selectedTeamIds: List<Int> = emptyList(),
    val levelProgressByPokemonId: Map<Int, PokemonLevelProgress> = emptyMap(),
    val role: OnlineRole? = null,
    val connectionStatus: OnlineConnectionStatus = OnlineConnectionStatus.IDLE,
    val roomCode: String = "",
    val joinCodeInput: String = "",
    val hostTeamSubmitted: Boolean = false,
    val guestTeamSubmitted: Boolean = false,
    val hostTeam: List<BattleCombatant> = emptyList(),
    val guestTeam: List<BattleCombatant> = emptyList(),
    val battleState: PvpBattleState? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class OnlinePvpViewModel @Inject constructor(
    private val getFavorites: GetFavoritesUseCase,
    private val getPokemonDetail: GetPokemonDetailUseCase,
    private val levelStore: PokemonLevelStore,
    private val transport: OnlinePvpTransport
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnlinePvpUiState())
    val uiState: StateFlow<OnlinePvpUiState> = _uiState.asStateFlow()

    init {
        observeFavorites()
        observePackets()
    }

    fun onJoinCodeChange(code: String) {
        _uiState.value = _uiState.value.copy(joinCodeInput = code, errorMessage = null)
    }

    fun onToggleTeamMember(pokemonId: Int) {
        val current = _uiState.value.selectedTeamIds
        val updated = if (pokemonId in current) {
            current.filterNot { it == pokemonId }
        } else if (current.size < 6) {
            current + pokemonId
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = "Tu peux choisir au maximum 6 Pokemon.")
            return
        }
        _uiState.value = _uiState.value.copy(selectedTeamIds = updated, errorMessage = null)
    }

    fun onCreateRoom() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(connectionStatus = OnlineConnectionStatus.HOSTING, errorMessage = null)
                val invite = transport.host()
                _uiState.value = _uiState.value.copy(
                    role = OnlineRole.HOST,
                    roomCode = RoomInviteCodec.encode(invite),
                    connectionStatus = OnlineConnectionStatus.WAITING_FOR_GUEST
                )
            } catch (throwable: Throwable) {
                _uiState.value = _uiState.value.copy(
                    connectionStatus = OnlineConnectionStatus.ERROR,
                    errorMessage = throwable.message ?: "Impossible de creer la room"
                )
            }
        }
    }

    fun onJoinRoom() {
        val code = _uiState.value.joinCodeInput.trim()
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Entre un code de room.")
            return
        }
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(connectionStatus = OnlineConnectionStatus.CONNECTED, errorMessage = null)
                transport.join(code)
                _uiState.value = _uiState.value.copy(
                    role = OnlineRole.GUEST,
                    roomCode = code,
                    connectionStatus = OnlineConnectionStatus.CONNECTED
                )
            } catch (throwable: Throwable) {
                _uiState.value = _uiState.value.copy(
                    connectionStatus = OnlineConnectionStatus.ERROR,
                    errorMessage = throwable.message ?: "Impossible de rejoindre la room"
                )
            }
        }
    }

    fun onSubmitTeam() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedTeamIds.isEmpty()) {
                _uiState.value = state.copy(errorMessage = "Choisis au moins un Pokemon pour ton equipe.")
                return@launch
            }
            val combatants = buildCombatants(state.selectedTeamIds)
            val json = gson.toJson(combatants)
            transport.send(OnlinePacket(type = "TEAM", payload = json))
            when (state.role) {
                OnlineRole.HOST -> _uiState.value = _uiState.value.copy(hostTeam = combatants, hostTeamSubmitted = true)
                OnlineRole.GUEST -> _uiState.value = _uiState.value.copy(guestTeam = combatants, guestTeamSubmitted = true)
                null -> _uiState.value = state.copy(errorMessage = "Creer ou rejoins une room avant de valider l'equipe.")
            }
            maybeStartBattle()
        }
    }

    fun onPlayMove(move: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val battle = state.battleState ?: return@launch
            val role = state.role ?: return@launch
            val side = if (role == OnlineRole.HOST) BattleSide.PLAYER else BattleSide.ENEMY
            val next = PvpBattleEngine.submitMove(battle, side, move)
            _uiState.value = state.copy(battleState = next, errorMessage = null)
            transport.send(OnlinePacket(type = "MOVE", payload = move))
            if (role == OnlineRole.HOST) {
                transport.send(OnlinePacket(type = "STATE", payload = gson.toJson(next)))
            }
        }
    }

    fun onResetBattle() {
        _uiState.value = _uiState.value.copy(battleState = null, hostTeamSubmitted = false, guestTeamSubmitted = false, hostTeam = emptyList(), guestTeam = emptyList())
    }

    fun disconnect() {
        viewModelScope.launch {
            transport.close()
            _uiState.value = OnlinePvpUiState(favorites = _uiState.value.favorites, selectedTeamIds = _uiState.value.selectedTeamIds, levelProgressByPokemonId = _uiState.value.levelProgressByPokemonId)
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavorites().collectLatest { favorites ->
                val levelProgressByPokemonId = favorites.associate { pokemon ->
                    pokemon.id to levelStore.getProgress(pokemon.id)
                }
                _uiState.value = _uiState.value.copy(
                    favorites = favorites,
                    levelProgressByPokemonId = levelProgressByPokemonId,
                    selectedTeamIds = _uiState.value.selectedTeamIds.filter { it in favorites.map { pokemon -> pokemon.id } }
                )
            }
        }
    }

    private fun observePackets() {
        viewModelScope.launch {
            transport.incomingPackets.collectLatest { packet ->
                handlePacket(packet)
            }
        }
    }

    private fun handlePacket(packet: OnlinePacket) {
        when (packet.type) {
            "TEAM" -> {
                val combatants = gson.fromJson(packet.payload, Array<BattleCombatant>::class.java).toList()
                val current = _uiState.value
                if (current.role == OnlineRole.HOST) {
                    _uiState.value = current.copy(
                        guestTeam = combatants,
                        guestTeamSubmitted = true,
                        connectionStatus = OnlineConnectionStatus.BATTLE_READY
                    )
                } else {
                    _uiState.value = current.copy(
                        hostTeam = combatants,
                        connectionStatus = OnlineConnectionStatus.BATTLE_READY
                    )
                }
                maybeStartBattle()
            }
            "MOVE" -> {
                val move = packet.payload
                val current = _uiState.value
                val battle = current.battleState ?: return
                if (current.role == OnlineRole.HOST) {
                    val next = PvpBattleEngine.submitMove(battle, BattleSide.ENEMY, move)
                    _uiState.value = current.copy(battleState = next, connectionStatus = if (next.winner == null) OnlineConnectionStatus.BATTLE_RUNNING else OnlineConnectionStatus.FINISHED)
                    viewModelScope.launch {
                        transport.send(OnlinePacket(type = "STATE", payload = gson.toJson(next)))
                    }
                }
            }
            "STATE" -> {
                val state = gson.fromJson(packet.payload, PvpBattleState::class.java)
                _uiState.value = _uiState.value.copy(
                    battleState = state,
                    connectionStatus = if (state.winner == null) OnlineConnectionStatus.BATTLE_RUNNING else OnlineConnectionStatus.FINISHED
                )
            }
            "ERROR" -> {
                _uiState.value = _uiState.value.copy(errorMessage = packet.payload, connectionStatus = OnlineConnectionStatus.ERROR)
            }
        }
    }

    private fun maybeStartBattle() {
        val state = _uiState.value
        if (state.hostTeamSubmitted && state.guestTeamSubmitted && state.role == OnlineRole.HOST && state.battleState == null) {
            val battle = PvpBattleEngine.startBattle(state.hostTeam, state.guestTeam)
            _uiState.value = state.copy(battleState = battle, connectionStatus = OnlineConnectionStatus.BATTLE_RUNNING)
            viewModelScope.launch {
                transport.send(OnlinePacket(type = "STATE", payload = gson.toJson(battle)))
            }
        }
    }

    private suspend fun buildCombatants(teamIds: List<Int>): List<BattleCombatant> {
        return teamIds.map { id ->
            val detail = getPokemonDetail(id).getOrNull() ?: throw CancellationException("Impossible de charger le Pokemon $id")
            buildCombatant(detail)
        }
    }

    private fun buildCombatant(detail: PokemonDetail): BattleCombatant {
        val progress = levelStore.getProgress(detail.pokemon.id)
        val levelBonus = progress.level - 1
        val moves = detail.moves.distinct().take(progress.unlockedMoveCount).ifEmpty { listOf("charge") }
        return BattleCombatant(
            id = detail.pokemon.id,
            name = detail.pokemon.name,
            spriteUrl = detail.pokemon.spriteUrl,
            isShiny = detail.pokemon.spriteUrl.contains("shiny", ignoreCase = true),
            level = progress.level,
            types = detail.pokemon.types,
            maxHp = (detail.stats.hp * 4 + levelBonus * 10).coerceIn(90, 420),
            attack = (detail.stats.attack + levelBonus * 3).coerceIn(10, 300),
            defense = (detail.stats.defense + levelBonus * 3).coerceIn(10, 300),
            speed = (detail.stats.speed + levelBonus * 2).coerceIn(10, 280),
            moves = moves
        )
    }
}

