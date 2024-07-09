package com.dicoding.submission.storyapp.di

import android.content.Context
import com.dicoding.submission.storyapp.helper.UserPreference
import com.dicoding.submission.storyapp.repository.StoryRepository

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val userPreference = UserPreference.getInstance(context)
        return StoryRepository.getInstance(userPreference)
    }
}

