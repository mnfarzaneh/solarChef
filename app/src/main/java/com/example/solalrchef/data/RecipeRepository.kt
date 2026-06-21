package com.example.solalrchef.data
import com.example.solalrchef.R
import com.example.solalrchef.model.CookingStep
import com.example.solalrchef.model.Ingredient
import com.example.solalrchef.model.Recipe

object RecipeRepository {

    val recipes = listOf(

        Recipe(
            id = "bread",
            title = "نان خانگی",
            description = "...",
            image = R.drawable.breadmain,           // ← آیکون روی سولاردام
            detailImage = R.drawable.prebread,  // ← عکس صفحه جزئیات
            totalTime = "2 ساعت",
            cookTime = "15 دقیقه",
            yield = "2",
            calories = 220,
            ingredients = listOf(
                Ingredient("400", "گرم", "آرد نول"),
                Ingredient("100", "گرم", "آرد کامل"),
                Ingredient("10", "گرم", "نمک"),
                Ingredient("8", "گرم", "مخمر"),
                Ingredient("370", "گرم", "آب"),

            ),
            steps = listOf(
                CookingStep("آرد، نمک و مخمر و آب رو مخلوط کن"),
                CookingStep("خمیر رو 5 دقیقه با سری هوک با سرعت 1 ورز بده "),
                CookingStep("بعد از 10 دقیقه این بار با سری پدل با سرعت 1 ورز بده تا زمانی که خمیر دور پدل جمع بشه "),
                CookingStep("سولاردام رو با دمای 220 درجه پیش گرمایش کن"),
                CookingStep("14 دقیقه بپز تا طلایی بشه"),
            )
        ),

        Recipe(
            id = "cake",
            title = "Cake",
            image = R.drawable.cakemain,           // ← آیکون روی سولاردام
            detailImage = R.drawable.precake,  // ← عکس صفحه جزئیات
            description = "Mix eggs, sugar and flour...",

        ),

        Recipe(
            id = "pizza",
            title = "Pizza",
            image = R.drawable.pizzamain,           // ← آیکون روی سولاردام
            detailImage = R.drawable.prepizza,  // ← عکس صفحه جزئیات
            description = "Prepare dough and add toppings...",
            
        )
    )
}