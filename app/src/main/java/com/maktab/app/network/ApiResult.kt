package com.maktab.app.network

// ─────────────────────────────────────────────
// API natija wrapper — Loading / Success / Error
// ─────────────────────────────────────────────

sealed class ApiResult<out T> {
    object Loading : ApiResult<Nothing>()
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

// Retrofit Response ni ApiResult ga aylantirish
suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Bo'sh javob", response.code())
            }
        } else {
            val errorMsg = when (response.code()) {
                401  -> "Avtorizatsiya talab etiladi. Qayta kiring."
                403  -> "Ruxsat yo'q."
                404  -> "Ma'lumot topilmadi."
                422  -> "Noto'g'ri ma'lumot kiritildi."
                500  -> "Server xatosi. Keyinroq urinib ko'ring."
                else -> "Xato: ${response.code()}"
            }
            ApiResult.Error(errorMsg, response.code())
        }
    } catch (e: java.net.UnknownHostException) {
        ApiResult.Error("Internet aloqasi yo'q")
    } catch (e: java.net.SocketTimeoutException) {
        ApiResult.Error("So'rov vaqti tugadi. Qayta urinib ko'ring.")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Noma'lum xato")
    }
}