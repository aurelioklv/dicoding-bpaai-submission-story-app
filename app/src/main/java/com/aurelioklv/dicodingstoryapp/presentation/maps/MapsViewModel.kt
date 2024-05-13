package com.aurelioklv.dicodingstoryapp.presentation.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurelioklv.dicodingstoryapp.data.Result
import com.aurelioklv.dicodingstoryapp.data.remote.api.BasicResponse
import com.aurelioklv.dicodingstoryapp.data.remote.api.StoryItem
import com.aurelioklv.dicodingstoryapp.data.repository.StoryRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MapsViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _stories = MutableLiveData<Result<List<StoryItem>>>()
    val stories: LiveData<Result<List<StoryItem>>> = _stories

    init {
        getAllStoriesWithLocation()
    }

    private fun getAllStoriesWithLocation() {
        Log.d(TAG, "getAllStoriesWithLocation")
        viewModelScope.launch {
            try {
                _stories.value = Result.Loading
                val response = repository.getAllStories(location = 1)
                if (!response.error) {
                    if (response.listStory.isNotEmpty()) {
                        _stories.value = Result.Success(response.listStory)
                    }
                }
            } catch (e: HttpException) {
                val jsonString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonString, BasicResponse::class.java)
                Log.e(TAG, errorBody.message)
                _stories.value = Result.Error(errorBody.message)
            } catch (e: Exception) {
                _stories.value = Result.Error(e.message.toString())
            }
        }
    }

    companion object {
        private const val TAG = "MapsViewModel"
    }
}