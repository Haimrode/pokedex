package com.example.pokedex.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pokedex.presentation.detail.PokemonDetailRoute
import com.example.pokedex.presentation.favorites.FavoritesRoute
import com.example.pokedex.presentation.game.GameScreen
import com.example.pokedex.presentation.garden.GardenRoute
import com.example.pokedex.presentation.list.PokemonListScreen
import com.example.pokedex.presentation.online.OnlinePvpRoute

private object Routes {
    const val POKEDEX = "pokedex"
    const val FAVORITES = "favorites"
    const val GAME = "game"
    const val GARDEN = "garden"
    const val ONLINE = "online"
    const val DETAIL_PATTERN = "detail/{id}"
    const val DETAIL_ARG_ID = "id"

    fun detail(id: Int): String = "detail/$id"
}

private sealed class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Pokedex : BottomDestination(
        route = Routes.POKEDEX,
        label = "Pokédex",
        icon = Icons.AutoMirrored.Filled.List,
        selectedIcon = Icons.AutoMirrored.Filled.List
    )

    data object Favorites : BottomDestination(
        route = Routes.FAVORITES,
        label = "Favoris",
        icon = Icons.Filled.FavoriteBorder,
        selectedIcon = Icons.Filled.Favorite
    )

    data object Game : BottomDestination(
        route = Routes.GAME,
        label = "Pokémondle",
        icon = Icons.Filled.Casino,
        selectedIcon = Icons.Filled.Casino
    )

    data object Garden : BottomDestination(
        route = Routes.GARDEN,
        label = "Jardin",
        icon = Icons.Filled.Eco,
        selectedIcon = Icons.Filled.Eco
    )

    data object Online : BottomDestination(
        route = Routes.ONLINE,
        label = "En ligne",
        icon = Icons.Filled.Public,
        selectedIcon = Icons.Filled.Public
    )
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val destinations = listOf(
        BottomDestination.Pokedex,
        BottomDestination.Favorites,
        BottomDestination.Game,
        BottomDestination.Garden,
        BottomDestination.Online,
    )

    val navigateToDetail: (Int) -> Unit = { id ->
        navController.navigate(Routes.detail(id))
    }
    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController, destinations = destinations) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.POKEDEX,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.POKEDEX) {
                PokemonListScreen(onPokemonClick = navigateToDetail)
            }
            composable(Routes.FAVORITES) {
                FavoritesRoute(onPokemonClick = navigateToDetail)
            }
            composable(Routes.GAME) {
                GameScreen()
            }
            composable(Routes.GARDEN) {
                GardenRoute(onPokemonClick = navigateToDetail)
            }
            composable(Routes.ONLINE) {
                OnlinePvpRoute(onPokemonClick = navigateToDetail)
            }
            composable(
                route = Routes.DETAIL_PATTERN,
                arguments = listOf(
                    navArgument(Routes.DETAIL_ARG_ID) { type = NavType.IntType }
                )
            ) {
                PokemonDetailRoute(onBack = navigateBack)
            }
        }
    }
}

@Composable
private fun BottomBar(
    navController: NavHostController,
    destinations: List<BottomDestination>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        destinations.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = { Text(text = destination.label) }
            )
        }
    }
}
