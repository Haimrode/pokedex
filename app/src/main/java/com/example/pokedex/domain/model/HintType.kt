package com.example.pokedex.domain.model

/**
 * Indices que le joueur peut demander dans le Pokémondle.
 *
 * Ordre de la déclaration = ordre de révélation. Le ViewModel pioche le
 * prochain non révélé à chaque clic sur le bouton "Demander un indice".
 *
 *  1. [SILHOUETTE]    — sprite affiché en noir (forme reconnaissable)
 *  2. [FIRST_LETTER]  — première lettre du nom
 *  3. [TYPE_1]        — type primaire dévoilé
 *  4. [COLOR]         — couleur dominante dévoilée
 *
 * Total = 4 indices possibles. Au-delà, le bouton se désactive.
 */
enum class HintType {
    SILHOUETTE,
    FIRST_LETTER,
    TYPE_1,
    COLOR;

    companion object {
        const val COOLDOWN_GUESSES = 5
    }
}
