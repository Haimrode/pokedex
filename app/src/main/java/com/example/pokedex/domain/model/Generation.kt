package com.example.pokedex.domain.model

/**
 * Les 9 générations Pokémon, mappées sur l'ordre d'ids PokéAPI.
 *
 * Utilisé pour :
 *  - filtrer le Pokédex par génération (Phase 0)
 *  - afficher la colonne "Génération" dans le Pokémondle
 *  - comparer les générations entre guess et cible (+/-)
 *
 * Les plages d'ids sont stables (PokéAPI les garde figées), donc on les
 * code en dur ici. Total = 1025 Pokémons sur les 9 générations.
 */
enum class Generation(
    val number: Int,
    val region: String,
    val idRange: IntRange,
) {
    GEN_1(1, "Kanto",  1..151),
    GEN_2(2, "Johto",  152..251),
    GEN_3(3, "Hoenn",  252..386),
    GEN_4(4, "Sinnoh", 387..493),
    GEN_5(5, "Unova",  494..649),
    GEN_6(6, "Kalos",  650..721),
    GEN_7(7, "Alola",  722..809),
    GEN_8(8, "Galar",  810..905),
    GEN_9(9, "Paldea", 906..1025);

    /** "Gen 1 — Kanto" pour les chips et colonnes. */
    val displayName: String get() = "Gen $number — $region"

    /** Décalage à passer à PokéAPI (offset = premier id − 1). */
    val offset: Int get() = idRange.first - 1

    /** Nombre de Pokémons dans la génération (limit pour PokéAPI). */
    val count: Int get() = idRange.last - idRange.first + 1

    companion object {
        /**
         * Parse "generation-i", "generation-ii", … (renvoyé par /pokemon-species).
         * On n'utilise que les chiffres romains 1-9, donc une petite map suffit.
         */
        fun fromApiName(apiName: String): Generation? {
            val roman = apiName.substringAfterLast('-')
            val number = ROMAN_TO_INT[roman] ?: return null
            return entries.find { it.number == number }
        }

        /** Trouve la génération d'un Pokémon depuis son id Pokédex. */
        fun fromPokedexId(id: Int): Generation? =
            entries.find { id in it.idRange }

        private val ROMAN_TO_INT = mapOf(
            "i" to 1, "ii" to 2, "iii" to 3,
            "iv" to 4, "v" to 5, "vi" to 6,
            "vii" to 7, "viii" to 8, "ix" to 9,
        )
    }
}
