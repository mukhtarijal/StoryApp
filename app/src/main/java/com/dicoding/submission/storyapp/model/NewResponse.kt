package com.dicoding.submission.storyapp.model

import com.google.gson.annotations.SerializedName

data class NewResponse(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)
