package com.example.pokedex.presentation.garden

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.pokedex.data.local.PokemonLevelProgress
import com.example.pokedex.domain.battle.BattleCombatant
import com.example.pokedex.domain.battle.BattleSide
import com.example.pokedex.domain.battle.TeamBattleState
import com.example.pokedex.domain.battle.BattleFighterState
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.presentation.favorites.asDisplayName

@Composable
fun GardenRoute(
    onPokemonClick: (Int) -> Unit,
    viewModel: GardenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GardenScreen(
        state = uiState,
        onPokemonClick = onPokemonClick,
        onModeChange = viewModel::onModeChange,
        onToggleTeamMember = viewModel::onToggleTeamMember,
        onStartTeamBattle = viewModel::onStartTeamBattle,
        onPlayBattleMove = viewModel::onPlayBattleMove,
        onResetTeamBattle = viewModel::onResetTeamBattle,
        onSelectFirst = viewModel::onSelectFirst,
        onSelectSecond = viewModel::onSelectSecond,
        onToggleMoveSelection = viewModel::onToggleMoveSelection,
        onStartEncounter = viewModel::onStartEncounter,
        onRandomEncounter = viewModel::onRandomEncounter,
        onClearJournal = viewModel::clearJournal
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GardenScreen(
    state: GardenUiState,
    onPokemonClick: (Int) -> Unit,
    onModeChange: (DuelMode) -> Unit,
    onToggleTeamMember: (Int) -> Unit,
    onStartTeamBattle: () -> Unit,
    onPlayBattleMove: (String) -> Unit,
    onResetTeamBattle: () -> Unit,
    onSelectFirst: (Int?) -> Unit,
    onSelectSecond: (Int?) -> Unit,
    onToggleMoveSelection: (Int, String) -> Unit,
    onStartEncounter: () -> Unit,
    onRandomEncounter: () -> Unit,
    onClearJournal: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Eco, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Jardin")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.favorites.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ajoute des favoris pour debloquer le jardin.")
                }
            }

            else -> {
                TeamBattleContent(
                    state = state,
                    contentPadding = innerPadding,
                    onPokemonClick = onPokemonClick,
                    onToggleTeamMember = onToggleTeamMember,
                    onStartTeamBattle = onStartTeamBattle,
                    onPlayBattleMove = onPlayBattleMove,
                    onResetTeamBattle = onResetTeamBattle,
                    onClearJournal = onClearJournal
                )
            }
        }
    }
}

@Composable
private fun TeamBattleContent(
    state: GardenUiState,
    contentPadding: PaddingValues,
    onPokemonClick: (Int) -> Unit,
    onToggleTeamMember: (Int) -> Unit,
    onStartTeamBattle: () -> Unit,
    onPlayBattleMove: (String) -> Unit,
    onResetTeamBattle: () -> Unit,
    onClearJournal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = "Mon équipe (${state.selectedTeamIds.size}/${PokemonTeamLimit()})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        SelectedTeamStrip(
            favorites = state.favorites,
            selectedTeamIds = state.selectedTeamIds,
            progressByPokemonId = state.levelProgressByPokemonId,
            onPokemonClick = onPokemonClick
        )

        Text(
            text = "Choisis jusqu'a 6 Pokemon dans tes favoris",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            modifier = Modifier.height(320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = state.favorites, key = { it.id }) { pokemon ->
                val selected = pokemon.id in state.selectedTeamIds
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            Text(
                                text = pokemon.name.asDisplayName(),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "#${pokemon.id.toString().padStart(3, '0')} • ${state.levelProgressByPokemonId[pokemon.id]?.level ?: 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilterChip(
                            selected = selected,
                            onClick = { onToggleTeamMember(pokemon.id) },
                            label = { Text(if (selected) "Retirer" else "Ajouter") }
                        )
                        AssistChip(
                            onClick = { onPokemonClick(pokemon.id) },
                            label = { Text("Fiche") }
                        )
                    }
                }
            }
        }

        Button(
            onClick = onStartTeamBattle,
            enabled = state.selectedTeamIds.isNotEmpty() && state.battleState == null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lancer le combat d'equipe")
        }

        state.battleState?.let { battle ->
            BattleArena(
                battle = battle,
                onPlayBattleMove = onPlayBattleMove,
                onResetBattle = onResetTeamBattle
            )
        }

        Text(
            text = "Historique",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Victoires session: ${state.victoriesByPokemonId.values.sum()}",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(onClick = onClearJournal) {
                Text("Vider")
            }
        }
        EncounterLog(log = state.battleState?.log ?: state.encounterLog)
    }
}

