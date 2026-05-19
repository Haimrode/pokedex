package com.example.pokedex.domain.battle

import kotlin.random.Random

data class BattleFighter(
    val id: Int,
    val name: String,
    val spriteUrl: String,
    val isShiny: Boolean,
    val types: List<String>,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val moves: List<String>
)

data class BattleFighterState(
    val fighter: BattleFighter,
    val currentHp: Int
)

data class BattleTurn(
    val turnNumber: Int,
    val attackerId: Int,
    val defenderId: Int,
    val moveName: String,
    val effectivenessLabel: String,
    val damage: Int,
    val defenderRemainingHp: Int
)

data class BattleResult(
    val first: BattleFighter,
    val second: BattleFighter,
    val turns: List<BattleTurn>,
    val winnerId: Int
)

object BattleEngine {

    fun runDuel(
        first: BattleFighter,
        second: BattleFighter,
        random: Random = Random.Default
    ): BattleResult {
        var firstHp = first.maxHp
        var secondHp = second.maxHp
        val turns = mutableListOf<BattleTurn>()
        val firstStarts = first.speed >= second.speed
        var isFirstTurn = firstStarts
        var turnNumber = 1

        while (firstHp > 0 && secondHp > 0) {
            val attacker = if (isFirstTurn) first else second
            val defender = if (isFirstTurn) second else first
            val move = BattleMath.chooseBestMove(attacker.moves, attacker.types, defender.types, random)
            val rawDamage = BattleMath.computeDamage(
                attackerAttack = attacker.attack,
                defenderDefense = defender.defense,
                defenderMaxHp = defender.maxHp,
                movePower = move.power,
                effectiveness = move.effectiveness,
                hasStab = move.type != null && attacker.types.contains(move.type),
                random = random
            )
            val damage = rawDamage.coerceAtMost(if (isFirstTurn) secondHp else firstHp)
            val effectivenessLabel = BattleMath.formatEffectiveness(move.effectiveness)

            if (isFirstTurn) {
                secondHp -= damage
            } else {
                firstHp -= damage
            }

            turns += BattleTurn(
                turnNumber = turnNumber,
                attackerId = attacker.id,
                defenderId = defender.id,
                moveName = move.name,
                effectivenessLabel = effectivenessLabel,
                damage = damage,
                defenderRemainingHp = if (isFirstTurn) secondHp else firstHp
            )

            isFirstTurn = !isFirstTurn
            turnNumber += 1
        }

        return BattleResult(
            first = first,
            second = second,
            turns = turns,
            winnerId = if (firstHp > 0) first.id else second.id
        )
    }

}

