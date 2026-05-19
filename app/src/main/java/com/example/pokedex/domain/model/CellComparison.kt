package com.example.pokedex.domain.model

/**
 * État d'une cellule dans la grille Pokémondle après comparaison
 * guess vs cible. Détermine la couleur d'affichage et l'indicateur.
 *
 *  - [Exact]    → vert (valeur identique)
 *  - [Mismatch] → rouge (valeur différente, pas comparable en +/-)
 *  - [Higher]   → rouge + ↑ (la cible a une valeur plus haute)
 *  - [Lower]    → rouge + ↓ (la cible a une valeur plus basse)
 *  - [Partial]  → orange (cas typique : ton T1 est le T2 de la cible)
 *
 * `data object` car ces variantes n'ont pas de payload — elles sont des
 * étiquettes singleton, plus efficaces que `object` simple pour
 * l'égalité / le pattern matching.
 */
sealed class CellComparison {
    data object Exact : CellComparison()
    data object Mismatch : CellComparison()
    data object Higher : CellComparison()
    data object Lower : CellComparison()
    data object Partial : CellComparison()
}
