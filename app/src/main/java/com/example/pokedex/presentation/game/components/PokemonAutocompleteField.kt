package com.example.pokedex.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.pokedex.domain.model.PokemonRef

/**
 * Champ de saisie avec autocomplétion.
 *
 * - Tape une lettre → liste de suggestions sous le champ
 * - Tape un Pokémon dans la liste → guess validé
 * - Indicateur de chargement à droite pendant la validation
 *
 * Volontairement basique côté Material : on n'utilise pas
 * `ExposedDropdownMenuBox` (qui ouvre un menu détaché) parce qu'on veut
 * que la liste soit *visible* sous le champ tant qu'on tape, sans
 * masquer la grille au-dessus.
 */
@Composable
fun PokemonAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<PokemonRef>,
    onSuggestionClick: (PokemonRef) -> Unit,
    enabled: Boolean,
    isValidating: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tape un Pokémon...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (isValidating) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp),
                        strokeWidth = 2.dp,
                    )
                }
            },
            singleLine = true,
            enabled = enabled,
        )

        if (suggestions.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(8.dp),
                    ),
            ) {
                items(items = suggestions, key = { it.id }) { ref ->
                    SuggestionRow(ref = ref, onClick = { onSuggestionClick(ref) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(ref: PokemonRef, onClick: () -> Unit) {
    Text(
        text = "#${ref.id}  ${ref.name.replaceFirstChar { it.uppercase() }}",
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        style = MaterialTheme.typography.bodyMedium,
    )
}
