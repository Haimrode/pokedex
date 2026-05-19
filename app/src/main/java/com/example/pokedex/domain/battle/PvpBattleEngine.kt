package com.example.pokedex.domain.battle

import kotlin.random.Random

data class PvpBattleState(
    val hostTeam: List<BattleCombatant>,
    val guestTeam: List<BattleCombatant>,
    val hostActiveIndex: Int = 0,
    val guestActiveIndex: Int = 0,
    val turnNumber: Int = 1,
    val hostChosenMove: String? = null,
    val guestChosenMove: String? = null,
    val hostAwaitingMove: Boolean = true,
    val guestAwaitingMove: Boolean = true,
    val winner: BattleSide? = null,
    val log: List<String> = emptyList()
) {
    fun activeHost(): BattleCombatant? = hostTeam.getOrNull(hostActiveIndex)?.takeIf { it.currentHp > 0 }
    fun activeGuest(): BattleCombatant? = guestTeam.getOrNull(guestActiveIndex)?.takeIf { it.currentHp > 0 }

    fun currentMovesForHost(): List<String> = activeHost()?.moves.orEmpty()
    fun currentMovesForGuest(): List<String> = activeGuest()?.moves.orEmpty()
}

object PvpBattleEngine {

    fun startBattle(hostTeam: List<BattleCombatant>, guestTeam: List<BattleCombatant>): PvpBattleState =
        PvpBattleState(
            hostTeam = hostTeam,
            guestTeam = guestTeam,
            hostActiveIndex = firstAliveIndex(hostTeam),
            guestActiveIndex = firstAliveIndex(guestTeam),
            log = listOf("Combat PVP lance: ${hostTeam.size} vs ${guestTeam.size}")
        )

    fun submitMove(
        state: PvpBattleState,
        side: BattleSide,
        moveName: String,
        random: Random = Random.Default
    ): PvpBattleState {
        if (state.winner != null) return state

        val normalizedMove = moveName.trim().ifBlank { "charge" }
        val withPending = when (side) {
            BattleSide.PLAYER -> state.copy(hostChosenMove = normalizedMove, hostAwaitingMove = false)
            BattleSide.ENEMY -> state.copy(guestChosenMove = normalizedMove, guestAwaitingMove = false)
        }

        return if (withPending.hostChosenMove != null && withPending.guestChosenMove != null) {
            resolveRound(withPending, random)
        } else {
            withPending
        }
    }

