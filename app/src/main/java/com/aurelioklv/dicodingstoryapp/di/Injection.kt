package com.aurelioklv.dicodingstoryapp.di

import android.content.Context
import com.aurelioklv.dicodingstoryapp.data.local.UserPreferences
import com.aurelioklv.dicodingstoryapp.data.local.dataStore
import com.aurelioklv.dicodingstoryapp.data.remote.api.ApiConfig
import com.aurelioklv.dicodingstoryapp.data.repository.StoryRepository

object Injection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val userPreferences = UserPreferences.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(userPreferences)
        return StoryRepository.getInstance(apiService, userPreferences)
    }
}