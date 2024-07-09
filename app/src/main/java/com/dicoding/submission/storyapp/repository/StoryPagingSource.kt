package com.dicoding.submission.storyapp.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.submission.storyapp.model.ListStoryItem

class StoryPagingSource(
    private val repository: StoryRepository,
    private val location: Int
) : PagingSource<Int, ListStoryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        val position = params.key ?: 1
        return try {
            val apiService = repository.getApiServiceWithToken()
            val response = apiService.getStories(page = position, size = params.loadSize, location = location)
            val stories = response.listStory?.filterNotNull() ?: emptyList()
            LoadResult.Page(
                data = stories,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (stories.isEmpty()) null else position + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}