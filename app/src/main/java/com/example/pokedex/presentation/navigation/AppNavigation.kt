package com.example.pokedex.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pokedex.presentation.favorites.FavoritesRoute

private sealed class BottomDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Pokedex : BottomDestination(
        route = "pokedex",
        label = "Pokédex",
        icon = Icons.AutoMirrored.Filled.List,
        selectedIcon = Icons.AutoMirrored.Filled.List
    )

    data object Favorites : BottomDestination(
        route = "favorites",
        label = "Favoris",
        icon = Icons.Filled.FavoriteBorder,
        selectedIcon = Icons.Filled.Favorite
    )
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val destinations = listOf(BottomDestination.Pokedex, BottomDestination.Favorites)

    Scaffold(
        bottomBar = { BottomBar(navController = navController, destinations = destinations) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomDestination.Pokedex.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomDestination.Pokedex.route) {
                PokedexPlaceholderScreen()
            }
            composable(BottomDestination.Favorites.route) {
                FavoritesRoute(onPokemonClick = { /* detail à brancher ensuite */ })
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

@Composable
private fun PokedexPlaceholderScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Écran Pokédex à brancher ensuite")
    }
}

