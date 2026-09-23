package com.androidengineers.pocketcook.data

data class RecipeStep(val title: String, val instruction: String)
data class Recipe(val id: String, val title: String, val minutes: Int, val description: String, val ingredients: List<String>, val steps: List<RecipeStep>)
object Recipes {
 val all = listOf(
  Recipe("pasta", "Tomato pasta", 25, "A comforting bowl of pasta with ripe tomatoes, fresh basil and a little olive oil.",
   listOf("200 g dried spaghetti (wheat)", "400 g chopped tomatoes", "1 small onion, finely chopped", "1 tbsp olive oil", "A handful of fresh basil", "Salt and black pepper, to taste"),
   listOf(RecipeStep("Get everything ready", "Wash the basil, chop the onion and bring a large pot of water to a boil. Check ingredient labels for allergens."),
    RecipeStep("Soften the onion", "Warm the olive oil in a pan over medium heat. Add the onion and stir for about 5 minutes, until softened."),
    RecipeStep("Let the sauce simmer", "Add the tomatoes to the onion. Reduce to a gentle simmer and stir occasionally for 12–15 minutes. Season to taste."),
    RecipeStep("Cook the pasta", "Cook the spaghetti in the boiling water for the time on its packet. Carefully reserve a little cooking water before draining."),
    RecipeStep("Bring it together", "Toss the pasta with the sauce. Loosen with a splash of reserved water if needed, tear over the basil and serve."))),
  Recipe("bowl", "Chickpea bowl", 20, "Warm chickpeas, fluffy couscous and a bright lemon dressing.",
   listOf("120 g couscous (wheat)", "1 can chickpeas, drained and rinsed", "1 cucumber, diced", "150 g cherry tomatoes, halved", "1 tbsp olive oil", "Half a lemon", "Salt and pepper, to taste"),
   listOf(RecipeStep("Prepare the couscous", "Prepare the couscous with hot water according to its packet instructions. Let it stand, then fluff with a fork."),
    RecipeStep("Warm the chickpeas", "Warm the drained chickpeas in a pan with a splash of water for about 5 minutes, stirring."),
    RecipeStep("Chop the vegetables", "Wash and dice the cucumber and halve the tomatoes."),
    RecipeStep("Dress and serve", "Combine couscous, chickpeas and vegetables. Dress with olive oil and lemon juice; season to taste."))),
  Recipe("stirfry", "Vegetable stir-fry", 15, "Colourful vegetables with a simple ginger and soy sauce.",
   listOf("1 bell pepper, sliced", "1 carrot, thinly sliced", "150 g broccoli, small florets", "1 tbsp neutral cooking oil", "1 tsp grated ginger", "1 tbsp soy sauce (soy; may contain wheat)", "2 tbsp water"),
   listOf(RecipeStep("Prepare the vegetables", "Wash the vegetables. Slice the carrot thinly, cut the pepper into strips and divide broccoli into small florets."),
    RecipeStep("Start the pan", "Heat the oil in a large pan over medium-high heat. Add the carrot and broccoli and stir for 3 minutes."),
    RecipeStep("Add pepper and ginger", "Add the pepper and ginger. Stir for 2 minutes, then add the water and cook until the vegetables are tender to your liking."),
    RecipeStep("Season and serve", "Stir in the soy sauce. Cook for another minute and serve. Check sauce labels for allergens.")))
 )
 fun get(id: String) = all.firstOrNull { it.id == id } ?: all.first()
}
fun boundedStep(requested: Int, count: Int): Int = requested.coerceIn(0, (count - 1).coerceAtLeast(0))
interface ProgressStore { fun read(recipe: String): Int; fun write(recipe: String, step: Int) }
