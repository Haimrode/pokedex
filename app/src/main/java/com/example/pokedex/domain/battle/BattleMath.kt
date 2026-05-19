package com.example.pokedex.domain.battle

import kotlin.math.roundToInt
import kotlin.random.Random

object BattleMath {

    data class EvaluatedMove(
        val name: String,
        val type: String?,
        val power: Int,
        val category: String, // "physical" | "special" | "status"
        val contact: Boolean,
        val effectiveness: Double,
        val score: Double
    )

    fun chooseBestMove(
        moves: List<String>,
        attackerTypes: List<String>,
        defenderTypes: List<String>,
        random: Random = Random.Default
    ): EvaluatedMove {
        val candidateMoves = if (moves.isEmpty()) listOf("charge") else moves
        return candidateMoves
            .map { moveName ->
                val moveType = inferMoveType(moveName)
                val category = inferMoveCategory(moveName)
                val contact = inferMoveContact(moveName)
                val effectiveness = typeEffectiveness(moveType, defenderTypes)
                val stab = if (moveType != null && attackerTypes.contains(moveType)) 1.2 else 1.0
                val power = estimatePower(moveName)
                val adjustedPower = if (category == "status") 0 else power
                val score = (adjustedPower * stab * effectiveness) + random.nextDouble(0.0, 0.15)
                EvaluatedMove(
                    name = moveName,
                    type = moveType,
                    power = adjustedPower,
                    category = category,
                    contact = contact,
                    effectiveness = effectiveness,
                    score = score
                )
            }
            .maxByOrNull { it.score }
            ?: EvaluatedMove("charge", null, 40, category = "physical", contact = true, effectiveness = 1.0, score = 0.0)
    }

    fun computeDamage(
        attackerAttack: Int,
        defenderDefense: Int,
        defenderMaxHp: Int,
        movePower: Int,
        effectiveness: Double,
        hasStab: Boolean,
        random: Random = Random.Default
    ): Int {
        // If move has no power (status) or is immune, no damage
        if (movePower <= 0 || effectiveness <= 0.0) return 0
        val attackOverDefense = attackerAttack.toDouble() / defenderDefense.coerceAtLeast(1)
        val base = 12.0 + (movePower * 0.28) + (attackOverDefense * 8.0)
        val stab = if (hasStab) 1.2 else 1.0
        val randomFactor = random.nextDouble(from = 0.92, until = 1.06)
        val raw = base * stab * effectiveness * randomFactor

        val maxAllowed = (defenderMaxHp * 0.22).roundToInt().coerceAtLeast(8)
        return raw.roundToInt().coerceIn(4, maxAllowed)
    }

    fun formatEffectiveness(multiplier: Double): String = when {
        multiplier == 0.0 -> "aucun effet"
        multiplier >= 1.8 -> "super efficace"
        multiplier <= 0.6 -> "peu efficace"
        else -> "efficacite normale"
    }

