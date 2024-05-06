package com.aurelioklv.dicodingstoryapp.presentation.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aurelioklv.dicodingstoryapp.data.repository.StoryRepository
import com.aurelioklv.dicodingstoryapp.di.Injection
import com.aurelioklv.dicodingstoryapp.presentation.auth.login.LoginViewModel
import com.aurelioklv.dicodingstoryapp.presentation.auth.register.RegisterViewModel
import com.aurelioklv.dicodingstoryapp.presentation.details.DetailsViewModel
import com.aurelioklv.dicodingstoryapp.presentation.home.HomeViewModel

class ViewModelFactory private constructor(
    private val repository: StoryRepository,
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            return DetailsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            return instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    Injection.provideStoryRepository(context)
                )
            }.also { instance = it }
        }
    }
}