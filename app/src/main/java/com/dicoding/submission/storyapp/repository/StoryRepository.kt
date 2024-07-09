package com.dicoding.submission.storyapp.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dicoding.submission.storyapp.helper.UserPreference
import com.dicoding.submission.storyapp.model.*
import com.dicoding.submission.storyapp.network.ApiConfig
import com.dicoding.submission.storyapp.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.IOException

open class StoryRepository(private val userPreference: UserPreference) {

    suspend fun getApiServiceWithToken(): ApiService {
        val token = userPreference.getUserToken().first()
        if (token.isNullOrBlank()) {
            throw IllegalStateException("Token is null or blank")
        }
        return ApiConfig.getApiService(token)
    }

    private fun getApiServiceWithoutToken(): ApiService = ApiConfig.getApiService()

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = getApiServiceWithoutToken().login(email, password)
            response.loginResult?.token?.let {
                userPreference.saveUserToken(it)
            }
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.message(), e.code())
        } catch (e: IOException) {
            Result.Error(ERROR_MESSAGE_NETWORK)
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<NewResponse> {
        return try {
            val response = getApiServiceWithoutToken().register(name, email, password)
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.message(), e.code())
        } catch (e: IOException) {
            Result.Error(ERROR_MESSAGE_NETWORK)
        }
    }

    suspend fun getDetailStory(id: String): Result<DetailStoryResponse> {
        return try {
            val response = getApiServiceWithToken().getDetailStory(id)
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.message(), e.code())
        } catch (e: IOException) {
            Result.Error(ERROR_MESSAGE_NETWORK)
        }
    }

    suspend fun addStory(file: MultipartBody.Part, description: RequestBody): Result<NewResponse> {
        return try {
            val response = getApiServiceWithToken().addStory(file, description)
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.message(), e.code())
        } catch (e: IOException) {
            Result.Error(ERROR_MESSAGE_NETWORK)
        }
    }

    suspend fun saveUserToken(token: String) {
        userPreference.saveUserToken(token)
    }

    suspend fun clearUserToken() {
        userPreference.clearUserToken()
    }

    suspend fun getUserToken(): String? = userPreference.getUserToken().first()

    fun getStoriesPaged(location: Int): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(this, location) }
        ).flow
    }

    suspend fun getStories(page: Int? = null, size: Int? = null, location: Int? = 0): Result<StoryResponse> {
        return try {
            val response = getApiServiceWithToken().getStories(page, size, location)
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.message(), e.code())
        } catch (e: IOException) {
            Result.Error(ERROR_MESSAGE_NETWORK)
        }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(userPreference: UserPreference): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(userPreference).also { instance = it }
            }

        private const val ERROR_MESSAGE_NETWORK = "Terjadi kesalahan jaringan, silakan coba lagi nanti."
    }
}
