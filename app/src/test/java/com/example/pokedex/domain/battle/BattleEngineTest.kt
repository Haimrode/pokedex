package com.example.pokedex.domain.battle

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleEngineTest {

    @Test
    fun runDuel_returnsWinnerAndTurns() {
        val first = BattleFighter(
            id = 1,
            name = "bulbasaur",
            spriteUrl = "",
            isShiny = false,
            types = listOf("grass"),
            maxHp = 80,
            attack = 55,
            defense = 40,
            speed = 45,
            moves = listOf("tackle", "vine-whip")
        )
        val second = BattleFighter(
            id = 4,
            name = "charmander",
            spriteUrl = "",
            isShiny = false,
            types = listOf("fire"),
            maxHp = 80,
            attack = 52,
            defense = 43,
            speed = 65,
            moves = listOf("scratch", "ember")
        )

        val result = BattleEngine.runDuel(first, second, random = Random(42))

        assertTrue(result.turns.isNotEmpty())
        assertTrue(result.winnerId == first.id || result.winnerId == second.id)
        assertEquals(1, result.turns.first().turnNumber)
        assertTrue(result.turns.first().effectivenessLabel.isNotBlank())
    }
}

