package com.aurelioklv.dicodingstoryapp.di

import android.content.Context
import com.aurelioklv.dicodingstoryapp.data.remote.api.ApiConfig
import com.aurelioklv.dicodingstoryapp.data.repository.StoryRepository
import com.aurelioklv.dicodingstoryapp.utils.UserPreferences
import com.aurelioklv.dicodingstoryapp.utils.dataStore

object Injection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val userPreferences = UserPreferences.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return StoryRepository.getInstance(apiService, userPreferences)
    }
}