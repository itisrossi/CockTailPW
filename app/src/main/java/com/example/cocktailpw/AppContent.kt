package com.example.cocktailpw

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppContent() {
    val navController = rememberNavController()

    var selectedDrink by remember { mutableStateOf<Drink?>(null) }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomBar(onHomeClick = { navController.navigate("home") }) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(navController = navController)
            }
            composable("search") {
                SearchScreen(onDrinkSelected = { drink ->
                    selectedDrink = drink
                    navController.navigate("drinkDetails")
                })
            }
            composable("list") {
                ListScreen(onDrinkSelected = { drink ->
                    selectedDrink = drink
                    navController.navigate("drinkDetails")
                })
            }
            composable("guess") {
                GuessScreen(onDrinkSelected = { drink ->
                    selectedDrink = drink
                    navController.navigate("drinkDetails")
                })
            }
            composable("ingredientSearch") {
                IngredientSearchScreen(onDrinkSelected = { drink ->
                    selectedDrink = drink
                    navController.navigate("drinkDetails")
                })
            }
            composable("drinkDetails") {
                selectedDrink?.let { DrinkDetailsScreen(drinkId = it.idDrink) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    androidx.compose.material3.CenterAlignedTopAppBar(
        title = { androidx.compose.material3.Text("Dotti Cocktail") }
    )
}

@Composable
fun BottomBar(onHomeClick: () -> Unit) {

    androidx.compose.material3.BottomAppBar {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Button(onClick = onHomeClick) {
                androidx.compose.material3.Text("Home")
            }
        }
    }
}
