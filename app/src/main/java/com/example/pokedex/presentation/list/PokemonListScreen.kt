package com.example.pokedex.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pokedex.domain.model.Generation
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.presentation.common.UiState
import com.example.pokedex.presentation.common.typeColor
import com.example.pokedex.presentation.common.typeNameFr
import com.example.pokedex.presentation.list.components.PokemonListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    onPokemonClick: (Int) -> Unit,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val availableTypes by viewModel.availableTypes.collectAsStateWithLifecycle()
    val selectedGeneration by viewModel.selectedGeneration.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Un filtre est "actif" si l'user a quitté l'état par défaut (Gen 1 + tous types).
    val hasActiveFilters = selectedGeneration != Generation.GEN_1 || selectedType != null

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // Column avec deux niveaux :
            //  - TopAppBar (collapse avec scrollBehavior)
            //  - SearchBar (toujours visible, sticky)
            // Les filtres Gen + Type sont sortis de l'écran et accessibles
            // via le bouton "Filtres" dans la TopAppBar — gain massif en paysage.
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Text(text = "Pokédex", fontWeight = FontWeight.Bold)
                    },
                    actions = {
                        IconButton(onClick = { showFilterSheet = true }) {
                            BadgedBox(
                                badge = {
                                    if (hasActiveFilters) {
                                        Badge()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filtres (génération et type)"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    scrollBehavior = scrollBehavior
                )
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange
                )
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            UiState.Loading -> LoadingContent(
                modifier = Modifier.padding(innerPadding)
            )
            is UiState.Error -> ErrorContent(
                message = state.message,
                onRetry = viewModel::loadPokemons,
                modifier = Modifier.padding(innerPadding)
            )
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyFilterContent()
                    }
                } else {
                    PokemonList(
                        pokemons = state.data,
                        onPokemonClick = onPokemonClick,
                        contentPadding = innerPadding
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            sheetState = sheetState,
            onDismiss = { showFilterSheet = false },
            selectedGeneration = selectedGeneration,
            onGenerationSelected = viewModel::onGenerationChange,
            availableTypes = availableTypes,
            selectedType = selectedType,
            onTypeSelected = viewModel::onTypeFilterChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    selectedGeneration: Generation,
    onGenerationSelected: (Generation) -> Unit,
    availableTypes: List<String>,
    selectedType: String?,
    onTypeSelected: (String?) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Filtres",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Text(
                text = "Génération",
                style = MaterialTheme.typography.titleMedium,
            )
            GenerationFilterRow(
                selectedGeneration = selectedGeneration,
                onGenerationSelected = onGenerationSelected,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Type",
                style = MaterialTheme.typography.titleMedium,
            )
            if (availableTypes.isEmpty()) {
                Text(
                    text = "Chargement des types…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                TypeFilterRow(
                    types = availableTypes,
                    selectedType = selectedType,
                    onTypeSelected = onTypeSelected,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Rechercher un Pokémon...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        singleLine = true
    )
}

@Composable
private fun GenerationFilterRow(
    selectedGeneration: Generation,
    onGenerationSelected: (Generation) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(items = Generation.entries, key = { it.number }) { generation ->
            FilterChip(
                selected = selectedGeneration == generation,
                onClick = { onGenerationSelected(generation) },
                label = { Text(generation.displayName) }
            )
        }
    }
}

@Composable
private fun TypeFilterRow(
    types: List<String>,
    selectedType: String?,
    onTypeSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item(key = "all") {
            FilterChip(
                selected = selectedType == null,
                onClick = { onTypeSelected(null) },
                label = { Text("Tous") }
            )
        }
        items(items = types, key = { it }) { type ->
            val color = typeColor(type)
            // Texte blanc ou noir selon la luminosité du fond — lisible sur
            // jaune (Électrik) comme sur violet sombre (Spectre).
            val labelOnColor = if (color.luminance() > 0.5f) Color.Black else Color.White
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(typeNameFr(type)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = color.copy(alpha = 0.30f),
                    selectedContainerColor = color,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedLabelColor = labelOnColor,
                ),
            )
        }
    }
}

@Composable
private fun PokemonList(
    pokemons: List<Pokemon>,
    onPokemonClick: (Int) -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = pokemons,
            key = { pokemon -> pokemon.id }
        ) { pokemon ->
            PokemonListItem(
                pokemon = pokemon,
                onClick = { onPokemonClick(pokemon.id) },
                modifier = Modifier.padding(horizontal = 16.dp)
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
            text = "Oups, une erreur est survenue",
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
private fun EmptyFilterContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Aucun Pokémon ne correspond à ta recherche",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
