package com.example.pokedex.domain.model

/**
 * Une tentative du joueur dans le Pokémondle + sa comparaison cellule par
 * cellule avec la cible mystère.
 *
 * Une seule [GuessResult] = une ligne dans la grille affichée à l'écran.
 * Le mapping cellule → couleur se fait dans le composant `GuessCell`.
 *
 * @property guess           Données complètes du Pokémon proposé (à afficher).
 * @property isExactMatch    `true` si le joueur a trouvé la cible (mêmes id).
 *                            Sert au ViewModel pour déclencher la victoire.
 */
data class GuessResult(
    val guess: PokemonGameData,
    val number: CellComparison,
    val generation: CellComparison,
    val type1: CellComparison,
    val type2: CellComparison,
    val color: CellComparison,
    val shape: CellComparison,
    val evolutionStage: CellComparison,
    val height: CellComparison,
    val weight: CellComparison,
    val isExactMatch: Boolean,
)
