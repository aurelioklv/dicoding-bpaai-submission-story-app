package com.aurelioklv.dicodingstoryapp.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aurelioklv.dicodingstoryapp.data.Result
import com.aurelioklv.dicodingstoryapp.data.remote.api.BasicResponse
import com.aurelioklv.dicodingstoryapp.data.remote.api.LoginResponse
import com.aurelioklv.dicodingstoryapp.data.repository.StoryRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _response =
        MutableLiveData<Result<LoginResponse>>()
    val response: LiveData<Result<LoginResponse>> = _response

    fun getToken(): LiveData<String?> {
        return repository.getToken().asLiveData()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _response.value = Result.Loading
                val response = repository.login(email, password)
                if (!response.error) {
                    if (response.loginResult!!.token.isNotEmpty()) {
                        _response.value = Result.Success(response)
                        repository.saveSession(
                            response.loginResult.token,
                            response.loginResult.name
                        )
                    }
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