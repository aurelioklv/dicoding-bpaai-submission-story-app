package com.aurelioklv.dicodingstoryapp.presentation.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurelioklv.dicodingstoryapp.data.Result
import com.aurelioklv.dicodingstoryapp.data.remote.api.ErrorResponse
import com.aurelioklv.dicodingstoryapp.data.remote.api.StoryItem
import com.aurelioklv.dicodingstoryapp.data.repository.StoryRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DetailsViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _story = MutableLiveData<Result<StoryItem>>()
    val story: LiveData<Result<StoryItem>> = _story

    fun getDetails(id: String) {
        viewModelScope.launch {
            try {
                _story.value = Result.Loading
                val response = repository.getDetails(id)
                if (!response.error) {
                    if (response.story != null) {
                        _story.value = Result.Success(response.story)
                    }
                }
            } catch (e: HttpException) {
                val jsonString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonString, ErrorResponse::class.java)
                _story.value = Result.Error(errorBody.message ?: e.message())
            }
        }
    }
}