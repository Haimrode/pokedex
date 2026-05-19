package com.example.pokedex.domain.usecase

import com.example.pokedex.domain.model.CellComparison
import com.example.pokedex.domain.model.GuessResult
import com.example.pokedex.domain.model.PokemonGameData
import com.example.pokedex.domain.repository.GameRepository
import javax.inject.Inject

/**
 * Use case : valide une tentative du joueur.
 *
 * - Fetch les données complètes du Pokémon proposé
 * - Construit un [GuessResult] où chaque cellule indique si elle correspond,
 *   diffère, ou comporte un indice +/- (Higher/Lower)
 *
 * Le ViewModel n'a plus qu'à pousser le résultat dans la liste affichée.
 */
class ValidateGuessUseCase @Inject constructor(
    private val repository: GameRepository
) {

    suspend operator fun invoke(
        guessId: Int,
        mystery: PokemonGameData,
    ): Result<GuessResult> =
        repository.getGameData(guessId).map { guess ->
            buildGuessResult(guess, mystery)
        }

    private fun buildGuessResult(
        guess: PokemonGameData,
        mystery: PokemonGameData,
    ): GuessResult = GuessResult(
        guess = guess,
        number         = compareInt(guess.id, mystery.id),
        generation     = compareInt(guess.generation.number, mystery.generation.number),
        type1          = compareType(guess.type1, mystery.type1, mystery.type2),
        type2          = compareType(guess.type2, mystery.type2, mystery.type1),
        color          = compareString(guess.color, mystery.color),
        shape          = compareString(guess.shape, mystery.shape),
        evolutionStage = compareInt(guess.evolutionStage, mystery.evolutionStage),
        height         = compareDouble(guess.height, mystery.height),
        weight         = compareDouble(guess.weight, mystery.weight),
        isExactMatch   = guess.id == mystery.id,
    )

    private fun compareInt(guess: Int, target: Int): CellComparison = when {
        guess == target -> CellComparison.Exact
        target > guess  -> CellComparison.Higher
        else            -> CellComparison.Lower
    }

    private fun compareDouble(guess: Double, target: Double): CellComparison = when {
        guess == target -> CellComparison.Exact
        target > guess  -> CellComparison.Higher
        else            -> CellComparison.Lower
    }

    private fun compareString(guess: String, target: String): CellComparison =
        if (guess == target) CellComparison.Exact else CellComparison.Mismatch

    /**
     * Comparaison spéciale pour les slots de type :
     *  - Exact : même type, même slot (Plante au slot 1 vs Plante au slot 1)
     *  - Partial (orange) : le type est dans l'autre slot de la cible
     *    (Plante au slot 1 du guess, Plante au slot 2 de la cible)
     *  - Mismatch : type absent de la cible
     *  - Null vs Null = Exact (deux Pokémons monotype au slot 2)
     */
    private fun compareType(
        guess: String?,
        sameSlotTarget: String?,
        otherSlotTarget: String?,
    ): CellComparison = when {
        guess == sameSlotTarget -> CellComparison.Exact
        guess != null && guess == otherSlotTarget -> CellComparison.Partial
        else -> CellComparison.Mismatch
    }
}
