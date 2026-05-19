package com.example.pokedex.domain.battle

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamBattleEngineTest {

    @Test
    fun startBattle_supportsTeamsOfSixAndWaitsForPlayerMove() {
        val playerTeam = (1..6).map { id ->
            BattleCombatant(
                id = id,
                name = "player-$id",
                spriteUrl = "",
                isShiny = false,
                level = 10,
                types = listOf("normal"),
                maxHp = 100,
                attack = 50,
                defense = 40,
                speed = 30,
                moves = listOf("tackle", "quick-attack")
            )
        }
        val enemyTeam = (101..106).map { id ->
            BattleCombatant(
                id = id,
                name = "enemy-$id",
                spriteUrl = "",
                isShiny = false,
                level = 10,
                types = listOf("grass"),
                maxHp = 100,
                attack = 45,
                defense = 35,
                speed = 25,
                moves = listOf("vine-whip", "razor-leaf")
            )
        }

        val state = TeamBattleEngine.startBattle(playerTeam, enemyTeam)

        assertEquals(6, state.playerTeam.size)
        assertEquals(6, state.enemyTeam.size)
        assertTrue(state.awaitingPlayerMove)
        assertTrue(state.winner == null)
    }

    @Test
    fun performPlayerTurn_advancesBattleAndEventuallyFindsWinner() {
        val playerTeam = listOf(
            BattleCombatant(
                id = 1,
                name = "pikachu",
                spriteUrl = "",
                isShiny = false,
                level = 12,
                types = listOf("electric"),
                maxHp = 120,
                attack = 90,
                defense = 50,
                speed = 90,
                moves = listOf("thunderbolt", "quick-attack")
            )
        )
        val enemyTeam = listOf(
            BattleCombatant(
                id = 2,
                name = "squirtle",
                spriteUrl = "",
                isShiny = false,
                level = 10,
                types = listOf("water"),
                maxHp = 100,
                attack = 40,
                defense = 35,
                speed = 30,
                moves = listOf("water-gun", "tackle")
            )
        )

        var state = TeamBattleEngine.startBattle(playerTeam, enemyTeam)
        repeat(10) {
            if (state.winner != null) return@repeat
            state = TeamBattleEngine.performPlayerTurn(state, "thunderbolt", Random(42))
        }

        assertTrue(state.winner == BattleSide.PLAYER || state.winner == BattleSide.ENEMY)
        assertTrue(state.log.isNotEmpty())
    }
}

