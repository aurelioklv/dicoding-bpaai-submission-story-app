package com.aurelioklv.dicodingstoryapp.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aurelioklv.dicodingstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: StoryRepository) : ViewModel() {
    fun getName(): LiveData<String?> {
        return repository.getName().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}