package com.dicoding.submission.storyapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.submission.storyapp.R
import com.dicoding.submission.storyapp.adapter.LoadingStateAdapter
import com.dicoding.submission.storyapp.adapter.StoryPagingAdapter
import com.dicoding.submission.storyapp.databinding.ActivityMainBinding
import com.dicoding.submission.storyapp.di.Injection
import com.dicoding.submission.storyapp.helper.LanguageManager
import com.dicoding.submission.storyapp.viewmodel.MainViewModel
import com.dicoding.submission.storyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(this))
    }
    private lateinit var adapter: StoryPagingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.loadLocale(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.title_home)

        mainViewModel.userToken.observe(this) { token ->
            if (token.isNullOrEmpty()) {
                navigateToLogin()
            } else {
                setupRecyclerView()
                loadStories()
                binding.fabAddStory.setOnClickListener {
                    startActivity(Intent(this@MainActivity, AddStoryActivity::class.java))
                }
            }
        }

        mainViewModel.userTokenError.observe(this) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmationDialog()
                true
            }
            R.id.action_refresh -> {
                loadStories()
                true
            }
            R.id.action_language -> {
                toggleLanguage()
                true
            }
            R.id.action_maps_story -> {
                startActivity(Intent(this, MapsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleLanguage() {
        val currentLanguage = LanguageManager.getSavedLanguage(this)
        val newLanguage = if (currentLanguage == "id") "en" else "id"
        LanguageManager.setLocale(this, newLanguage)
        recreate()
    }

    private fun setupRecyclerView() {
        adapter = StoryPagingAdapter { story ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_STORY_ID, story.id)
            startActivity(intent)
        }
        binding.rvStories.layoutManager = LinearLayoutManager(this)
        binding.rvStories.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter { adapter.retry() }
        )

        adapter.addLoadStateListener { loadState ->
            binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading
            binding.rvStories.isVisible = loadState.source.refresh is LoadState.NotLoading
            binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error

            if (loadState.source.refresh is LoadState.Error) {
                val errorState = loadState.source.refresh as LoadState.Error
                Toast.makeText(this, errorState.error.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

        binding.retryButton.setOnClickListener { adapter.retry() }
    }

    private fun loadStories() {
        lifecycleScope.launch {
            mainViewModel.getStoriesPaged(location = 0).collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.logout)
        builder.setMessage(R.string.logout_confirmation)

        builder.setPositiveButton(R.string.yes) { _, _ ->
            logout()
        }

        builder.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun logout() {
        mainViewModel.clearUserToken()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }
}
