package com.rhezarijaya.stories.service

import com.rhezarijaya.stories.model.CreateStoryResponse
import com.rhezarijaya.stories.model.LoginResponse
import com.rhezarijaya.stories.model.RegisterResponse
import com.rhezarijaya.stories.model.StoryResponse
import com.rhezarijaya.stories.util.Constants
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.text.SimpleDateFormat
import java.util.*

const val UNEXPECTED_ERROR = "Unexpected Error"
const val UNEXPECTED_DATA_ERROR = "Unexpected Data Error"

interface APIService {
    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @Multipart
    @POST("stories")
    fun postStory(
        @Header("Authorization") authorization: String,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?
    ): Call<CreateStoryResponse>

    @GET("stories")
    fun getStories(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int?,
        @Query("size") size: Int?,
        @Query("location") location: Int?
    ): Call<StoryResponse>
}

fun formatBearerToken(token: String): String {
    return "Bearer $token"
}

fun getAPIService(): APIService {
    return Retrofit.Builder()
        .baseUrl(Constants.API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .build()
        )
        .build()
        .create(APIService::class.java)
}

fun formatResponseCode(code: Int): String {
    return if (code >= 500) {
        "Server Error"
    } else if (code >= 400) {
        "Client Error"
    } else {
        UNEXPECTED_ERROR
    }
}

fun formatCreatedAt(dateString: String): String {
    var result = ""

    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("GMT")

        val date = sdf.parse(dateString)

        result =
            date.let {
                if (it != null) {
                    SimpleDateFormat("yyyy-mm-dd HH:mm:ss", Locale.getDefault()).format(it)
                }
            }
                .toString()
    } catch (e: Exception) {
    }

    return result
}