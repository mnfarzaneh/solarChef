//package com.mnfarzaneh.solalrchef.data.remote
//
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.SetOptions
//import com.google.firebase.firestore.DocumentSnapshot
//import com.mnfarzaneh.solalrchef.model.CookingStep
//import com.mnfarzaneh.solalrchef.model.Ingredient
//import com.mnfarzaneh.solalrchef.model.Recipe
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.callbackFlow
//import kotlinx.coroutines.tasks.await
//import javax.inject.Inject
//import javax.inject.Singleton
//
//sealed class SyncResult {
//    object Success : SyncResult()
//    data class Error(val message: String) : SyncResult()
//}
//
//@Singleton
//class FirestoreRepository @Inject constructor(
//    private val firestore: FirebaseFirestore,
//    private val authRepository: FirebaseAuthRepository
//) {
//
//    // ── مسیر دستورات کاربر توی Firestore ────────────────
//    // users/{userId}/recipes/{recipeId}
//    private fun userRecipesCollection(userId: String) =
//        firestore.collection("users")
//            .document(userId)
//            .collection("recipes")
//
//    // ── آپلود یک دستور به Firestore ──────────────────────
//    suspend fun uploadRecipe(recipe: Recipe): SyncResult {
//        val userId = authRepository.userId
//            ?: return SyncResult.Error("وارد نشدید")
//
//        return try {
//            val data = recipe.toFirestoreMap()
//
//            userRecipesCollection(userId)
//                .document(recipe.id)
//                .set(data, SetOptions.merge())
//                .await()
//
//            SyncResult.Success
//        } catch (e: Exception) {
//            SyncResult.Error("خطا در آپلود: ${e.message}")
//        }
//    }
//
//    // ── حذف یک دستور از Firestore ────────────────────────
//    suspend fun deleteRecipe(recipeId: String): SyncResult {
//        val userId = authRepository.userId
//            ?: return SyncResult.Error("وارد نشدید")
//
//        return try {
//            userRecipesCollection(userId)
//                .document(recipeId)
//                .delete()
//                .await()
//
//            SyncResult.Success
//        } catch (e: Exception) {
//            SyncResult.Error("خطا در حذف: ${e.message}")
//        }
//    }
//
//    // ── دانلود همه دستورات از Firestore ──────────────────
//    suspend fun downloadAllRecipes(): Result<List<Recipe>> {
//        val userId = authRepository.userId
//            ?: return Result.failure(Exception("وارد نشدید"))
//
//        return try {
//            val snapshot = userRecipesCollection(userId)
//                .get()
//                .await()
//
//            val recipes = snapshot.documents.mapNotNull { doc ->
//                doc.toRecipe()
//            }
//
//            Result.success(recipes)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    // ── گوش دادن به تغییرات realtime ─────────────────────
//    fun observeRecipes(userId: String): Flow<List<Recipe>> = callbackFlow {
//        val listener = userRecipesCollection(userId)
//            .addSnapshotListener { snapshot, error ->
//
//                if (error != null) {
//                    close(error)
//                    return@addSnapshotListener
//                }
//
//                val recipes = snapshot?.documents
//                    ?.mapNotNull { it.toRecipe() }
//                    ?: emptyList()
//
//                trySend(recipes)
//            }
//
//        awaitClose {
//            listener.remove()
//        }
//    }
//}
//
//// ─── تبدیل Recipe به Map برای Firestore ──────────────────
//
//private fun Recipe.toFirestoreMap(): Map<String, Any> {
//    return mapOf(
//        "id" to id,
//        "title" to title,
//        "description" to description,
//        "imagePath" to imagePath,
//        "author" to author,
//        "source" to source,
//        "totalTime" to totalTime,
//        "cookTime" to cookTime,
//        "yield" to yield,
//        "calories" to calories,
//        "difficulty" to difficulty,
//        "rating" to rating,
//        "isFavorite" to isFavorite,
//        "equipment"   to equipment,
//        "categoryIds" to categoryIds,  // ← اضافه کن
//        "ingredients" to ingredients.map { ing ->
//            mapOf(
//                "amount" to ing.amount,
//                "unit" to ing.unit,
//                "name" to ing.name
//            )
//        },
//
//        "steps" to steps.map {
//            it.instruction
//        },
//
//        "equipment" to equipment
//    )
//}
//
//// ─── تبدیل Firestore Document به Recipe ──────────────────
//
//private fun DocumentSnapshot.toRecipe(): Recipe? {
//    return try {
//
//        val ingredientsList =
//            (get("ingredients") as? List<*>)?.mapNotNull { item ->
//                (item as? Map<*, *>)?.let { map ->
//                    Ingredient(
//                        amount = map["amount"]?.toString() ?: "",
//                        unit = map["unit"]?.toString() ?: "",
//                        name = map["name"]?.toString() ?: ""
//                    )
//                }
//            } ?: emptyList()
//
//        val stepsList =
//            (get("steps") as? List<*>)?.mapNotNull { item ->
//                item?.toString()?.let {
//                    CookingStep(it)
//                }
//            } ?: emptyList()
//
//        val categoryIdsList = (get("categoryIds") as? List<*>)?.mapNotNull {
//            it?.toString()
//        } ?: emptyList()
//
//        val equipmentList =
//            (get("equipment") as? List<*>)?.mapNotNull {
//                it?.toString()
//            } ?: emptyList()
//
//        Recipe(
//            id = getString("id") ?: id,
//            title = getString("title") ?: "",
//            description = getString("description") ?: "",
//            image = 0,
//            imagePath = getString("imagePath") ?: "",
//            author = getString("author") ?: "",
//            source = getString("source") ?: "دستور شخصی",
//            totalTime = getString("totalTime") ?: "",
//            cookTime = getString("cookTime") ?: "",
//            yield = getString("yield") ?: "4",
//            calories = getLong("calories")?.toInt() ?: 0,
//            difficulty = getString("difficulty") ?: "متوسط",
//            rating = getDouble("rating")?.toFloat() ?: 5f,
//            isFavorite = getBoolean("isFavorite") ?: false,
//            ingredients = ingredientsList,
//            steps = stepsList,
//            equipment = equipmentList
//        )
//
//    } catch (e: Exception) {
//        null
//    }
//}