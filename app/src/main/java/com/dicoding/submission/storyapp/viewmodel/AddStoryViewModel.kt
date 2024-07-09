package com.dicoding.submission.storyapp.viewmodel

import androidx.lifecycle.*
import com.dicoding.submission.storyapp.model.*
import com.dicoding.submission.storyapp.repository.StoryRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _addStoryResponse = MutableLiveData<Result<NewResponse>>()
    val addStoryResponse: LiveData<Result<NewResponse>> = _addStoryResponse

    fun addStory(file: MultipartBody.Part, description: RequestBody) {
        viewModelScope.launch {
            try {
                _addStoryResponse.value = repository.addStory(file, description)
            } catch (e: Exception) {
                _addStoryResponse.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }
}