@Composable
private fun SelectedTeamStrip(
    favorites: List<Pokemon>,
    selectedTeamIds: List<Int>,
    progressByPokemonId: Map<Int, PokemonLevelProgress>,
    onPokemonClick: (Int) -> Unit
) {
    if (selectedTeamIds.isEmpty()) {
        Text(
            text = "Aucun Pokemon dans l'equipe pour le moment.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        selectedTeamIds.forEachIndexed { index, id ->
            val pokemon = favorites.firstOrNull { it.id == id }
            if (pokemon != null) {
                AssistChip(
                    onClick = { onPokemonClick(id) },
                    label = {
                        Text(
                            text = "${index + 1}. ${pokemon.name.asDisplayName()} Lv.${progressByPokemonId[id]?.level ?: 1}"
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BattleArena(
    battle: TeamBattleState,
    onPlayBattleMove: (String) -> Unit,
    onResetBattle: () -> Unit
) {
    Text(
        text = "Combat d'equipe",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    TeamCombatantCard(
        title = "Ton Pokemon actif",
        combatant = battle.activePlayer(),
        active = true
    )
    TeamCombatantCard(
        title = "Pokemon adverse",
        combatant = battle.activeEnemy(),
        active = false
    )

    if (battle.winner == null) {
        Text(
            text = if (battle.awaitingPlayerMove) "Choisis une attaque" else "Attente du tour...",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        BattleMoveButtons(
            moves = battle.playerMoves(),
            enabled = battle.awaitingPlayerMove,
            onPlayBattleMove = onPlayBattleMove
        )
    } else {
        Text(
            text = if (battle.winner == BattleSide.PLAYER) "Victoire !" else "Defaite...",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (battle.winner == BattleSide.PLAYER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Button(onClick = onResetBattle) {
            Text("Nouvelle bataille")
        }
    }
}

@Composable
private fun TeamCombatantCard(
    title: String,
    combatant: BattleCombatant?,
    active: Boolean
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (combatant == null) {
            Text(
                text = "$title: aucun Pokemon disponible",
                modifier = Modifier.padding(12.dp)
            )
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
                    Text(
                        text = "$title - ${combatant.name.asDisplayName()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Niv. ${combatant.level} • PV ${combatant.currentHp}/${combatant.maxHp}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    LinearProgressIndicator(
                        progress = { combatant.currentHp.toFloat() / combatant.maxHp.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = combatant.moves.joinToString(" • ") { it.asDisplayName() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BattleMoveButtons(
    moves: List<String>,
    enabled: Boolean,
    onPlayBattleMove: (String) -> Unit
) {
    val visibleMoves = moves.take(4)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        visibleMoves.chunked(2).forEach { chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chunk.forEach { move ->
                    FilterChip(
                        selected = false,
                        enabled = enabled,
                        onClick = { onPlayBattleMove(move) },
                        label = { Text(move.asDisplayName()) }
                    )
                }
            }
        }
    }
}

private fun PokemonTeamLimit(): Int = 6

@Composable
private fun FavoriteSelectorRow(
    title: String,
    favorites: List<Pokemon>,
    selectedId: Int?,
    progressByPokemonId: Map<Int, PokemonLevelProgress>,
    onSelect: (Int?) -> Unit,
    onPokemonClick: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelLarge)
        LazyColumn(
            modifier = Modifier.height(130.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(items = favorites, key = { it.id }) { pokemon ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(pokemon.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = pokemon.spriteUrl,
                            contentDescription = pokemon.name,
                            modifier = Modifier.size(48.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pokemon.name.asDisplayName(),
                                fontWeight = if (pokemon.id == selectedId) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(text = "#${pokemon.id.toString().padStart(3, '0')}")
                            progressByPokemonId[pokemon.id]?.let { progress ->
                                Text(
                                    text = "Niv. ${progress.level} • ${progress.xpInLevel}/${100} XP",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LinearProgressIndicator(
                                    progress = { progress.xpInLevel / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        AssistChip(
                            onClick = { onPokemonClick(pokemon.id) },
                            label = { Text("Fiche") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BattleCard(
    label: String,
    fighter: BattleFighterState,
    winnerId: Int?
) {
    val progress = fighter.currentHp.toFloat() / fighter.fighter.maxHp.toFloat()
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (winnerId == fighter.fighter.id) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = fighter.fighter.spriteUrl,
                contentDescription = fighter.fighter.name,
                modifier = Modifier.size(72.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$label - ${fighter.fighter.name.asDisplayName()}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (fighter.fighter.isShiny) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "shiny",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("HP: ${fighter.currentHp}/${fighter.fighter.maxHp}")
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MovesPicker(
    title: String,
    allMoves: List<String>,
    selectedMoves: Set<String>,
    onToggle: (String) -> Unit
) {
    if (allMoves.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$title (${selectedMoves.size}/4)",
            style = MaterialTheme.typography.labelLarge
        )
        allMoves.take(12).chunked(3).forEach { chunk ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chunk.forEach { move ->
                        FilterChip(
                            selected = selectedMoves.contains(move),
                            onClick = { onToggle(move) },
                            label = {
                                Column {
                                    Text(move.asDisplayName())
                                    val t = com.example.pokedex.domain.battle.BattleMath.inferMoveType(move)
                                    val c = com.example.pokedex.domain.battle.BattleMath.inferMoveCategory(move)
                                    Text(
                                        text = listOfNotNull(t?.replaceFirstChar { it.uppercase() }, c.replaceFirstChar { it.uppercase() }).joinToString(" • "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                }
            }
        }
        Text(
            text = "Les 4 attaques choisies seront utilisees pour le duel.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EncounterLog(log: List<String>) {
    if (log.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Text("Aucune rencontre pour le moment.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        log.takeLast(15).forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

