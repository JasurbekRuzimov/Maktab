package com.maktab.app.network

import com.maktab.app.network.models.*
import retrofit2.Response
import retrofit2.http.*

// ─────────────────────────────────────────────
// AUTH
// POST /api/users/login      → token + role
// POST /api/users/refresh    → yangi access token
// POST /api/users/logout     → sessiyani yopish
// GET  /api/profile          → joriy foydalanuvchi ma'lumoti
// ─────────────────────────────────────────────
interface AuthApiService {

    @POST("api/users/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // Dinamik URL — teacher: /api/teacher/users/renew, chef: /api/users/renew
    @POST
    suspend fun renewToken(
        @Url url: String,
        @Body request: RefreshRequest
    ): Response<RefreshResponse>

    @POST("api/users/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PUT("api/profile/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<BaseResponse>
}

// ─────────────────────────────────────────────
// TEACHER — barcha endpoint lar api/teacher/ prefiksi bilan
// Login javobida: route_group = "teacher", namespace = "/teacher"
// ─────────────────────────────────────────────
interface TeacherApiService {

    // Dars jadvali — haftalik (scheduled-lessons/board)
    @GET("api/teacher/scheduled-lessons/board")
    suspend fun getLessonsBoard(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("class_id") classId: String? = null,
        @Query("subject_id") subjectId: String? = null
    ): Response<BaseResponse>

    // Rejalashtirilgan darslar ro'yxati
    @GET("api/teacher/scheduled-lessons")
    suspend fun getScheduledLessons(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<BaseResponse>

    // Jurnal — sinf+fan+chorak bo'yicha
    @GET("api/teacher/journal")
    suspend fun getJournal(
        @Query("class_id") classId: String,
        @Query("quarter_id") quarterId: String,
        @Query("subject_id") subjectId: String,
        @Query("week_start") weekStart: String? = null
    ): Response<BaseResponse>

    // Jurnal variantlari (sinflar, fanlar, choraklar)
    @GET("api/teacher/journal/options")
    suspend fun getJournalOptions(): Response<BaseResponse>

    // Jurnal katakchasini yangilash (baho + davomat)
    @PUT("api/teacher/journal/cells")
    suspend fun updateJournalCell(
        @Body request: JournalCellRequest
    ): Response<BaseResponse>

    // Davomat — darslar bo'yicha
    @GET("api/teacher/attendance/lessons")
    suspend fun getAttendanceLessons(
        @Query("class_id") classId: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<BaseResponse>

    // Davomat yozuvlari
    @GET("api/teacher/attendance-records")
    suspend fun getAttendanceRecords(
        @Query("class_id") classId: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<BaseResponse>

    // Sinflar ro'yxati
    @GET("api/teacher/classes")
    suspend fun getClasses(): Response<BaseResponse>

    // Uy vazifalari
    @GET("api/teacher/homework")
    suspend fun getHomework(
        @Query("class_id") classId: String? = null,
        @Query("subject_id") subjectId: String? = null,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<BaseResponse>

    // Uy vazifasini yangilash
    @PATCH("api/teacher/homework/{homework_id}")
    suspend fun updateHomework(
        @Path("homework_id") homeworkId: String,
        @Body request: HomeworkUpdateRequest
    ): Response<BaseResponse>

    // Xulq yozuvlari
    @GET("api/teacher/behavior-cases")
    suspend fun getBehaviorCases(
        @Query("class_id") classId: String? = null,
        @Query("pupil_id") pupilId: String? = null
    ): Response<BaseResponse>

    // Baholar
    @GET("api/teacher/grade-records")
    suspend fun getGradeRecords(
        @Query("student_id") studentId: String? = null,
        @Query("scheduled_lesson_id") lessonId: String? = null
    ): Response<BaseResponse>

    // O'quv yillari va choraklar
    @GET("api/teacher/academic-quarters")
    suspend fun getAcademicQuarters(
        @Query("academic_year_id") academicYearId: String? = null
    ): Response<BaseResponse>

    // Fanlar
    @GET("api/teacher/subjects")
    suspend fun getSubjects(): Response<BaseResponse>
}

// ─────────────────────────────────────────────
// PARENT
// GET /api/parent/dashboard        → bosh sahifa statistika
// GET /api/parent/children/{id}    → farzand ma'lumoti
// GET /api/attendance-records      → farzand davomati
// GET /api/grade-records           → farzand baholari
// GET /api/homework                → uyga vazifalar
// GET /api/internal/surveys/available → so'rovnomalar
// ─────────────────────────────────────────────
interface ParentApiService {

    // Ota-ona dashboard
    @GET("api/parent/dashboard")
    suspend fun getDashboard(): Response<BaseResponse>

    // Farzand ma'lumoti
    @GET("api/parent/children/{id}")
    suspend fun getChild(
        @Path("id") childId: String
    ): Response<BaseResponse>

    // Farzand davomati
    @GET("api/attendance-records")
    suspend fun getAttendanceRecords(
        @Query("student_id") studentId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<BaseResponse>

    // Farzand baholari
    @GET("api/grade-records")
    suspend fun getGradeRecords(
        @Query("student_id") studentId: String
    ): Response<BaseResponse>

    // Uyga vazifalar
    @GET("api/homework")
    suspend fun getHomework(
        @Query("from_date") fromDate: String? = null
    ): Response<BaseResponse>

    // Vazifa topshirish
    @POST("api/homework/{homework_id}/submissions")
    suspend fun submitHomework(
        @Path("homework_id") homeworkId: String,
        @Body request: HomeworkSubmitRequest
    ): Response<BaseResponse>

    // So'rovnomalar
    @GET("api/internal/surveys/available")
    suspend fun getSurveys(
        @Query("status") status: String? = null
    ): Response<BaseResponse>

    // So'rovnomaga javob berish
    @POST("api/internal/surveys/{id}/submit")
    suspend fun submitSurvey(
        @Path("id") surveyId: String,
        @Body request: SurveySubmitRequest
    ): Response<BaseResponse>

    // Xabarnomalar
    @GET("api/notifications")
    suspend fun getNotifications(): Response<BaseResponse>
}

// ─────────────────────────────────────────────
// STUDENT
// GET /api/portal/dashboard        → o'quvchi dashboard
// GET /api/portal/lessons/board    → dars jadvali
// GET /api/attendance-records      → o'z davomati
// GET /api/grade-records           → o'z baholari
// GET /api/portal/homework/lessons → uyga vazifalar
// ─────────────────────────────────────────────
interface StudentApiService {

    // Student dashboard
    @GET("api/portal/dashboard")
    suspend fun getDashboard(): Response<BaseResponse>

    // Dars jadvali
    @GET("api/portal/lessons/board")
    suspend fun getLessonsBoard(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<BaseResponse>

    // Davomat
    @GET("api/attendance-records")
    suspend fun getAttendanceRecords(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<BaseResponse>

    // Baholar
    @GET("api/grade-records")
    suspend fun getGradeRecords(): Response<BaseResponse>

    // Uyga vazifalar
    @GET("api/portal/homework/lessons")
    suspend fun getHomework(): Response<BaseResponse>

    // Vazifa topshirish
    @POST("api/homework/{homework_id}/submissions")
    suspend fun submitHomework(
        @Path("homework_id") homeworkId: String,
        @Body request: HomeworkSubmitRequest
    ): Response<BaseResponse>

    // Imtihonlar
    @GET("api/grade-records")
    suspend fun getExamResults(
        @Query("source_type") sourceType: String = "exam"
    ): Response<BaseResponse>

    // So'rovnomalar
    @GET("api/internal/surveys/available")
    suspend fun getSurveys(): Response<BaseResponse>

    // So'rovnomaga javob
    @POST("api/internal/surveys/{id}/submit")
    suspend fun submitSurvey(
        @Path("id") surveyId: String,
        @Body request: SurveySubmitRequest
    ): Response<BaseResponse>

    // Xabarnomalar
    @GET("api/notifications")
    suspend fun getNotifications(): Response<BaseResponse>
}

// ─────────────────────────────────────────────
// CHEF (CAFETERIA)
// GET  /api/cafeteria/dashboard       → bosh panel
// GET  /api/cafeteria/ingredients     → ingredientlar
// POST /api/cafeteria/ingredients     → yangi ingredient
// PUT  /api/cafeteria/ingredients/{id}→ ingredient tahrirlash
// GET  /api/cafeteria/recipes         → retseptlar
// GET  /api/cafeteria/calendar        → menyu kalendari
// POST /api/cafeteria/calendar        → yangi menyu yozuvi
// GET  /api/cafeteria/movements       → stock harakatlari
// ─────────────────────────────────────────────
interface ChefApiService {

    // Kafeteriya dashboard
    @GET("api/cafeteria/dashboard")
    suspend fun getDashboard(): Response<BaseResponse>

    // Tasdiqlanishi kerak bo'lganlar
    @GET("api/cafeteria/confirmations/pending")
    suspend fun getPendingConfirmations(): Response<BaseResponse>

    // Ingredientlar ro'yxati
    @GET("api/cafeteria/ingredients")
    suspend fun getIngredients(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<BaseResponse>

    // Ingredient yaratish
    @POST("api/cafeteria/ingredients")
    suspend fun createIngredient(
        @Body request: IngredientRequest
    ): Response<BaseResponse>

    // Ingredient tahrirlash
    @PUT("api/cafeteria/ingredients/{id}")
    suspend fun updateIngredient(
        @Path("id") id: String,
        @Body request: IngredientRequest
    ): Response<BaseResponse>

    // Ingredient o'chirish
    @DELETE("api/cafeteria/ingredients/{id}")
    suspend fun deleteIngredient(
        @Path("id") id: String
    ): Response<BaseResponse>

    // Retseptlar
    @GET("api/cafeteria/recipes")
    suspend fun getRecipes(
        @Query("search") search: String? = null,
        @Query("is_active") isActive: Boolean? = null
    ): Response<BaseResponse>

    // Retsept yaratish
    @POST("api/cafeteria/recipes")
    suspend fun createRecipe(
        @Body request: RecipeRequest
    ): Response<BaseResponse>

    // Retsept o'chirish
    @DELETE("api/cafeteria/recipes/{id}")
    suspend fun deleteRecipe(
        @Path("id") id: String
    ): Response<BaseResponse>

    // Menyu kalendari
    @GET("api/cafeteria/calendar")
    suspend fun getMenuCalendar(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("meal_type") mealType: String? = null
    ): Response<BaseResponse>

    // Menyu yozuvi yaratish
    @POST("api/cafeteria/calendar")
    suspend fun createMenuEntry(
        @Body request: MenuEntryRequest
    ): Response<BaseResponse>

    // Menyuni tasdiqlash
    @POST("api/cafeteria/calendar/{id}/confirm")
    suspend fun confirmMenuEntry(
        @Path("id") id: String
    ): Response<BaseResponse>

    // Menyu yozuvini o'chirish
    @DELETE("api/cafeteria/calendar/{id}")
    suspend fun deleteMenuEntry(
        @Path("id") id: String
    ): Response<BaseResponse>

    // Stock harakatlari (kirim/chiqim)
    @GET("api/cafeteria/movements")
    suspend fun getMovements(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("movement_type") movementType: String? = null,
        @Query("ingredient_id") ingredientId: String? = null
    ): Response<BaseResponse>
}

// ─────────────────────────────────────────────
// HR
// GET /api/users/employees    → xodimlar ro'yxati
// POST /api/users/employees   → yangi xodim
// PUT /api/users/employees/{id} → xodimni tahrirlash
// GET /api/attendance-records → xodimlar davomati
// GET /api/hr-workflows       → ta'til/ruxsat so'rovlar
// PUT /api/hr-workflows/{id}  → so'rovni tasdiqlash/rad etish
// ─────────────────────────────────────────────
interface HRApiService {

    // Xodimlar ro'yxati
    @GET("api/users/employees")
    suspend fun getEmployees(
        @Query("search") search: String? = null,
        @Query("role_code") roleCode: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<BaseResponse>

    // Xodim ma'lumoti
    @GET("api/users/employees/{id}")
    suspend fun getEmployee(
        @Path("id") id: String
    ): Response<BaseResponse>

    // Yangi xodim yaratish
    @POST("api/users/employees")
    suspend fun createEmployee(
        @Body request: EmployeeRequest
    ): Response<BaseResponse>

    // Xodimni yangilash
    @PUT("api/users/employees/{id}")
    suspend fun updateEmployee(
        @Path("id") id: String,
        @Body request: EmployeeRequest
    ): Response<BaseResponse>

    // Xodimni o'chirish
    @DELETE("api/users/employees/{id}")
    suspend fun deleteEmployee(
        @Path("id") id: String
    ): Response<BaseResponse>

    // Xodimlar davomati
    @GET("api/attendance-records")
    suspend fun getAttendanceRecords(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("teacher_id") teacherId: String? = null
    ): Response<BaseResponse>

    // Ta'til/ruxsat so'rovlar
    @GET("api/hr-workflows")
    suspend fun getLeaveRequests(
        @Query("status") status: String? = null,
        @Query("employee_id") employeeId: String? = null
    ): Response<BaseResponse>

    // Ta'til so'rovini yaratish
    @POST("api/hr-workflows")
    suspend fun createLeaveRequest(
        @Body request: LeaveRequest
    ): Response<BaseResponse>

    // So'rovni tasdiqlash/rad etish
    @PUT("api/hr-workflows/{id}")
    suspend fun updateLeaveRequest(
        @Path("id") id: String,
        @Body request: LeaveUpdateRequest
    ): Response<BaseResponse>

    // Lavozimlar ro'yxati
    @GET("api/public/users/employees")
    suspend fun getPositions(
        @Query("role_code") roleCode: String? = null
    ): Response<BaseResponse>

    // HR statistika
    @GET("api/users/employees/stats")
    suspend fun getEmployeeStats(): Response<BaseResponse>
}