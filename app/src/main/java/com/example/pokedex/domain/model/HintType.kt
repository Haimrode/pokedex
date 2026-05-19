package com.example.pokedex.domain.model

/**
 * Indices que le joueur peut demander dans le Pokémondle.
 *
 * Ordre de la déclaration = ordre de révélation (du plus vague au plus
 * puissant). Le ViewModel pioche le prochain non révélé à chaque clic sur
 * le bouton "Demander un indice".
 *
 *  1. [GENERATION]    — la génération de la cible (Gen 1, Gen 2…)
 *  2. [COLOR]         — couleur dominante
 *  3. [TYPE_1]        — type primaire
 *  4. [FIRST_LETTER]  — première lettre du nom
 *  5. [SILHOUETTE]    — sprite en noir (gros indice visuel : reveal final)
 *
 * Total = 5 indices. Chaque indice ajoute [PENALTY_PER_HINT] tentatives
 * "fantômes" au score final affiché à la victoire.
 */
enum class HintType {
    GENERATION,
    COLOR,
    TYPE_1,
    FIRST_LETTER,
    SILHOUETTE;

    companion object {
        const val COOLDOWN_GUESSES = 5
        const val PENALTY_PER_HINT = 2
    }
}
