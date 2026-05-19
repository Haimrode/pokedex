package com.example.pokedex.presentation.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.pokedex.domain.model.PokemonGameData
import com.example.pokedex.presentation.game.components.GuessGrid
import com.example.pokedex.presentation.game.components.HintPanel
import com.example.pokedex.presentation.game.components.PokemonAutocompleteField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Même pattern que sur le Pokédex : HintPanel + AutocompleteField se cachent
    // quand l'utilisateur scrolle la grille vers le bas (utile en paysage)
    // et réapparaissent dès qu'il remonte.
    val showExtras by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction < 0.5f }
    }

    LaunchedEffect(state.error) {
        val message = state.error ?: return@LaunchedEffect
        snackbarHost.showSnackbar(message)
        viewModel.onDismissError()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                TopAppBar(
                    title = {
                        Text("Pokémondle", fontWeight = FontWeight.Bold)
                    },
                    actions = {
                        IconButton(onClick = viewModel::onRestart) {
                            Icon(Icons.Default.Refresh, contentDescription = "Nouvelle partie")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    scrollBehavior = scrollBehavior,
                )
                AnimatedVisibility(
                    visible = showExtras && state.mystery != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    ) {
                        HintPanel(
                            canRequestHint = state.canRequestHint,
                            remainingCooldown = state.remainingCooldown,
                            revealedHints = state.revealedHints,
                            mystery = state.mystery,
                            onRequestHint = viewModel::onRequestHint,
                        )
                        PokemonAutocompleteField(
                            value = state.currentInput,
                            onValueChange = viewModel::onInputChange,
                            suggestions = state.suggestions,
                            onSuggestionClick = viewModel::onGuessSelected,
                            enabled = !state.isValidating && !state.isWon,
                            isValidating = state.isValidating,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHost) { data -> Snackbar(data) }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when {
                state.isLoading && state.mystery == null -> LoadingContent()
                state.mystery == null -> EmptyStateContent(onRestart = viewModel::onRestart)
                else -> GuessGrid(
                    guesses = state.guesses,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                )
            }
            if (state.isWon && state.mystery != null) {
                VictoryDialog(
                    mystery = state.mystery!!,
                    guessCount = state.guesses.size,
                    onRestart = viewModel::onRestart,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = "Chargement de la liste des Pokémons...",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EmptyStateContent(onRestart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Casino,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
        )
        Text(
            text = "Pas de Pokémon mystère",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        Button(onClick = onRestart, modifier = Modifier.padding(top = 16.dp)) {
            Text("Démarrer une partie")
        }
    }
}

@Composable
private fun VictoryDialog(
    mystery: PokemonGameData,
    guessCount: Int,
    onRestart: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onRestart,
        title = {
            Text(
                text = "🎉 Bravo !",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                AsyncImage(
                    model = mystery.spriteUrl,
                    contentDescription = mystery.name,
                    modifier = Modifier.size(140.dp),
                )
                Text(
                    text = mystery.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Trouvé en $guessCount tentative" + if (guessCount > 1) "s" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            Button(onClick = onRestart) { Text("Rejouer") }
        },
    )
}
