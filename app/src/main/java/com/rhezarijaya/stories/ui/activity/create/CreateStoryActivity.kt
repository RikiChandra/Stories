package com.rhezarijaya.stories.ui.activity.create

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.rhezarijaya.stories.BuildConfig
import com.rhezarijaya.stories.R
import com.rhezarijaya.stories.databinding.ActivityCreateStoryBinding
import com.rhezarijaya.stories.model.CreateStoryResponse
import com.rhezarijaya.stories.ui.activity.main.MainActivity
import com.rhezarijaya.stories.util.AppPreferences
import com.rhezarijaya.stories.util.Constants
import com.rhezarijaya.stories.util.ViewModelFactory
import com.rhezarijaya.stories.util.getPlaceholderImage
import java.io.*

class CreateStoryActivity : AppCompatActivity() {
    private val Context.dataStore by preferencesDataStore(name = Constants.PREFERENCES_NAME)
    private lateinit var binding: ActivityCreateStoryBinding
    private lateinit var createStoryViewModel: CreateStoryViewModel

    private lateinit var tempCameraImagePath: String
    private var currentImageFile: File? = null

    private val intentCameraResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val resultImage = File(tempCameraImagePath)
                currentImageFile = resultImage

                Glide.with(this)
                    .load(resultImage)
                    .placeholder(R.drawable.ic_baseline_broken_image_24)
                    .error(R.drawable.ic_baseline_broken_image_24)
                    .into(binding.createIvPreview)
            }
        }

    private val intentGalleryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val resultImage = getFileFromUri(this, it.data?.data as Uri)
                currentImageFile = resultImage

                Glide.with(this)
                    .load(resultImage)
                    .placeholder(R.drawable.ic_baseline_broken_image_24)
                    .error(R.drawable.ic_baseline_broken_image_24)
                    .into(binding.createIvPreview)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        val appPreferences = AppPreferences.getInstance(dataStore)
        createStoryViewModel =
            ViewModelProvider(
                this,
                ViewModelFactory(appPreferences)
            )[CreateStoryViewModel::class.java]

        binding.createBtnCamera.setOnClickListener {
            val tempFile = createImageTempFile(this)

            tempCameraImagePath = tempFile.absolutePath
            val tempFileUri: Uri = FileProvider.getUriForFile(
                this@CreateStoryActivity,
                BuildConfig.APPLICATION_ID,
                tempFile
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                resolveActivity(packageManager)
                putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri)
            }

            intentCameraResult.launch(intent)
        }

        binding.createBtnGallery.setOnClickListener {
            intentGalleryResult.launch(
                Intent.createChooser(
                    Intent(ACTION_GET_CONTENT).apply { type = "image/*" },
                    getString(R.string.choose_picture)
                )
            )
        }

        binding.createBtnClear.setOnClickListener {
            currentImageFile = null
            Glide.with(this)
                .load(
                    getPlaceholderImage(this)
                )
                .into(binding.createIvPreview)
        }

        binding.createBtnSubmit.setOnClickListener {
            if (currentImageFile != null && !TextUtils.isEmpty(binding.createEtDescription.text.toString())) {
                val compressed = compressImageFile(currentImageFile!!)

                createStoryViewModel.submit(
                    object : OnSuccessCallback<CreateStoryResponse> {
                        override fun onSuccess(message: CreateStoryResponse) {
                            Toast.makeText(
                                this@CreateStoryActivity,
                                message.message,
                                Toast.LENGTH_SHORT
                            ).show()

                            setResult(MainActivity.INTENT_CREATE_STORY)
                            finish()
                        }
                    },
                    binding.createEtDescription.text.toString(),
                    compressed,
                    null,
                    null
                )
            } else {
                Toast.makeText(this, "Please add the image and description", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        createStoryViewModel.isLoading().observe(this) { isLoading ->
            binding.apply {
                createProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                createBtnCamera.isEnabled = !isLoading
                createBtnGallery.isEnabled = !isLoading
                createBtnClear.isEnabled = !isLoading
                createEtDescription.isEnabled = !isLoading
                createBtnSubmit.isEnabled = !isLoading
            }
        }

        createStoryViewModel.getCreateError().observe(this) { createError ->
            createError.getData()?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun compressImageFile(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)

        var compressQuality = 100
        var streamLength: Int

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)

            val bmpPicByteArray = bmpStream.toByteArray()

            streamLength = bmpPicByteArray.size
            compressQuality -= 10
        } while (streamLength > 1000000)

        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    private fun createImageTempFile(context: Context): File {
        return File.createTempFile(
            System.currentTimeMillis().toString() + "_" + (Math.random() * 1000).toInt().toString(),
            ".jpg",
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
    }

    private fun getFileFromUri(context: Context, uriToFile: Uri): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createImageTempFile(context)

        val inputStream = contentResolver.openInputStream(uriToFile) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)

        val buffer = ByteArray(1024)
        var length: Int

        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        inputStream.close()
        outputStream.close()

        return myFile
    }
}