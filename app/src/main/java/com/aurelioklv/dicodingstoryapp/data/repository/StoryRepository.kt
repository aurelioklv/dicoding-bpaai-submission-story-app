package com.aurelioklv.dicodingstoryapp.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.aurelioklv.dicodingstoryapp.data.local.StoryRemoteMediator
import com.aurelioklv.dicodingstoryapp.data.local.UserPreferences
import com.aurelioklv.dicodingstoryapp.data.local.db.StoryDatabase
import com.aurelioklv.dicodingstoryapp.data.remote.api.ApiService
import com.aurelioklv.dicodingstoryapp.data.remote.api.BasicResponse
import com.aurelioklv.dicodingstoryapp.data.remote.api.DetailsResponse
import com.aurelioklv.dicodingstoryapp.data.remote.api.StoriesResponse
import com.aurelioklv.dicodingstoryapp.data.remote.api.StoryItem
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository private constructor(
    private val database: StoryDatabase,
    private val apiService: ApiService,
    private val userPreferences: UserPreferences,
) {
    fun getToken() = userPreferences.getToken()
    fun getName() = userPreferences.getName()
    suspend fun saveSession(token: String, name: String) = userPreferences.saveSession(token, name)

    suspend fun register(name: String, email: String, password: String) =
        apiService.register(name, email, password)

    suspend fun login(email: String, password: String) = apiService.login(email, password)
    suspend fun logout() = userPreferences.clearSession()

    suspend fun getAllStories(location: Int = 0): StoriesResponse {
        return apiService.getAllStories(location = location)
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getAllStories(): LiveData<PagingData<StoryItem>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { database.storyDao().getAllStory() },
            remoteMediator = StoryRemoteMediator(database, apiService)
        ).liveData
    }

    suspend fun getDetails(id: String): DetailsResponse {
        return apiService.getDetails(id)
    }

    suspend fun addStory(
        multipartBody: MultipartBody.Part,
        descriptionRequestBody: RequestBody,
    ): BasicResponse {
        return apiService.addStory(multipartBody, descriptionRequestBody)
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(
            database: StoryDatabase,
            apiService: ApiService,
            userPreferences: UserPreferences
        ): StoryRepository {
            return instance ?: synchronized(this) {
                instance ?: StoryRepository(database, apiService, userPreferences)
            }.also { instance = it }
        }
    }
}