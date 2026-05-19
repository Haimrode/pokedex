package com.example.pokedex.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pokedex.domain.model.HintType
import com.example.pokedex.domain.model.PokemonGameData
import com.example.pokedex.presentation.common.colorNameFr
import com.example.pokedex.presentation.common.typeNameFr

/**
 * Panneau d'indices : bouton "Demander un indice" + chips des indices déjà
 * révélés au joueur.
 *
 * Cooldown (B1) : le bouton est désactivé tant que le joueur n'a pas fait
 * 5 nouvelles tentatives depuis le dernier indice. Le texte affiche le
 * nombre de tentatives restantes pour informer l'utilisateur.
 */
@Composable
fun HintPanel(
    canRequestHint: Boolean,
    remainingCooldown: Int,
    revealedHints: Set<HintType>,
    mystery: PokemonGameData?,
    onRequestHint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onRequestHint,
            enabled = canRequestHint,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = when {
                    revealedHints.size >= HintType.entries.size -> "Tous les indices utilisés"
                    remainingCooldown > 0 -> "Indice dispo dans $remainingCooldown essai" +
                        if (remainingCooldown > 1) "s" else ""
                    else -> "Demander un indice"
                },
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        if (mystery != null && revealedHints.isNotEmpty()) {
            // horizontalScroll : avec 5 indices possibles, on déborde en portrait.
            // Le joueur scrolle pour voir les indices au-delà de l'écran.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                revealedHints
                    .sortedBy { it.ordinal }
                    .forEach { hint -> HintChip(hint = hint, mystery = mystery) }
            }
        }
    }
}

@Composable
private fun HintChip(hint: HintType, mystery: PokemonGameData) {
    when (hint) {
        HintType.GENERATION -> AssistChip(
            onClick = {},
            label = { Text("Génération : Gen ${mystery.generation.number}") },
        )
        HintType.COLOR -> AssistChip(
            onClick = {},
            label = { Text("Couleur : ${colorNameFr(mystery.color)}") },
        )
        HintType.TYPE_1 -> AssistChip(
            onClick = {},
            label = { Text("Type 1 : ${typeNameFr(mystery.type1)}") },
        )
        HintType.FIRST_LETTER -> AssistChip(
            onClick = {},
            label = { Text("1re lettre : ${mystery.name.first().uppercase()}") },
        )
        HintType.SILHOUETTE -> SilhouetteChip(mystery.spriteUrl)
    }
}

/**
 * Affiche la silhouette du Pokémon en noir total (ColorMatrix qui met
 * R/G/B à 0). L'alpha est gardé pour que la forme reste lisible.
 */
@Composable
private fun SilhouetteChip(spriteUrl: String) {
    val blackTint = ColorFilter.colorMatrix(
        ColorMatrix(floatArrayOf(
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        ))
    )
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = spriteUrl,
            contentDescription = "Silhouette",
            colorFilter = blackTint,
            modifier = Modifier.size(48.dp),
        )
    }
}
