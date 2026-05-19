package com.example.pokedex.domain.battle

import kotlin.random.Random

data class BattleCombatant(
    val id: Int,
    val name: String,
    val spriteUrl: String,
    val isShiny: Boolean,
    val level: Int,
    val types: List<String>,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val moves: List<String>,
    val currentHp: Int = maxHp
)

enum class BattleSide {
    PLAYER,
    ENEMY
}

data class TeamBattleState(
    val playerTeam: List<BattleCombatant>,
    val enemyTeam: List<BattleCombatant>,
    val playerActiveIndex: Int = 0,
    val enemyActiveIndex: Int = 0,
    val turnNumber: Int = 1,
    val awaitingPlayerMove: Boolean = true,
    val winner: BattleSide? = null,
    val log: List<String> = emptyList()
) {
    fun activePlayer(): BattleCombatant? = playerTeam.getOrNull(playerActiveIndex)?.takeIf { it.currentHp > 0 }
    fun activeEnemy(): BattleCombatant? = enemyTeam.getOrNull(enemyActiveIndex)?.takeIf { it.currentHp > 0 }
    fun playerMoves(): List<String> = activePlayer()?.moves.orEmpty()
}

object TeamBattleEngine {

    fun startBattle(
        playerTeam: List<BattleCombatant>,
        enemyTeam: List<BattleCombatant>
    ): TeamBattleState = TeamBattleState(
        playerTeam = playerTeam,
        enemyTeam = enemyTeam,
        playerActiveIndex = firstAliveIndex(playerTeam),
        enemyActiveIndex = firstAliveIndex(enemyTeam),
        turnNumber = 1,
        awaitingPlayerMove = true,
        winner = null,
        log = listOf(
            "Combat d'ecole: ${playerTeam.size} Pokemon(s) contre ${enemyTeam.size} Pokemon(s)"
        )
    )

