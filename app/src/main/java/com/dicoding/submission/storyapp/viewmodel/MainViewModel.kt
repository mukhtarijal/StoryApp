package com.dicoding.submission.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.submission.storyapp.model.ListStoryItem
import com.dicoding.submission.storyapp.model.Result
import com.dicoding.submission.storyapp.model.StoryResponse
import com.dicoding.submission.storyapp.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _storiesResponse = MutableLiveData<Result<StoryResponse>>()
    val storiesResponse: LiveData<Result<StoryResponse>> = _storiesResponse

    private val _userTokenError = MutableLiveData<String>()
    val userTokenError: LiveData<String> = _userTokenError

    private val _userToken = MutableLiveData<String?>()
    val userToken: LiveData<String?> get() = _userToken

    init {
        fetchUserToken()
    }

    fun fetchUserToken() {
        viewModelScope.launch {
            _userToken.value = repository.getUserToken()
        }
    }

    fun getStories(page: Int? = null, size: Int? = null, location: Int? = 0) {
        viewModelScope.launch {
            try {
                val result = repository.getStories(page, size, location)
                _storiesResponse.value = result
            } catch (e: Exception) {
                _storiesResponse.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getStoriesPaged(location: Int): Flow<PagingData<ListStoryItem>> {
        return repository.getStoriesPaged(location).cachedIn(viewModelScope)
    }

    fun clearUserToken() {
        viewModelScope.launch {
            try {
                repository.clearUserToken()
            } catch (e: Exception) {
                _userTokenError.value = e.message ?: "Failed to clear user token"
            }
        }
    }
}
