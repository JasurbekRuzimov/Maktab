package com.maktab.app.network.repositories

import com.maktab.app.network.*
import com.maktab.app.network.models.*

class ChefRepository {
    private val api = RetrofitClient.chefService

    suspend fun getDashboard() = safeApiCall { api.getDashboard() }

    suspend fun getIngredients(
        search: String? = null,
        category: String? = null,
        status: String? = null
    ) = safeApiCall { api.getIngredients(search = search, category = category, status = status) }

    suspend fun createIngredient(request: IngredientRequest) = safeApiCall {
        api.createIngredient(request)
    }

    suspend fun updateIngredient(id: String, request: IngredientRequest) = safeApiCall {
        api.updateIngredient(id, request)
    }

    suspend fun deleteIngredient(id: String) = safeApiCall { api.deleteIngredient(id) }

    suspend fun getRecipes(search: String? = null, isActive: Boolean? = null) = safeApiCall {
        api.getRecipes(search = search, isActive = isActive)
    }

    suspend fun createRecipe(request: RecipeRequest) = safeApiCall { api.createRecipe(request) }

    suspend fun deleteRecipe(id: String) = safeApiCall { api.deleteRecipe(id) }

    suspend fun getMenuCalendar(from: String? = null, to: String? = null) = safeApiCall {
        api.getMenuCalendar(from = from, to = to)
    }

    suspend fun createMenuEntry(request: MenuEntryRequest) = safeApiCall {
        api.createMenuEntry(request)
    }

    suspend fun confirmMenuEntry(id: String) = safeApiCall { api.confirmMenuEntry(id) }

    suspend fun deleteMenuEntry(id: String) = safeApiCall { api.deleteMenuEntry(id) }

    suspend fun getMovements(
        from: String? = null,
        to: String? = null,
        movementType: String? = null
    ) = safeApiCall {
        api.getMovements(from = from, to = to, movementType = movementType)
    }
}
