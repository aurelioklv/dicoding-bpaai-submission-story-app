package com.aurelioklv.dicodingstoryapp.data.repository

import com.aurelioklv.dicodingstoryapp.data.local.UserPreferences
import com.aurelioklv.dicodingstoryapp.data.remote.api.ApiService
import com.aurelioklv.dicodingstoryapp.data.remote.api.DetailsResponse
import com.aurelioklv.dicodingstoryapp.data.remote.api.StoriesResponse

class StoryRepository private constructor(
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

    suspend fun getAllStories(): StoriesResponse {
        return apiService.getAllStories()
    }

    suspend fun getDetails(id: String): DetailsResponse {
        return apiService.getDetails(id)
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService, userPreferences: UserPreferences): StoryRepository {
            return instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, userPreferences)
            }.also { instance = it }
        }
    }
}

