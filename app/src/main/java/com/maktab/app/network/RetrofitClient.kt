package com.maktab.app.network

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://147.45.213.148:3000/"

    private val moshi = Moshi.Builder()
        .add(AnyJsonAdapterFactory())   // Any? tipidagi maydonlarni to'g'ri parse qilish uchun
        .add(KotlinJsonAdapterFactory())
        .build()

    // Token ni tashqaridan set qilish uchun
    var accessToken: String = ""

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val request = if (accessToken.isNotEmpty()) {
            original.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()
        } else {
            original.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()
        }
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)   // Debug uchun — release da olib tashlanadi
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // Nisbiy endpoint dan to'liq URL yasash
    // Masalan: "/api/teacher/users/renew" → "http://147.45.213.148:3000/api/teacher/users/renew"
    fun buildUrl(endpoint: String): String {
        val base = BASE_URL.trimEnd('/')
        val path = endpoint.trimStart('/')
        return "$base/$path"
    }

    // Har bir rol uchun service instance lar
    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val teacherService: TeacherApiService by lazy {
        retrofit.create(TeacherApiService::class.java)
    }

    val parentService: ParentApiService by lazy {
        retrofit.create(ParentApiService::class.java)
    }

    val studentService: StudentApiService by lazy {
        retrofit.create(StudentApiService::class.java)
    }

    val chefService: ChefApiService by lazy {
        retrofit.create(ChefApiService::class.java)
    }

    val hrService: HRApiService by lazy {
        retrofit.create(HRApiService::class.java)
    }
}