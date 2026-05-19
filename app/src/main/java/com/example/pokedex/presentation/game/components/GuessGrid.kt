package com.example.pokedex.presentation.game.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pokedex.domain.model.CellComparison
import com.example.pokedex.domain.model.GuessResult

private val SPRITE_CELL_WIDTH = 80.dp
private val CELL_WIDTH = 72.dp
private val CELL_HEIGHT = 64.dp

private val ColorExact   = Color(0xFF4CAF50)
private val ColorWrong   = Color(0xFFE57373)
private val ColorPartial = Color(0xFFFFB74D)

/**
 * Grille des tentatives.
 *
 * Toutes les lignes partagent **le même [ScrollState] horizontal** — ça
 * fait que quand tu scrolles une ligne, les autres scrollent en même temps.
 * C'est ce qui permet l'effet "tableau" malgré la largeur > écran.
 *
 * Le header reste collé en haut via `LazyColumn` + `stickyHeader`.
 */
@Composable
fun GuessGrid(
    guesses: List<GuessResult>,
    modifier: Modifier = Modifier,
) {
    val hScroll = rememberScrollState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item(key = "header") {
            HeaderRow(hScroll)
        }
        items(items = guesses.asReversed(), key = { it.guess.id }) { result ->
            GuessRow(result = result, hScroll = hScroll)
        }
    }
}

@Composable
private fun HeaderRow(hScroll: ScrollState) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .horizontalScroll(hScroll)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        HeaderCell("", width = SPRITE_CELL_WIDTH)
        HeaderCell("N°")
        HeaderCell("Gen")
        HeaderCell("Type 1")
        HeaderCell("Type 2")
        HeaderCell("Couleur")
        HeaderCell("Forme")
        HeaderCell("Stade")
        HeaderCell("Hauteur")
        HeaderCell("Poids")
    }
}

@Composable
private fun HeaderCell(label: String, width: androidx.compose.ui.unit.Dp = CELL_WIDTH) {
    Box(
        modifier = Modifier.width(width).height(CELL_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GuessRow(result: GuessResult, hScroll: ScrollState) {
    Row(
        modifier = Modifier
            .horizontalScroll(hScroll)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SpriteCell(spriteUrl = result.guess.spriteUrl, name = result.guess.name)
        ValueCell("#${result.guess.id}", result.number)
        ValueCell("Gen ${result.guess.generation.number}", result.generation)
        ValueCell(result.guess.type1.replaceFirstChar { it.uppercase() }, result.type1)
        ValueCell(
            text = result.guess.type2?.replaceFirstChar { it.uppercase() } ?: "Aucun",
            comparison = result.type2,
        )
        ValueCell(result.guess.color.replaceFirstChar { it.uppercase() }, result.color)
        ValueCell(result.guess.shape.replaceFirstChar { it.uppercase() }, result.shape)
        ValueCell("Stade ${result.guess.evolutionStage}", result.evolutionStage)
        ValueCell("%.1f m".format(result.guess.height), result.height)
        ValueCell("%.1f kg".format(result.guess.weight), result.weight)
    }
}

@Composable
private fun SpriteCell(spriteUrl: String, name: String) {
    Column(
        modifier = Modifier.width(SPRITE_CELL_WIDTH).height(CELL_HEIGHT),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AsyncImage(
            model = spriteUrl,
            contentDescription = name,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = name.replaceFirstChar { it.uppercase() },
            fontSize = 10.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun ValueCell(text: String, comparison: CellComparison) {
    Box(
        modifier = Modifier
            .width(CELL_WIDTH)
            .height(CELL_HEIGHT)
            .clip(RoundedCornerShape(8.dp))
            .background(comparison.toBackground()),
        contentAlignment = Alignment.Center,
    ) {
        when (comparison) {
            CellComparison.Higher -> CellWithArrow(text, up = true)
            CellComparison.Lower  -> CellWithArrow(text, up = false)
            else -> Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CellWithArrow(text: String, up: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Icon(
            imageVector = if (up) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (up) "Cible plus haute" else "Cible plus basse",
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun CellComparison.toBackground(): Color = when (this) {
    CellComparison.Exact    -> ColorExact
    CellComparison.Mismatch -> ColorWrong
    CellComparison.Higher   -> ColorWrong
    CellComparison.Lower    -> ColorWrong
    CellComparison.Partial  -> ColorPartial
}