    fun performPlayerTurn(
        state: TeamBattleState,
        chosenMove: String,
        random: Random = Random.Default
    ): TeamBattleState {
        if (state.winner != null || !state.awaitingPlayerMove) return state

        val player = state.activePlayer() ?: return state.copy(winner = BattleSide.ENEMY, awaitingPlayerMove = false)
        val enemy = state.activeEnemy() ?: return state.copy(winner = BattleSide.PLAYER, awaitingPlayerMove = false)

        val playerMove = resolveMove(player.moves, chosenMove)
        val playerEval = BattleMath.chooseBestMove(listOf(playerMove), player.types, enemy.types, random)
        val enemyEval = BattleMath.chooseBestMove(enemy.moves, enemy.types, player.types, random)
        val playerFirst = player.speed >= enemy.speed

        var updatedPlayerTeam = state.playerTeam
        var updatedEnemyTeam = state.enemyTeam
        val roundLog = mutableListOf<String>()
        val playerActiveIndex = state.playerActiveIndex
        val enemyActiveIndex = state.enemyActiveIndex

        fun attack(
            attacker: BattleCombatant,
            defender: BattleCombatant,
            attackerIsPlayer: Boolean,
            eval: BattleMath.EvaluatedMove
        ): AttackOutcome {
            val damage = BattleMath.computeDamage(
                attackerAttack = attacker.attack,
                defenderDefense = defender.defense,
                defenderMaxHp = defender.maxHp,
                movePower = eval.power,
                effectiveness = eval.effectiveness,
                hasStab = eval.type != null && attacker.types.contains(eval.type),
                random = random
            )
            val remainingHp = (defender.currentHp - damage).coerceAtLeast(0)
            val updatedDefender = defender.copy(currentHp = remainingHp)
            if (attackerIsPlayer) {
                updatedEnemyTeam = replaceAt(updatedEnemyTeam, enemyActiveIndex, updatedDefender)
            } else {
                updatedPlayerTeam = replaceAt(updatedPlayerTeam, playerActiveIndex, updatedDefender)
            }
            // If move makes contact, apply optional recoil (default: 25% of damage to attacker)
            if (eval.contact && damage > 0) {
                val recoil = (damage / 4).coerceAtLeast(1)
                val attackerRemaining = (attacker.currentHp - recoil).coerceAtLeast(0)
                val updatedAttacker = attacker.copy(currentHp = attackerRemaining)
                if (attackerIsPlayer) {
                    updatedPlayerTeam = replaceAt(updatedPlayerTeam, playerActiveIndex, updatedAttacker)
                } else {
                    updatedEnemyTeam = replaceAt(updatedEnemyTeam, enemyActiveIndex, updatedAttacker)
                }
                roundLog += "${attacker.name} subit $recoil points de recul (contact)"
            }
            val label = BattleMath.formatEffectiveness(eval.effectiveness)
            roundLog += "${attacker.name} utilise ${eval.name} ($label) et inflige $damage degats"
            return AttackOutcome(updatedDefender, damage, label)
        }

        if (playerFirst) {
            val afterPlayer = attack(player, enemy, attackerIsPlayer = true, playerEval)
            val enemyDefeated = afterPlayer.defender.currentHp <= 0
            if (!enemyDefeated) {
                attack(enemy.copy(currentHp = afterPlayer.defender.currentHp), player, attackerIsPlayer = false, enemyEval)
            }
        } else {
            val afterEnemy = attack(enemy, player, attackerIsPlayer = false, enemyEval)
            val playerDefeated = afterEnemy.defender.currentHp <= 0
            if (!playerDefeated) {
                attack(player.copy(currentHp = afterEnemy.defender.currentHp), enemy, attackerIsPlayer = true, playerEval)
            }
        }

        val nextPlayerIndex = nextAliveIndex(updatedPlayerTeam, state.playerActiveIndex)
        val nextEnemyIndex = nextAliveIndex(updatedEnemyTeam, state.enemyActiveIndex)
        val playerAlive = nextPlayerIndex >= 0
        val enemyAlive = nextEnemyIndex >= 0

        val winner = when {
            !playerAlive -> BattleSide.ENEMY
            !enemyAlive -> BattleSide.PLAYER
            else -> null
        }

        return state.copy(
            playerTeam = updatedPlayerTeam,
            enemyTeam = updatedEnemyTeam,
            playerActiveIndex = if (playerAlive) nextPlayerIndex else state.playerActiveIndex,
            enemyActiveIndex = if (enemyAlive) nextEnemyIndex else state.enemyActiveIndex,
            turnNumber = state.turnNumber + 1,
            awaitingPlayerMove = winner == null,
            winner = winner,
            log = state.log + roundLog + when (winner) {
                BattleSide.PLAYER -> "Victoire de l'equipe du joueur !"
                BattleSide.ENEMY -> "Defaite... l'equipe adverse a gagne."
                null -> "Le combat continue !"
            }
        )
    }

    private fun resolveMove(moves: List<String>, chosenMove: String): String {
        if (moves.isEmpty()) return "charge"
        return moves.firstOrNull { it == chosenMove } ?: moves.first()
    }

    private fun firstAliveIndex(team: List<BattleCombatant>): Int =
        team.indexOfFirst { it.currentHp > 0 }

    private fun nextAliveIndex(team: List<BattleCombatant>, currentIndex: Int): Int {
        val startIndex = currentIndex.coerceAtLeast(0)
        val current = team.getOrNull(startIndex)
        if (current != null && current.currentHp > 0) return startIndex
        val next = team.indexOfFirstIndexed(startIndex + 1) { it.currentHp > 0 }
        if (next >= 0) return next
        return team.indexOfFirst { it.currentHp > 0 }
    }

    private fun replaceAt(team: List<BattleCombatant>, index: Int, combatant: BattleCombatant): List<BattleCombatant> {
        if (index !in team.indices) return team
        return team.toMutableList().also { it[index] = combatant }
    }

    private data class AttackOutcome(
        val defender: BattleCombatant,
        val damage: Int,
        val effectivenessLabel: String
    )

    private fun List<BattleCombatant>.indexOfFirstIndexed(startIndex: Int, predicate: (BattleCombatant) -> Boolean): Int {
        for (index in startIndex until size) {
            if (predicate(this[index])) return index
        }
        return -1
    }
}

