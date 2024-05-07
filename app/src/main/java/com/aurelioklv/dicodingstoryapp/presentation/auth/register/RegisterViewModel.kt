package com.aurelioklv.dicodingstoryapp.presentation.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurelioklv.dicodingstoryapp.data.Result
import com.aurelioklv.dicodingstoryapp.data.remote.api.BasicResponse
import com.aurelioklv.dicodingstoryapp.data.repository.StoryRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _response =
        MutableLiveData<Result<BasicResponse>>()
    val response: LiveData<Result<BasicResponse>> = _response

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
                val errorBody = Gson().fromJson(jsonString, BasicResponse::class.java)
                _response.value = Result.Error(errorBody.message)
            } catch (e: Exception) {
                _response.value = Result.Error(e.message.toString())
            }
        }
    }
}