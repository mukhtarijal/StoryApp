package com.dicoding.submission.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.submission.storyapp.model.LoginResponse
import com.dicoding.submission.storyapp.model.Result
import com.dicoding.submission.storyapp.repository.StoryRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _loginResponse = MutableLiveData<Result<LoginResponse>>()
    val loginResponse: LiveData<Result<LoginResponse>> = _loginResponse

    private val _userTokenError = MutableLiveData<String>()
    val userTokenError: LiveData<String> = _userTokenError

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = repository.login(email, password)
                _loginResponse.value = result
            } catch (e: Exception) {
                _loginResponse.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveUserToken(token: String) {
        viewModelScope.launch {
            try {
                repository.saveUserToken(token)
            } catch (e: Exception) {
                _userTokenError.value = e.message ?: "Failed to save user token"
            }
        }
    }

    fun getUserToken(): LiveData<String?> {
        val tokenLiveData = MutableLiveData<String?>()
        viewModelScope.launch {
            tokenLiveData.value = repository.getUserToken()
        }
        return tokenLiveData
    }
}