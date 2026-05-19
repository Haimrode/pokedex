package com.example.pokedex.presentation.detail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.pokedex.domain.model.PokemonDetail
import com.example.pokedex.presentation.common.UiState
import com.example.pokedex.presentation.common.typeColor
import com.example.pokedex.presentation.common.typeNameFr
import com.example.pokedex.presentation.detail.components.StatBar

@Composable
fun PokemonDetailRoute(
    onBack: () -> Unit,
    viewModel: PokemonDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.favoriteState.collectAsStateWithLifecycle()
    val levelProgress by viewModel.levelProgress.collectAsStateWithLifecycle()
    val selectedMoves by viewModel.selectedMoves.collectAsStateWithLifecycle()

    PokemonDetailScreen(
        uiState = uiState,
        isFavorite = isFavorite,
        levelProgress = levelProgress,
        selectedMoves = selectedMoves,
        onSetLevel = viewModel::setLevel,
        onToggleMove = viewModel::toggleMoveSelection,
        onBack = onBack,
        onToggleFavorite = viewModel::onToggleFavorite,
        onRetry = viewModel::loadDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    uiState: UiState<PokemonDetail>,
    isFavorite: Boolean,
    levelProgress: com.example.pokedex.data.local.PokemonLevelProgress,
    selectedMoves: Set<String>,
    onSetLevel: (Int) -> Unit,
    onToggleMove: (String) -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    val title = when (uiState) {
                        is UiState.Success -> uiState.data.pokemon.name
                            .replaceFirstChar { it.uppercase() }
                        else -> "Pokémon"
                    }
                    Text(text = title, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (uiState is UiState.Success) {
                FloatingActionButton(
                    onClick = onToggleFavorite,
                    containerColor = if (isFavorite) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite
                            else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Retirer des favoris"
                            else "Ajouter aux favoris",
                        tint = if (isFavorite) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) { innerPadding ->
        when (uiState) {
            UiState.Loading -> LoadingContent(
                modifier = Modifier.padding(innerPadding)
            )
            is UiState.Error -> ErrorContent(
                message = uiState.message,
                onRetry = onRetry,
                modifier = Modifier.padding(innerPadding)
            )
            is UiState.Success -> DetailContent(
                detail = uiState.data,
                contentPadding = innerPadding,
                levelProgress = levelProgress,
                selectedMoves = selectedMoves,
                onSetLevel = onSetLevel,
                onToggleMove = onToggleMove
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Erreur de chargement",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Réessayer")
        }
    }
}

@Composable
private fun DetailContent(
    detail: PokemonDetail,
    contentPadding: PaddingValues,
    levelProgress: com.example.pokedex.data.local.PokemonLevelProgress,
    selectedMoves: Set<String>,
    onSetLevel: (Int) -> Unit,
    onToggleMove: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sprite + numéro
        Text(
            text = "#${detail.pokemon.id.toString().padStart(3, '0')}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AsyncImage(
            model = detail.pokemon.spriteUrl,
            contentDescription = detail.pokemon.name,
            modifier = Modifier.size(220.dp),
            contentScale = ContentScale.Fit
        )

        if (detail.pokemon.isShiny) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Shiny obtenu en ${detail.pokemon.shinyAttempts ?: 1} tentative(s)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
        }

        // Types (chips) — couleurs iconiques + libellés FR (Feu, Eau, Plante…).
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            detail.pokemon.types.forEach { type ->
                val color = typeColor(type)
                val labelOnColor = if (color.luminance() > 0.5f) Color.Black else Color.White
                AssistChip(
                    onClick = {},
                    label = { Text(typeNameFr(type)) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = color,
                        labelColor = labelOnColor,
                    ),
                    border = null,
                )
            }
        }

        // Taille + poids
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoStat(label = "Taille", value = "%.1f m".format(detail.height))
            InfoStat(label = "Poids", value = "%.1f kg".format(detail.weight))
        }

        // Section stats
        Text(
            text = "Statistiques de base",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBar(label = "PV", value = detail.stats.hp)
            StatBar(label = "Attaque", value = detail.stats.attack)
            StatBar(label = "Défense", value = detail.stats.defense)
            StatBar(label = "Att. Spé.", value = detail.stats.specialAttack)
            StatBar(label = "Déf. Spé.", value = detail.stats.specialDefense)
            StatBar(label = "Vitesse", value = detail.stats.speed)
        }

        // Level selector
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Text(
            text = "Niveau: ${levelProgress.level}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onSetLevel((levelProgress.level - 1).coerceAtLeast(1)) }) {
                Text("-")
            }
            Button(onClick = { onSetLevel(levelProgress.level + 1) }) {
                Text("+")
            }
            Text(
                text = "(${levelProgress.unlockedMoveCount} attaques debloquées)",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        // Moves selection
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Text(
            text = "Attaques",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val moves = detail.moves.distinct()
            Text(
                text = "Selection: ${selectedMoves.size}/${levelProgress.unlockedMoveCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            moves.chunked(3).forEach { chunk ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chunk.forEach { move ->
                        val enabled = selectedMoves.contains(move) || selectedMoves.size < levelProgress.unlockedMoveCount
                        androidx.compose.material3.FilterChip(
                            selected = selectedMoves.contains(move),
                            onClick = { onToggleMove(move) },
                            enabled = enabled,
                            label = { Text(move.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
        }

        // Espace pour ne pas que le FAB cache la dernière stat
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun InfoStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
