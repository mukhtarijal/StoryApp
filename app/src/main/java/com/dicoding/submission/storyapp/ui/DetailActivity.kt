package com.dicoding.submission.storyapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.submission.storyapp.R
import com.dicoding.submission.storyapp.databinding.ActivityDetailBinding
import com.dicoding.submission.storyapp.di.Injection
import com.dicoding.submission.storyapp.model.Result
import com.dicoding.submission.storyapp.viewmodel.DetailViewModel
import com.dicoding.submission.storyapp.viewmodel.ViewModelFactory

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val detailViewModel: DetailViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(this))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.title_detail_story)

        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        if (storyId != null) {
            showLoading(true)
            detailViewModel.getStoryDetail(storyId)
            detailViewModel.storyDetailResponse.observe(this) { result ->
                showLoading(false)
                when (result) {
                    is Result.Success -> {
                        val story = result.data.story
                        binding.tvDetailName.text = story?.name
                        binding.tvDetailDescription.text = story?.description
                        Glide.with(this)
                            .load(story?.photoUrl)
                            .into(binding.ivDetailPhoto)
                    }
                    is Result.Error -> {
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.error_story_id_not_available), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.progressOverlay.visibility = View.VISIBLE
            binding.loadingOverlay.progressBar.visibility = View.VISIBLE
        } else {
            binding.loadingOverlay.progressOverlay.visibility = View.GONE
            binding.loadingOverlay.progressBar.visibility = View.GONE
        }
    }

    companion object {
        const val EXTRA_STORY_ID = "STORY_ID"
    }
}

