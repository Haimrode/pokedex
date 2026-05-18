package com.example.pokedex.presentation.common

/**
 * Wrapper d'état pour toutes les opérations asynchrones de l'UI.
 * Trois états mutuellement exclusifs :
 *  - [Loading]  : opération en cours
 *  - [Success]  : données prêtes
 *  - [Error]    : échec avec message lisible
 *
 * Modélisé en sealed class pour forcer le `when` exhaustif côté UI :
 * impossible d'oublier un état au compile-time.
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
