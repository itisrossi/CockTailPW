package com.example.cocktailpw

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterDropdownMenu(selectedLetter: String, onLetterSelected: (String) -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    val letters = ('A'..'Z').map { it.toString() }

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = !expanded.value }
    ) {
        TextField(
            value = selectedLetter,
            onValueChange = {},
            readOnly = true,
            label = { Text("Seleziona lettera") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value)
            },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            letters.forEach { letter ->
                DropdownMenuItem(
                    text = { Text(letter) },
                    onClick = {
                        onLetterSelected(letter)
                        expanded.value = false
                    }
                )
            }
        }
    }
}


@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigate("search") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Cerca Cocktail")
        }
        Button(
            onClick = { navController.navigate("list") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Lista Cocktail")
        }
        Button(
            onClick = { navController.navigate("guess") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Indovina il Cocktail")
        }
        Button(
            onClick = { navController.navigate("ingredientSearch") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Cerca Ingredienti")
        }
    }
}


@Composable
fun SearchScreen(onDrinkSelected: (Drink) -> Unit) {
    var searchText by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<Drink>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { newText -> searchText = newText },
            label = { Text("Cerca Cocktail per nome") },
            modifier = Modifier.fillMaxWidth()
        )
        LaunchedEffect(searchText) {
            delay(500)
            if (searchText.length >= 2) {
                isLoading = true
                suggestions = fetchCocktailsByName(searchText)
                isLoading = false
            } else {
                suggestions = emptyList()
            }
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(suggestions) { drink ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onDrinkSelected(drink) }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = drink.strDrinkThumb,
                            contentDescription = drink.strDrink,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = drink.strDrink, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}


@Composable
fun ListScreen(onDrinkSelected: (Drink) -> Unit) {
    var selectedLetter by remember { mutableStateOf("A") }
    var cocktails by remember { mutableStateOf<List<Drink>>(emptyList()) }

    LaunchedEffect(selectedLetter) {
        cocktails = fetchCocktailsByLetter(selectedLetter)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LetterDropdownMenu(selectedLetter) { newLetter ->
            selectedLetter = newLetter
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (cocktails.isEmpty()) {
            Text("Nessun cocktail trovato per la lettera $selectedLetter")
        } else {
            LazyColumn {
                items(cocktails) { cocktail ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onDrinkSelected(cocktail) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = cocktail.strDrinkThumb,
                                contentDescription = cocktail.strDrink,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = cocktail.strDrink, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun GuessScreen(onDrinkSelected: (Drink) -> Unit) {
    var randomDrink by remember { mutableStateOf<Drink?>(null) }
    var guessedLetters by remember { mutableStateOf(setOf<Char>()) }
    var wrongGuesses by remember { mutableStateOf(0) }
    val maxWrong = 6
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        randomDrink = fetchRandomCocktail()
        guessedLetters = emptySet()
        wrongGuesses = 0
    }

    if (randomDrink == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val targetWord = randomDrink!!.strDrink.uppercase()
    val displayWord = targetWord.map {
        if (!it.isLetter()) it else if (guessedLetters.contains(it)) it else '_'
    }.joinToString(" ")

    val hasWon = displayWord.replace(" ", "") == targetWord.replace(" ", "")
    val hasLost = wrongGuesses >= maxWrong

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "The Hangman - Guess the Cocktail",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = displayWord, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Errori: $wrongGuesses / $maxWrong")
        Spacer(modifier = Modifier.height(16.dp))
        if (!hasWon && !hasLost) {
            val letters = ('A'..'Z').toList()
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                items(letters.size) { index ->
                    val letter = letters[index]
                    Button(
                        onClick = {
                            guessedLetters += letter
                            if (!targetWord.contains(letter)) {
                                wrongGuesses++
                            }
                        },
                        enabled = !guessedLetters.contains(letter),
                        modifier = Modifier.size(56.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        } else {
            if (hasWon) {
                Text(
                    "Hai vinto! Il cocktail era: ${randomDrink!!.strDrink}",
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (hasLost) {
                Text(
                    "Hai perso! Il cocktail era: ${randomDrink!!.strDrink}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onDrinkSelected(randomDrink!!) }) {
                Text("Visualizza dettagli")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                guessedLetters = emptySet()
                wrongGuesses = 0
                coroutineScope.launch {
                    randomDrink = fetchRandomCocktail()
                }
            }) {
                Text("Ricomincia")
            }
        }
    }
}


@Composable
fun DrinkDetailsScreen(drinkId: String) {
    var drink by remember { mutableStateOf<Drink?>(null) }
    LaunchedEffect(drinkId) {
        drink = fetchDrinkDetails(drinkId)
    }

    if (drink == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val ingredients = listOfNotNull(
        drink!!.strIngredient1?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure1 ?: "") },
        drink!!.strIngredient2?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure2 ?: "") },
        drink!!.strIngredient3?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure3 ?: "") },
        drink!!.strIngredient4?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure4 ?: "") },
        drink!!.strIngredient5?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure5 ?: "") },
        drink!!.strIngredient6?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure6 ?: "") },
        drink!!.strIngredient7?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure7 ?: "") },
        drink!!.strIngredient8?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure8 ?: "") },
        drink!!.strIngredient9?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure9 ?: "") },
        drink!!.strIngredient10?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure10 ?: "") },
        drink!!.strIngredient11?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure11 ?: "") },
        drink!!.strIngredient12?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure12 ?: "") },
        drink!!.strIngredient13?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure13 ?: "") },
        drink!!.strIngredient14?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure14 ?: "") },
        drink!!.strIngredient15?.takeIf { it.isNotBlank() }?.let { it to (drink!!.strMeasure15 ?: "") }
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(text = drink!!.strDrink, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        if (!drink!!.strCategory.isNullOrBlank()) {
            Text("Categoria: ${drink!!.strCategory}", style = MaterialTheme.typography.bodyLarge)
        }
        if (!drink!!.strAlcoholic.isNullOrBlank()) {
            Text("Alcolico: ${drink!!.strAlcoholic}", style = MaterialTheme.typography.bodyLarge)
        }
        if (!drink!!.strGlass.isNullOrBlank()) {
            Text("Tipo di bicchiere: ${drink!!.strGlass}", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            model = drink!!.strDrinkThumb,
            contentDescription = drink!!.strDrink,
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Istruzioni:", style = MaterialTheme.typography.titleMedium)
        Text(text = drink!!.strInstructions ?: "Nessuna istruzione disponibile", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (ingredients.isNotEmpty()) {
            Text("Ingredienti:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ingredients.forEach { (ingredient, measure) ->
                Text(text = "- $ingredient: $measure", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}


@Composable
fun IngredientSearchScreen(onDrinkSelected: (Drink) -> Unit) {
    var ingredientQuery by remember { mutableStateOf("") }
    var ingredientList by remember { mutableStateOf<List<String>>(emptyList()) }
    var filteredIngredients by remember { mutableStateOf<List<String>>(emptyList()) }
    var cocktails by remember { mutableStateOf<List<Drink>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        val response = fetchIngredientsList()
        ingredientList = response ?: emptyList()
        filteredIngredients = ingredientList
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = ingredientQuery,
            onValueChange = { query ->
                ingredientQuery = query
                filteredIngredients = if (query.isEmpty()) ingredientList
                else ingredientList.filter { it.contains(query, ignoreCase = true) }
            },
            label = { Text("Cerca Ingredienti") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        if (cocktails.isNotEmpty()) {
            Text("Cocktail trovati per l'ingrediente \"$ingredientQuery\":")
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(cocktails) { drink ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onDrinkSelected(drink) }
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = drink.strDrinkThumb,
                                contentDescription = drink.strDrink,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = drink.strDrink, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        } else {

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filteredIngredients) { ingredient ->
                    Text(
                        text = ingredient,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                ingredientQuery = ingredient
                                coroutineScope.launch {
                                    isLoading = true
                                    cocktails = fetchCocktailsByIngredient(ingredient)
                                    isLoading = false
                                }
                            }
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}



suspend fun fetchCocktailsByLetter(letter: String): List<Drink> {
    return withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getCocktailsByLetter(letter.lowercase())
            response.drinks ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

suspend fun fetchCocktailsByName(query: String): List<Drink> {
    return withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.searchCocktails(query)
            response.drinks ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

suspend fun fetchRandomCocktail(): Drink? {
    return withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getRandomCocktail()
            response.drinks?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

suspend fun fetchCocktailsByIngredient(ingredient: String): List<Drink> {
    return withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getCocktailsByIngredient(ingredient)
            response.drinks ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

suspend fun fetchIngredientsList(): List<String>? {
    return withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.getIngredientsList("list")
            response.drinks?.map { it.ingredient }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

