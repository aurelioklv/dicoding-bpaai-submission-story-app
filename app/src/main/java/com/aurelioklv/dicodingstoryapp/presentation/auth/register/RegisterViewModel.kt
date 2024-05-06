package com.aurelioklv.dicodingstoryapp.presentation.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurelioklv.dicodingstoryapp.data.Result
import com.aurelioklv.dicodingstoryapp.data.remote.api.ErrorResponse
import com.aurelioklv.dicodingstoryapp.data.remote.api.RegisterResponse
import com.aurelioklv.dicodingstoryapp.data.repository.StoryRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _response =
        MutableLiveData<Result<RegisterResponse>>()
    val response: LiveData<Result<RegisterResponse>> = _response

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _response.value = Result.Loading
                val response = repository.register(name, email, password)
                if (!response.error) {
                    _response.value = Result.Success(response)
                }
            } catch (e: HttpException) {
                val jsonString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonString, ErrorResponse::class.java)
                _response.value = Result.Error(errorBody.message ?: e.message())
            }
        }
    }
}