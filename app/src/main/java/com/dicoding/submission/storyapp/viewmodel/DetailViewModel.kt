package com.dicoding.submission.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.submission.storyapp.model.DetailStoryResponse
import com.dicoding.submission.storyapp.model.Result
import com.dicoding.submission.storyapp.repository.StoryRepository
import kotlinx.coroutines.launch

class DetailViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _storyDetailResponse = MutableLiveData<Result<DetailStoryResponse>>()
    val storyDetailResponse: LiveData<Result<DetailStoryResponse>> = _storyDetailResponse

    fun getStoryDetail(id: String) {
        viewModelScope.launch {
            try {
                val result = repository.getDetailStory(id)
                _storyDetailResponse.value = result
            } catch (e: Exception) {
                _storyDetailResponse.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }
}
