package com.aurelioklv.dicodingstoryapp.presentation.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aurelioklv.dicodingstoryapp.data.Result
import com.aurelioklv.dicodingstoryapp.data.remote.api.ErrorResponse
import com.aurelioklv.dicodingstoryapp.data.remote.api.StoryItem
import com.aurelioklv.dicodingstoryapp.data.repository.StoryRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class HomeViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _stories = MutableLiveData<Result<List<StoryItem?>>>()
    val stories: LiveData<Result<List<StoryItem?>>> = _stories

    init {
        getAllStories()
    }

    fun getAllStories() {
        viewModelScope.launch {
            try {
                _stories.value = Result.Loading
                val response = repository.getAllStories()
                if (!response.error) {
                    if (response.listStory!!.isNotEmpty()) {
                        _stories.value = Result.Success(response.listStory)
                    }
                }
            } catch (e: HttpException) {
                val jsonString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonString, ErrorResponse::class.java)
                Log.e(TAG, errorBody.message.toString())
                _stories.value = Result.Error(errorBody.message ?: e.message())
            }
        }
    }

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