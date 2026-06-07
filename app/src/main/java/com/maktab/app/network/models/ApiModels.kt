package com.maktab.app.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─────────────────────────────────────────────
// UMUMIY
// ─────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class BaseResponse(
    @Json(name = "result") val result: ResultWrapper? = null,
    @Json(name = "errors") val errors: Any? = null
)

@JsonClass(generateAdapter = true)
data class ResultWrapper(
    @Json(name = "data") val data: Any? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "meta") val meta: MetaData? = null
)

@JsonClass(generateAdapter = true)
data class MetaData(
    @Json(name = "total") val total: Int? = null,
    @Json(name = "per_page") val perPage: Int? = null,
    @Json(name = "current_page") val currentPage: Int? = null,
    @Json(name = "last_page") val lastPage: Int? = null
)

// ─────────────────────────────────────────────
// AUTH — LOGIN
// POST /api/users/login
// Javob: { result: { data: { user, session, tokens } } }
// ─────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String,
    @Json(name = "device_name") val deviceName: String = "android"
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "result") val result: LoginResult? = null,
    @Json(name = "errors") val errors: Any? = null
)

@JsonClass(generateAdapter = true)
data class LoginResult(
    @Json(name = "data") val data: LoginData? = null
)

@JsonClass(generateAdapter = true)
data class LoginData(
    @Json(name = "user") val user: UserData? = null,
    @Json(name = "session") val session: SessionData? = null,
    @Json(name = "tokens") val tokens: TokenData? = null
)

@JsonClass(generateAdapter = true)
data class UserData(
    @Json(name = "id") val id: String? = null,
    @Json(name = "fullname") val fullname: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "employee_id") val employeeId: String? = null,
    @Json(name = "teacher_id") val teacherId: String? = null,
    @Json(name = "parent_id") val parentId: String? = null,
    @Json(name = "student_id") val studentId: String? = null,
    @Json(name = "branch_id") val branchId: String? = null,
    @Json(name = "app") val app: String? = null,
    @Json(name = "role") val role: RoleData? = null,
    @Json(name = "branches") val branches: List<BranchData>? = null
)

@JsonClass(generateAdapter = true)
data class BranchData(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "code") val code: String? = null
)

@JsonClass(generateAdapter = true)
data class RoleData(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "code") val code: String? = null,
    @Json(name = "app") val app: String? = null
)

@JsonClass(generateAdapter = true)
data class SessionData(
    @Json(name = "id") val id: String? = null,
    @Json(name = "role_code") val roleCode: String? = null,
    @Json(name = "route_group") val routeGroup: String? = null,
    @Json(name = "namespace") val namespace: String? = null,
    @Json(name = "branch_id") val branchId: String? = null,
    @Json(name = "access_expires_at") val accessExpiresAt: String? = null,
    @Json(name = "refresh_expires_at") val refreshExpiresAt: String? = null
)

@JsonClass(generateAdapter = true)
data class TokenData(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    @Json(name = "app") val app: String? = null,
    @Json(name = "route_group") val routeGroup: String? = null,
    @Json(name = "access_token_expires_in") val accessTokenExpiresIn: Int? = null,
    @Json(name = "refresh_token_expires_in") val refreshTokenExpiresIn: Int? = null,
    @Json(name = "renew_endpoint") val renewEndpoint: String? = null,
    @Json(name = "refresh_endpoint") val refreshEndpoint: String? = null,
    @Json(name = "auto_renew_after_seconds") val autoRenewAfterSeconds: Int? = null
)

// ─────────────────────────────────────────────
// AUTH — REFRESH / RENEW
// Teacher:  POST /api/teacher/users/renew
// Chef:     POST /api/users/renew
// ─────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class RefreshRequest(
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "device_name") val deviceName: String = "android"
)

@JsonClass(generateAdapter = true)
data class RefreshResponse(
    @Json(name = "result") val result: LoginResult? = null,
    @Json(name = "errors") val errors: Any? = null
)

// ─────────────────────────────────────────────
// PROFILE
// ─────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class ProfileResponse(
    @Json(name = "result") val result: ProfileResult? = null,
    @Json(name = "errors") val errors: Any? = null
)

@JsonClass(generateAdapter = true)
data class ProfileResult(
    @Json(name = "data") val data: ProfileData? = null
)

@JsonClass(generateAdapter = true)
data class ProfileData(
    @Json(name = "id") val id: String? = null,
    @Json(name = "fullname") val fullname: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "role") val role: RoleData? = null
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password") val currentPassword: String,
    @Json(name = "password") val password: String,
    @Json(name = "password_confirmation") val passwordConfirmation: String
)

// ─────────────────────────────────────────────
// TEACHER
// ─────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class JournalCellRequest(
    @Json(name = "student_id") val studentId: String,
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "grade") val grade: Int? = null,
    @Json(name = "attendance") val attendance: String? = null,
    @Json(name = "homework") val homework: String? = null
)

@JsonClass(generateAdapter = true)
data class HomeworkUpdateRequest(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "due_date") val dueDate: String? = null
)

// ─────────────────────────────────────────────
// PARENT & STUDENT
// ─────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class HomeworkSubmitRequest(
    @Json(name = "comment") val comment: String? = null,
    @Json(name = "file_ids") val fileIds: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class SurveySubmitRequest(
    @Json(name = "answers") val answers: List<SurveyAnswer>
)

@JsonClass(generateAdapter = true)
data class SurveyAnswer(
    @Json(name = "question_id") val questionId: String,
    @Json(name = "answer") val answer: String? = null,
    @Json(name = "option_id") val optionId: String? = null
)

// ─────────────────────────────────────────────
// CHEF
// ─────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class StockMovementRequest(
    @Json(name = "movement_type") val movementType: String,  // "INCOMING" yoki "OUTGOING"
    @Json(name = "quantity") val quantity: Double,
    @Json(name = "reason") val reason: String
)

data class IngredientRequest(
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String? = null,
    @Json(name = "unit") val unit: String,
    @Json(name = "current_stock") val quantity: Double,         // FIX: quantity → current_stock
    @Json(name = "minimum_stock") val minQuantity: Double? = null, // FIX: min_quantity → minimum_stock
    @Json(name = "expiration_date") val expiryDate: String? = null // FIX: expiry_date → expiration_date
)

@JsonClass(generateAdapter = true)
data class RecipeRequest(
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String? = null,
    @Json(name = "serving_count") val portionCount: Int? = null, // FIX: portion_count → serving_count
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "ingredients") val ingredients: List<RecipeIngredient>? = null
)

@JsonClass(generateAdapter = true)
data class RecipeIngredient(
    @Json(name = "ingredient_id") val ingredientId: String,
    @Json(name = "quantity") val quantity: Double,
    @Json(name = "unit") val unit: String
)

@JsonClass(generateAdapter = true)
data class MenuEntryRequest(
    @Json(name = "recipe_id") val recipeId: String,
    @Json(name = "meal_type") val mealType: String,
    @Json(name = "date") val dateKey: String
)

// ─────────────────────────────────────────────
// HR
// ─────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class EmployeeRequest(
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "position") val position: String? = null,
    @Json(name = "department") val department: String? = null,
    @Json(name = "join_date") val joinDate: String? = null,
    @Json(name = "status") val status: String = "active"
)

@JsonClass(generateAdapter = true)
data class LeaveRequest(
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "type") val type: String,
    @Json(name = "from_date") val fromDate: String,
    @Json(name = "to_date") val toDate: String,
    @Json(name = "reason") val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class LeaveUpdateRequest(
    @Json(name = "status") val status: String
)