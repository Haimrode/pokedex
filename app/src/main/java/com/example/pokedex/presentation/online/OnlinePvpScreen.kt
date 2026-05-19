package com.example.pokedex.presentation.online

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.pokedex.domain.battle.BattleSide
import com.example.pokedex.domain.battle.PvpBattleState
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.presentation.favorites.asDisplayName

@Composable
fun OnlinePvpRoute(
    onPokemonClick: (Int) -> Unit,
    viewModel: OnlinePvpViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OnlinePvpScreen(
        state = state,
        onPokemonClick = onPokemonClick,
        onJoinCodeChange = viewModel::onJoinCodeChange,
        onToggleTeamMember = viewModel::onToggleTeamMember,
        onCreateRoom = viewModel::onCreateRoom,
        onJoinRoom = viewModel::onJoinRoom,
        onSubmitTeam = viewModel::onSubmitTeam,
        onPlayMove = viewModel::onPlayMove,
        onResetBattle = viewModel::onResetBattle,
        onDisconnect = viewModel::disconnect
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlinePvpScreen(
    state: OnlinePvpUiState,
    onPokemonClick: (Int) -> Unit,
    onJoinCodeChange: (String) -> Unit,
    onToggleTeamMember: (Int) -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onSubmitTeam: () -> Unit,
    onPlayMove: (String) -> Unit,
    onResetBattle: () -> Unit,
    onDisconnect: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("En ligne")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            state.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            when (state.connectionStatus) {
                OnlineConnectionStatus.IDLE, OnlineConnectionStatus.HOSTING, OnlineConnectionStatus.WAITING_FOR_GUEST, OnlineConnectionStatus.CONNECTED, OnlineConnectionStatus.BATTLE_READY -> {
                    HostJoinControls(
                        state = state,
                        onJoinCodeChange = onJoinCodeChange,
                        onCreateRoom = onCreateRoom,
                        onJoinRoom = onJoinRoom,
                        onDisconnect = onDisconnect
                    )
                }
                OnlineConnectionStatus.BATTLE_RUNNING, OnlineConnectionStatus.FINISHED -> Unit
                OnlineConnectionStatus.ERROR -> HostJoinControls(
                    state = state,
                    onJoinCodeChange = onJoinCodeChange,
                    onCreateRoom = onCreateRoom,
                    onJoinRoom = onJoinRoom,
                    onDisconnect = onDisconnect
                )
            }

            TeamBuilderSection(
                favorites = state.favorites,
                selectedTeamIds = state.selectedTeamIds,
                onToggleTeamMember = onToggleTeamMember,
                onPokemonClick = onPokemonClick
            )

            Button(
                onClick = onSubmitTeam,
                enabled = state.selectedTeamIds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Group, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Valider l'équipe")
            }

            state.battleState?.let { battle ->
                BattleArena(
                    state = battle,
                    localRole = state.role,
                    onPlayMove = onPlayMove,
                    onResetBattle = onResetBattle
                )
            }
        }
    }
}

@Composable
private fun HostJoinControls(
    state: OnlinePvpUiState,
    onJoinCodeChange: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onCreateRoom) {
                Icon(Icons.Default.MeetingRoom, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("Héberger")
            }
            OutlinedButton(onClick = onDisconnect) {
                Text("Déconnecter")
            }
        }
        state.roomCode.takeIf { it.isNotBlank() }?.let { code ->
            CopyableCode(code = code)
        }
        OutlinedTextField(
            value = state.joinCodeInput,
            onValueChange = onJoinCodeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Code de room") },
            placeholder = { Text("Colle le code d'invitation ici") }
        )
        Button(
            onClick = onJoinRoom,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.joinCodeInput.isNotBlank()
        ) {
            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text("Rejoindre")
        }
        Text(
            text = when (state.connectionStatus) {
                OnlineConnectionStatus.HOSTING -> "Création de la room..."
                OnlineConnectionStatus.WAITING_FOR_GUEST -> "En attente du joueur invité"
                OnlineConnectionStatus.CONNECTED -> "Connecté"
                OnlineConnectionStatus.BATTLE_READY -> "Les équipes sont prêtes"
                OnlineConnectionStatus.BATTLE_RUNNING -> "Combat en cours"
                OnlineConnectionStatus.FINISHED -> "Combat terminé"
                OnlineConnectionStatus.ERROR -> "Erreur de connexion"
                else -> "Prêt"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CopyableCode(code: String) {
    val clipboardManager = LocalClipboardManager.current
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Code de room", fontWeight = FontWeight.Bold)
                Text(text = code, style = MaterialTheme.typography.bodySmall)
            }
            AssistChip(
                onClick = { clipboardManager.setText(AnnotatedString(code)) },
                label = { Text("Copier") },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
            )
        }
    }
}