    fun inferMoveType(moveName: String): String? {
        // Normalize move name: lowercase, remove accents, replace non-alphanum with space
        val raw = moveName.lowercase()
        val normalized = java.text.Normalizer.normalize(raw, java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .replace("[^a-z0-9]".toRegex(), " ")

        // First try matching English keywords (existing map)
        val byKeyword = moveTypeKeywords.entries.firstOrNull { (_, keywords) ->
            keywords.any { keyword -> normalized.contains(keyword) }
        }
        if (byKeyword != null) return byKeyword.key

        // Some move names come from a French localization (eg. "charge", "flamèche").
        // Map common French substrings to the canonical English type keys used in typeChart.
        val frenchToType = mapOf(
            // fire
            "flameche" to "fire",
            "flameche" to "fire",
            "feu" to "fire",
            "boutefeu" to "fire",
            "embras" to "fire",
            // water
            "pistolet" to "water",
            "eau" to "water",
            "aqu" to "water",
            // grass
            "fouet" to "grass",
            "liane" to "grass",
            "feuille" to "grass",
            // electric
            "eclair" to "electric",
            "tonnerre" to "electric",
            // normal / contact
            "charge" to "normal",
            "coup" to "normal",
            "pique" to "normal",
            // fighting
            "poing" to "fighting",
            "combat" to "fighting",
            // ice
            "glace" to "ice",
            // rock / ground
            "lance" to "rock",
            // psychic
            "psy" to "psychic",
            // flying
            "vol" to "flying"
        )

        val match = frenchToType.entries.firstOrNull { (kw, _) -> normalized.contains(kw) }
        return match?.value
    }

    fun inferMoveCategory(moveName: String): String {
        val lower = moveName.lowercase()
        return when {
            lower.contains("heal") || lower.contains("recover") || lower.contains("status") || lower.contains("growl") || lower.contains("leer") || lower.contains("synth") || lower.contains("protect") || lower.contains("reflect") -> "status"
            lower.contains("beam") || lower.contains("blast") || lower.contains("wave") || lower.contains("bolt") || lower.contains("flame") || lower.contains("flam") || lower.contains("sur") || lower.contains("blast") -> "special"
            lower.contains("punch") || lower.contains("tackle") || lower.contains("bite") || lower.contains("slash") || lower.contains("claw") || lower.contains("headbutt") || lower.contains("kick") -> "physical"
            else -> "physical"
        }
    }

    fun inferMoveContact(moveName: String): Boolean {
        val lower = moveName.lowercase()
        val contactKeywords = listOf("punch", "tackle", "bite", "slash", "claw", "headbutt", "double-edge", "recoil", "body slam", "slam", "charge", "cut", "leer")
        return contactKeywords.any { lower.contains(it) }
    }

    fun typeEffectiveness(moveType: String?, defenderTypes: List<String>): Double {
        if (moveType == null || defenderTypes.isEmpty()) return 1.0
        val against = typeChart[moveType] ?: return 1.0
        return defenderTypes.fold(1.0) { acc, defenderType ->
            acc * (against[defenderType.lowercase()] ?: 1.0)
        }
    }

    private fun estimatePower(moveName: String): Int {
        val lower = moveName.lowercase()
        return when {
            lower.contains("beam") || lower.contains("blast") || lower.contains("storm") -> 85
            lower.contains("punch") || lower.contains("kick") || lower.contains("claw") -> 68
            lower.contains("bite") || lower.contains("slash") || lower.contains("fang") -> 64
            lower.contains("shot") || lower.contains("bolt") || lower.contains("wave") -> 72
            lower.contains("quick") || lower.contains("feint") || lower.contains("jab") -> 52
            else -> 58
        }
    }

    private val moveTypeKeywords: Map<String, List<String>> = mapOf(
        "fire" to listOf("fire", "flame", "burn", "heat", "ember"),
        "water" to listOf("water", "aqua", "bubble", "hydro", "surf"),
        "grass" to listOf("leaf", "vine", "seed", "spore", "petal"),
        "electric" to listOf("thunder", "spark", "volt", "zap", "shock"),
        "ground" to listOf("earth", "mud", "quake", "sand"),
        "rock" to listOf("rock", "stone"),
        "ice" to listOf("ice", "frost", "blizzard", "snow"),
        "fighting" to listOf("punch", "kick", "karate", "chop"),
        "poison" to listOf("poison", "toxic", "acid", "venom"),
        "flying" to listOf("wing", "air", "gust", "aerial"),
        "psychic" to listOf("psy", "mind", "zen"),
        "bug" to listOf("bug", "x-scissor", "signal"),
        "ghost" to listOf("shadow", "night", "phantom", "hex"),
        "dragon" to listOf("dragon", "draco"),
        "dark" to listOf("dark", "snarl", "foul", "night"),
        "steel" to listOf("steel", "metal", "iron"),
        "fairy" to listOf("fairy", "charm", "kiss"),
        "normal" to listOf("tackle", "slam", "scratch", "quick")
    )

    // Basic type chart with some common immunities included (not exhaustive but covers common cases)
    private val typeChart: Map<String, Map<String, Double>> = mapOf(
        "fire" to mapOf("grass" to 2.0, "water" to 0.5, "rock" to 0.5, "ice" to 2.0),
        "water" to mapOf("fire" to 2.0, "grass" to 0.5, "rock" to 2.0, "ground" to 2.0),
        "grass" to mapOf("water" to 2.0, "fire" to 0.5, "ground" to 2.0, "rock" to 2.0, "flying" to 0.5),
        // electric has no effect on ground (immunity)
        "electric" to mapOf("water" to 2.0, "flying" to 2.0, "ground" to 0.0, "grass" to 0.5),
        // ground has no effect on flying (immunity)
        "ground" to mapOf("electric" to 2.0, "fire" to 2.0, "grass" to 0.5, "rock" to 2.0, "flying" to 0.0),
        "rock" to mapOf("fire" to 2.0, "flying" to 2.0, "ground" to 0.5),
        "ice" to mapOf("grass" to 2.0, "dragon" to 2.0, "water" to 0.5, "fire" to 0.5),
        // fighting moves don't affect ghost-type (immunity)
        "fighting" to mapOf("normal" to 2.0, "rock" to 2.0, "psychic" to 0.5, "ghost" to 0.0),
        // poison does not affect steel (immunity)
        "poison" to mapOf("grass" to 2.0, "rock" to 0.5, "ground" to 0.5, "steel" to 0.0),
        "flying" to mapOf("grass" to 2.0, "electric" to 0.5, "rock" to 0.5),
        // psychic has no effect on dark (immunity)
        "psychic" to mapOf("fighting" to 2.0, "poison" to 2.0, "steel" to 0.5, "dark" to 0.0),
        "bug" to mapOf("grass" to 2.0, "fire" to 0.5, "fighting" to 0.5),
        // ghost has no effect on normal (immunity)
        "ghost" to mapOf("psychic" to 2.0, "normal" to 0.0),
        "dragon" to mapOf("dragon" to 2.0),
        "dark" to mapOf("psychic" to 2.0, "fighting" to 0.5, "fairy" to 0.5),
        "steel" to mapOf("ice" to 2.0, "rock" to 2.0, "water" to 0.5, "fire" to 0.5),
        "fairy" to mapOf("dragon" to 2.0, "fire" to 0.5, "poison" to 0.5),
        // normal and others default
        "normal" to emptyMap()
    )
}

