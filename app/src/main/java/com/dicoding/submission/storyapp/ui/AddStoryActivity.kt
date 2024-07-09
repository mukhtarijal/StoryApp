package com.dicoding.submission.storyapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.submission.storyapp.R
import com.dicoding.submission.storyapp.databinding.ActivityAddStoryBinding
import com.dicoding.submission.storyapp.di.Injection
import com.dicoding.submission.storyapp.helper.reduceFileImage
import com.dicoding.submission.storyapp.model.Result
import com.dicoding.submission.storyapp.viewmodel.AddStoryViewModel
import com.dicoding.submission.storyapp.viewmodel.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private var getFile: File? = null
    private val addStoryViewModel: AddStoryViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(this))
    }

    private val launcherCamera = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val tempFile = saveBitmapToFile(bitmap, this)
            getFile = tempFile
            binding.ivPreview.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, getString(R.string.error_camera_capture), Toast.LENGTH_SHORT).show()
        }
    }

    private val launcherGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            getFile = uriToFile(it, this)
            binding.ivPreview.setImageURI(it)
        } ?: run {
            Toast.makeText(this, getString(R.string.error_gallery_selection), Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, getString(R.string.error_permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.title_add_story)

        binding.btnGallery.setOnClickListener {
            launcherGallery.launch("image/*")
        }

        binding.btnCamera.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    launchCamera()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        binding.btnUpload.setOnClickListener {
            uploadStory()
        }
    }

    private fun launchCamera() {
        launcherCamera.launch()
    }

    private fun uploadStory() {
        val description = binding.edAddDescription.text.toString()
        if (description.isBlank()) {
            Toast.makeText(this, getString(R.string.error_empty_description), Toast.LENGTH_SHORT).show()
            return
        }
        if (getFile == null) {
            Toast.makeText(this, getString(R.string.error_no_image_selected), Toast.LENGTH_SHORT).show()
            return
        }

        val file = reduceFileImage(getFile as File)
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )

        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

        showLoading(true)
        addStoryViewModel.addStory(imageMultipart, descriptionRequestBody)
        addStoryViewModel.addStoryResponse.observe(this) { result ->
            showLoading(false)
            when (result) {
                is Result.Success -> {
                    Toast.makeText(
                        this,
                        getString(R.string.success_story_uploaded),
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

                is Result.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT)
                        .show()
                }
            }
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

    private fun saveBitmapToFile(bitmap: Bitmap, context: Context): File {
        val tempFile =
            File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp_image.jpg")
        try {
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return tempFile
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val myFile =
            File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "selected_image.jpg")

        val inputStream = contentResolver.openInputStream(selectedImg) ?: return myFile
        val outputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) {
            outputStream.write(buf, 0, len)
        }
        outputStream.close()
        inputStream.close()

        return myFile
    }
}
