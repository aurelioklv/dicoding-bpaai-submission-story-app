package com.aurelioklv.dicodingstoryapp.di

import android.content.Context
import com.aurelioklv.dicodingstoryapp.data.local.UserPreferences
import com.aurelioklv.dicodingstoryapp.data.local.dataStore
import com.aurelioklv.dicodingstoryapp.data.remote.api.ApiConfig
import com.aurelioklv.dicodingstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val userPreferences = UserPreferences.getInstance(context.dataStore)
        val token = runBlocking { userPreferences.getToken().first() }
        val apiService = ApiConfig.getApiService(token.toString())
        return StoryRepository.getInstance(apiService, userPreferences)
    }
}