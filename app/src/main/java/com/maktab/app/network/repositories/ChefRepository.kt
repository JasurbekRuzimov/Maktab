package com.maktab.app.network.repositories

import android.util.Log
import com.maktab.app.network.ApiResult
import com.maktab.app.network.RetrofitClient
import com.maktab.app.network.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response

class ChefRepository {

    private val api = RetrofitClient.chefService
    private val tag = "ChefAPI"

    // ─── Raw HTTP → JSONObject ────────────────────────────────────────────────

    private suspend fun rawCall(
        call: suspend () -> Response<ResponseBody>
    ): ApiResult<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val response = call()
            val bodyStr = response.body()?.string() ?: "{}"
            Log.d(tag, "[${response.code()}] $bodyStr")
            if (response.isSuccessful) {
                ApiResult.Success(JSONObject(bodyStr))
            } else {
                val msg = try {
                    val err = JSONObject(bodyStr)
                    err.optString("message", "").ifEmpty { null }
                        ?: err.optJSONObject("errors")?.toString()
                        ?: statusMessage(response.code())
                } catch (_: Exception) { statusMessage(response.code()) }
                ApiResult.Error(msg, response.code())
            }
        } catch (e: java.net.UnknownHostException) {
            ApiResult.Error("Internet aloqasi yo'q")
        } catch (e: java.net.SocketTimeoutException) {
            ApiResult.Error("So'rov vaqti tugadi. Qayta urinib ko'ring.")
        } catch (e: Exception) {
            Log.e(tag, "Xato: ${e.message}", e)
            ApiResult.Error(e.message ?: "Noma'lum xato")
        }
    }

    private fun statusMessage(code: Int) = when (code) {
        401 -> "Avtorizatsiya talab etiladi. Qayta kiring."
        403 -> "Ruxsat yo'q."
        404 -> "Ma'lumot topilmadi."
        422 -> "Noto'g'ri ma'lumot kiritildi."
        500 -> "Server xatosi. Keyinroq urinib ko'ring."
        else -> "Xato: $code"
    }

    // ─── JSON yordamchi funksiyalar ───────────────────────────────────────────

    /** result.data yoki data ni JSONObject ichidan olish */
    fun extractData(json: JSONObject): Any? = try {
        val result = json.optJSONObject("result")
        if (result != null) result.opt("data") else json.opt("data")
    } catch (_: Exception) { null }

    /** org.json turlari → Kotlin/Map/List turlari */
    fun jsonToAny(o: Any?): Any? = when {
        o == null || o == JSONObject.NULL -> null
        o is JSONObject -> buildMap<String, Any?> {
            o.keys().forEach { k -> put(k, jsonToAny(o.opt(k))) }
        }
        o is JSONArray -> buildList {
            for (i in 0 until o.length()) add(jsonToAny(o.opt(i)))
        }
        else -> o  // String, Boolean, Int, Long, Double
    }

    // ─── API chaqiruvlar ──────────────────────────────────────────────────────

    suspend fun getDashboard() = rawCall { api.getDashboard() }

    suspend fun getIngredients(
        search: String? = null,
        category: String? = null,
        status: String? = null
    ) = rawCall { api.getIngredients(search = search, category = category, status = status) }

    suspend fun createIngredient(request: IngredientRequest) =
        rawCall { api.createIngredient(request) }

    suspend fun updateIngredient(id: String, request: IngredientRequest) =
        rawCall { api.updateIngredient(id, request) }

    suspend fun deleteIngredient(id: String) = rawCall { api.deleteIngredient(id) }

    suspend fun getRecipes(search: String? = null, isActive: Boolean? = null) =
        rawCall { api.getRecipes(search = search, isActive = isActive) }

    suspend fun createRecipe(request: RecipeRequest) = rawCall { api.createRecipe(request) }

    suspend fun deleteRecipe(id: String) = rawCall { api.deleteRecipe(id) }

    suspend fun getMenuCalendar(from: String? = null, to: String? = null) =
        rawCall { api.getMenuCalendar(from = from, to = to) }

    suspend fun createMenuEntry(request: MenuEntryRequest) =
        rawCall { api.createMenuEntry(request) }

    suspend fun confirmMenuEntry(id: String) = rawCall { api.confirmMenuEntry(id) }

    suspend fun deleteMenuEntry(id: String) = rawCall { api.deleteMenuEntry(id) }

    suspend fun getMovements(
        from: String? = null,
        to: String? = null,
        movementType: String? = null
    ) = rawCall { api.getMovements(from = from, to = to, movementType = movementType) }
}