@Composable
private fun TeamBuilderSection(
    favorites: List<Pokemon>,
    selectedTeamIds: List<Int>,
    onToggleTeamMember: (Int) -> Unit,
    onPokemonClick: (Int) -> Unit
) {
    Text(
        text = "Mon équipe (${selectedTeamIds.size}/6)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    LazyColumn(
        modifier = Modifier.height(320.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = favorites, key = { it.id }) { pokemon ->
            val selected = pokemon.id in selectedTeamIds
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleTeamMember(pokemon.id) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = pokemon.spriteUrl,
                        contentDescription = pokemon.name,
                        modifier = Modifier.size(56.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = pokemon.name.asDisplayName(), fontWeight = FontWeight.Bold)
                        Text(text = "#${pokemon.id.toString().padStart(3, '0')}")
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { onToggleTeamMember(pokemon.id) },
                        label = { Text(if (selected) "Choisi" else "Choisir") }
                    )
                    AssistChip(onClick = { onPokemonClick(pokemon.id) }, label = { Text("Fiche") })
                }
            }
        }
    }
}

@Composable
private fun BattleArena(
    state: PvpBattleState,
    localRole: OnlineRole?,
    onPlayMove: (String) -> Unit,
    onResetBattle: () -> Unit
) {
    Text(
        text = "Combat PVP",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    BattleCombatantCard(
        title = if (localRole == OnlineRole.HOST) "Mon Pokémon" else "Adversaire",
        combatant = if (localRole == OnlineRole.HOST) state.activeHost() else state.activeGuest()
    )
    BattleCombatantCard(
        title = if (localRole == OnlineRole.HOST) "Adversaire" else "Mon Pokémon",
        combatant = if (localRole == OnlineRole.HOST) state.activeGuest() else state.activeHost()
    )

    if (state.winner == null) {
        Text(
            text = if ((localRole == OnlineRole.HOST && state.hostAwaitingMove) || (localRole == OnlineRole.GUEST && state.guestAwaitingMove)) {
                "Choisis une attaque"
            } else {
                "En attente de l'autre joueur..."
            },
            fontWeight = FontWeight.Bold
        )
        val moves = if (localRole == OnlineRole.HOST) state.currentMovesForHost() else state.currentMovesForGuest()
        BattleMoveButtons(moves = moves, onPlayMove = onPlayMove)
    } else {
        Text(
            text = if (state.winner == BattleSide.PLAYER) "Victoire du host" else "Victoire de l'invité",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Button(onClick = onResetBattle) {
            Text("Recommencer")
        }
    }

    Text("Journal")
    state.log.takeLast(10).forEach { line ->
        Text(text = line, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun BattleCombatantCard(title: String, combatant: com.example.pokedex.domain.battle.BattleCombatant?) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (combatant == null) {
            Text(text = "$title: indisponible", modifier = Modifier.padding(12.dp))
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = combatant.spriteUrl,
                    contentDescription = combatant.name,
                    modifier = Modifier.size(72.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "$title - ${combatant.name.asDisplayName()}", fontWeight = FontWeight.Bold)
                    Text(text = "Niv. ${combatant.level} • PV ${combatant.currentHp}/${combatant.maxHp}")
                    LinearProgressIndicator(
                        progress = { combatant.currentHp.toFloat() / combatant.maxHp.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(text = combatant.moves.joinToString(" • ") { it.asDisplayName() }, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun BattleMoveButtons(moves: List<String>, onPlayMove: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        moves.take(4).chunked(2).forEach { chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chunk.forEach { move ->
                    FilterChip(selected = false, onClick = { onPlayMove(move) }, label = { Text(move.asDisplayName()) })
                }
            }
        }
    }
}

