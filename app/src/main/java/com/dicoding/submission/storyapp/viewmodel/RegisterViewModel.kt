package com.dicoding.submission.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.submission.storyapp.model.NewResponse
import com.dicoding.submission.storyapp.model.Result
import com.dicoding.submission.storyapp.repository.StoryRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _userTokenError = MutableLiveData<String>()
    val userTokenError: LiveData<String> = _userTokenError

    private val _registerResponse = MutableLiveData<Result<NewResponse>>()
    val registerResponse: LiveData<Result<NewResponse>> = _registerResponse

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = repository.register(name, email, password)
                _registerResponse.value = result
            } catch (e: Exception) {
                _registerResponse.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }
}
