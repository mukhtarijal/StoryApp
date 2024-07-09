package com.dicoding.submission.storyapp

import com.dicoding.submission.storyapp.model.ListStoryItem

object DataDummy {
    fun generateDummyStories(): List<ListStoryItem> {
        val stories = mutableListOf<ListStoryItem>()
        for (i in 1..10) {
            stories.add(
                ListStoryItem(
                    id = "$i",
                    name = "Name $i",
                    description = "Description $i",
                    photoUrl = "http://example.com/photo$i.jpg",
                    createdAt = "2021-11-11T00:00:00Z",
                    lat = -6.2,
                    lon = 106.8
                )
            )
        }
        return stories
    }
}
