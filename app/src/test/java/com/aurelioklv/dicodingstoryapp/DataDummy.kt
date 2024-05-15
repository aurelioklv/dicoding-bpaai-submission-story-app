package com.aurelioklv.dicodingstoryapp

import com.aurelioklv.dicodingstoryapp.data.remote.api.StoryItem

object DataDummy {
    fun generateDummyStories(): List<StoryItem> {
        val stories: MutableList<StoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = StoryItem(
                "photoUrl $i",
                "createdAt $i",
                "name $i",
                "desc $i",
                i.toDouble(),
                "id $i",
                i.toDouble()
            )
            stories.add(story)
        }
        return stories
    }
}