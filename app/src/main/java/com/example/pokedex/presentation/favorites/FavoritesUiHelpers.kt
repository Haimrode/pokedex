@file:Suppress("unused")

package com.example.pokedex.presentation.favorites

import java.util.Locale

fun String.asDisplayName(): String = replaceFirstChar { char ->
    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
}

