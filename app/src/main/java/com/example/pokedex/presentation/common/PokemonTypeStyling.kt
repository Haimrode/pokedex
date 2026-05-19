package com.example.pokedex.presentation.common

import androidx.compose.ui.graphics.Color

/**
 * Traductions FR + couleurs iconiques des types, formes et couleurs PokéAPI.
 *
 * PokéAPI renvoie tout en anglais ("fire", "water", "quadruped", "yellow"…).
 * On centralise la localisation et le style ici pour pas dupliquer ces maps
 * dans les composables (TypeFilterRow, GuessGrid, HintPanel).
 *
 * Les couleurs reprennent les conventions des jeux Pokémon depuis Gen 1.
 * Source : https://bulbapedia.bulbagarden.net/wiki/Type
 */

private val TYPE_FR = mapOf(
    "normal"   to "Normal",
    "fire"     to "Feu",
    "water"    to "Eau",
    "electric" to "Électrik",
    "grass"    to "Plante",
    "ice"      to "Glace",
    "fighting" to "Combat",
    "poison"   to "Poison",
    "ground"   to "Sol",
    "flying"   to "Vol",
    "psychic"  to "Psy",
    "bug"      to "Insecte",
    "rock"     to "Roche",
    "ghost"    to "Spectre",
    "dragon"   to "Dragon",
    "dark"     to "Ténèbres",
    "steel"    to "Acier",
    "fairy"    to "Fée",
)

private val TYPE_COLOR = mapOf(
    "normal"   to Color(0xFFA8A878),
    "fire"     to Color(0xFFF08030),
    "water"    to Color(0xFF6890F0),
    "electric" to Color(0xFFF8D030),
    "grass"    to Color(0xFF78C850),
    "ice"      to Color(0xFF98D8D8),
    "fighting" to Color(0xFFC03028),
    "poison"   to Color(0xFFA040A0),
    "ground"   to Color(0xFFE0C068),
    "flying"   to Color(0xFFA890F0),
    "psychic"  to Color(0xFFF85888),
    "bug"      to Color(0xFFA8B820),
    "rock"     to Color(0xFFB8A038),
    "ghost"    to Color(0xFF705898),
    "dragon"   to Color(0xFF7038F8),
    "dark"     to Color(0xFF705848),
    "steel"    to Color(0xFFB8B8D0),
    "fairy"    to Color(0xFFEE99AC),
)

private val SHAPE_FR = mapOf(
    "ball"      to "Boule",
    "squiggle"  to "Sinueuse",
    "fish"      to "Poisson",
    "arms"      to "Bras",
    "blob"      to "Goutte",
    "upright"   to "Bipède",
    "legs"      to "Pattes",
    "quadruped" to "Quadrupède",
    "wings"     to "Ailes",
    "tentacles" to "Tentacules",
    "heads"     to "Têtes",
    "humanoid"  to "Humanoïde",
    "bug-wings" to "Ailes d'insecte",
    "armor"     to "Armure",
)

private val COLOR_FR = mapOf(
    "black"  to "Noir",
    "blue"   to "Bleu",
    "brown"  to "Marron",
    "gray"   to "Gris",
    "green"  to "Vert",
    "pink"   to "Rose",
    "purple" to "Violet",
    "red"    to "Rouge",
    "white"  to "Blanc",
    "yellow" to "Jaune",
)

/** Renvoie le nom FR du type Pokémon, ou la version capitalisée si inconnu. */
fun typeNameFr(type: String): String =
    TYPE_FR[type.lowercase()] ?: type.replaceFirstChar { it.uppercase() }

/** Couleur iconique d'un type Pokémon (Bulbapedia). Gris doux par défaut. */
fun typeColor(type: String): Color =
    TYPE_COLOR[type.lowercase()] ?: Color(0xFF68A090)

/** Nom FR d'une forme corporelle (shape). */
fun shapeNameFr(shape: String): String =
    SHAPE_FR[shape.lowercase()] ?: shape.replaceFirstChar { it.uppercase() }

/** Nom FR d'une couleur dominante. */
fun colorNameFr(color: String): String =
    COLOR_FR[color.lowercase()] ?: color.replaceFirstChar { it.uppercase() }
