package com.aurelioklv.dicodingstoryapp.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.aurelioklv.dicodingstoryapp.data.remote.api.StoryItem
import com.aurelioklv.dicodingstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: StoryRepository) : ViewModel() {
    val stories: LiveData<PagingData<StoryItem>> =
        repository.getAllStories().cachedIn(viewModelScope)

    fun getName(): LiveData<String?> {
        return repository.getName().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}