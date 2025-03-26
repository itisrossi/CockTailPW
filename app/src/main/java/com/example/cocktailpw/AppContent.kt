package com.example.cocktailpw

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppContent() {
    val navController = rememberNavController()
    var selectedDrink by remember { mutableStateOf<Drink?>(null) }

    Scaffold(
        modifier = Modifier.background(Color.Black), // Sfondo nero
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
    CenterAlignedTopAppBar(
        title = { Text("Cocktail PW", color = Color.White) }, // Testo bianco per contrasto
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
    )
}

@Composable
fun BottomBar(onHomeClick: () -> Unit) {
    BottomAppBar(
        containerColor = Color.Black // Sfondo nero per la barra inferiore
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onHomeClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black)
            ) {
                Text("Home")
            }
        }
    }
}
