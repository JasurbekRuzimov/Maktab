package com.maktab.app.network

import com.maktab.app.network.models.*

// ─────────────────────────────────────────────
// Sessiyada saqlanadigan ma'lumotlar
// ─────────────────────────────────────────────

data class SessionInfo(
    val userId: String,
    val fullname: String,
    val username: String,
    val role: String,
    val branchId: String,
    val branchName: String,   // "Shahar", "SHAHAR" emas — insoniy o'qiladigan nom
    val accessToken: String,
    val refreshToken: String,
    val renewEndpoint: String,
    val refreshEndpoint: String
)

class AuthRepository {

    private val authService = RetrofitClient.authService

    // ─────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────
    suspend fun login(username: String, password: String): ApiResult<SessionInfo> {
        val result = safeApiCall {
            authService.login(LoginRequest(username, password))
        }

        return when (result) {
            is ApiResult.Success -> {
                val data = result.data.result?.data

                val tokens  = data?.tokens
                val user    = data?.user
                val session = data?.session

                val accessToken = tokens?.accessToken
                if (accessToken.isNullOrEmpty()) {
                    return ApiResult.Error("Token olinmadi. Login ma'lumotlarini tekshiring.")
                }

                // Role aniqlash — session.role_code eng ishonchli
                val role = session?.roleCode
                    ?: user?.role?.code
                    ?: return ApiResult.Error("Rol aniqlanmadi")

                // Filial nomi — branches listdan, code emas name ishlatamiz
                val branchName = user?.branches
                    ?.firstOrNull { it.id == user.branchId }
                    ?.name
                    ?: user?.branches?.firstOrNull()?.name
                    ?: ""

                RetrofitClient.accessToken = accessToken

                ApiResult.Success(
                    SessionInfo(
                        userId        = user?.id ?: "",
                        fullname      = user?.fullname ?: username,
                        username      = user?.username ?: username,
                        role          = role,
                        branchId      = user?.branchId ?: "",
                        branchName    = branchName,
                        accessToken   = accessToken,
                        refreshToken  = tokens.refreshToken ?: "",
                        renewEndpoint = tokens.renewEndpoint ?: "/api/users/renew",
                        refreshEndpoint = tokens.refreshEndpoint ?: "/api/users/refresh"
                    )
                )
            }
            is ApiResult.Error -> result
            ApiResult.Loading  -> ApiResult.Loading
        }
    }

    // ─────────────────────────────────────────
    // TOKEN YANGILASH
    // Teacher uchun: /api/teacher/users/renew
    // Chef uchun:    /api/users/renew
    // ─────────────────────────────────────────
    suspend fun renewToken(refreshToken: String, renewEndpoint: String): ApiResult<TokenData> {
        val result = safeApiCall {
            authService.renewToken(
                url = RetrofitClient.buildUrl(renewEndpoint),
                request = RefreshRequest(refreshToken)
            )
        }
        return when (result) {
            is ApiResult.Success -> {
                val tokens = result.data.result?.data?.tokens
                if (tokens?.accessToken != null) {
                    RetrofitClient.accessToken = tokens.accessToken
                    ApiResult.Success(tokens)
                } else {
                    ApiResult.Error("Token yangilanmadi")
                }
            }
            is ApiResult.Error -> result
            ApiResult.Loading  -> ApiResult.Loading
        }
    }

    // ─────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────
    suspend fun logout(): ApiResult<Unit> {
        val result = safeApiCall { authService.logout() }
        RetrofitClient.accessToken = ""
        return result
    }
}