    fun resolveRound(
        state: PvpBattleState,
        random: Random = Random.Default
    ): PvpBattleState {
        val host = state.activeHost() ?: return state.copy(winner = BattleSide.ENEMY)
        val guest = state.activeGuest() ?: return state.copy(winner = BattleSide.PLAYER)

        val hostMove = resolveMove(host.moves, state.hostChosenMove ?: "charge")
        val guestMove = resolveMove(guest.moves, state.guestChosenMove ?: "charge")

        val hostEval = BattleMath.chooseBestMove(listOf(hostMove), host.types, guest.types, random)
        val guestEval = BattleMath.chooseBestMove(listOf(guestMove), guest.types, host.types, random)
        val hostFirst = host.speed >= guest.speed

        var updatedHostTeam = state.hostTeam
        var updatedGuestTeam = state.guestTeam
        var hostActiveIndex = state.hostActiveIndex
        var guestActiveIndex = state.guestActiveIndex
        val roundLog = mutableListOf<String>()

        fun attack(
            attacker: BattleCombatant,
            defender: BattleCombatant,
            attackerIsHost: Boolean,
            eval: BattleMath.EvaluatedMove
        ): BattleCombatant {
            val damage = BattleMath.computeDamage(
                attackerAttack = attacker.attack,
                defenderDefense = defender.defense,
                defenderMaxHp = defender.maxHp,
                movePower = eval.power,
                effectiveness = eval.effectiveness,
                hasStab = eval.type != null && attacker.types.contains(eval.type),
                random = random
            )
            val updatedDefender = defender.copy(currentHp = (defender.currentHp - damage).coerceAtLeast(0))
            val label = BattleMath.formatEffectiveness(eval.effectiveness)
            roundLog += "${attacker.name} utilise ${eval.name} ($label) et inflige $damage degats"
            // If move makes contact, attacker takes a small recoil (default 25% of damage)
            if (eval.contact && damage > 0) {
                val recoil = (damage / 4).coerceAtLeast(1)
                val attackerRemaining = (attacker.currentHp - recoil).coerceAtLeast(0)
                val updatedAttacker = attacker.copy(currentHp = attackerRemaining)
                roundLog += "${attacker.name} subit $recoil points de recul (contact)"
                if (attackerIsHost) {
                    updatedHostTeam = replaceAt(updatedHostTeam, hostActiveIndex, updatedAttacker)
                } else {
                    updatedGuestTeam = replaceAt(updatedGuestTeam, guestActiveIndex, updatedAttacker)
                }
            }
            if (attackerIsHost) {
                updatedGuestTeam = replaceAt(updatedGuestTeam, guestActiveIndex, updatedDefender)
            } else {
                updatedHostTeam = replaceAt(updatedHostTeam, hostActiveIndex, updatedDefender)
            }
            return updatedDefender
        }

        if (hostFirst) {
            val guestAfterHost = attack(host, guest, attackerIsHost = true, hostEval)
            if (guestAfterHost.currentHp > 0) {
                attack(guest.copy(currentHp = guestAfterHost.currentHp), host, attackerIsHost = false, guestEval)
            }
        } else {
            val hostAfterGuest = attack(guest, host, attackerIsHost = false, guestEval)
            if (hostAfterGuest.currentHp > 0) {
                attack(host.copy(currentHp = hostAfterGuest.currentHp), guest, attackerIsHost = true, hostEval)
            }
        }

        hostActiveIndex = nextAliveIndex(updatedHostTeam, hostActiveIndex)
        guestActiveIndex = nextAliveIndex(updatedGuestTeam, guestActiveIndex)

        val hostAlive = hostActiveIndex >= 0
        val guestAlive = guestActiveIndex >= 0
        val winner = when {
            !hostAlive -> BattleSide.ENEMY
            !guestAlive -> BattleSide.PLAYER
            else -> null
        }

        return state.copy(
            hostTeam = updatedHostTeam,
            guestTeam = updatedGuestTeam,
            hostActiveIndex = if (hostAlive) hostActiveIndex else state.hostActiveIndex,
            guestActiveIndex = if (guestAlive) guestActiveIndex else state.guestActiveIndex,
            turnNumber = state.turnNumber + 1,
            hostChosenMove = null,
            guestChosenMove = null,
            hostAwaitingMove = winner == null,
            guestAwaitingMove = winner == null,
            winner = winner,
            log = state.log + roundLog + when (winner) {
                BattleSide.PLAYER -> "Victoire du joueur host !"
                BattleSide.ENEMY -> "Victoire du joueur invite !"
                null -> "Le round est termine, suivant !"
            }
        )
    }

    private fun resolveMove(moves: List<String>, chosenMove: String): String =
        if (moves.isEmpty()) "charge" else moves.firstOrNull { it == chosenMove } ?: moves.first()

    private fun firstAliveIndex(team: List<BattleCombatant>): Int =
        team.indexOfFirst { it.currentHp > 0 }

    private fun nextAliveIndex(team: List<BattleCombatant>, currentIndex: Int): Int {
        val startIndex = currentIndex.coerceAtLeast(0)
        if ((team.getOrNull(startIndex)?.currentHp ?: 0) > 0) return startIndex
        for (index in startIndex + 1 until team.size) {
            if (team[index].currentHp > 0) return index
        }
        for (index in 0 until team.size) {
            if (team[index].currentHp > 0) return index
        }
        return -1
    }

    private fun replaceAt(team: List<BattleCombatant>, index: Int, combatant: BattleCombatant): List<BattleCombatant> {
        if (index !in team.indices) return team
        return team.toMutableList().also { it[index] = combatant }
    }
}

