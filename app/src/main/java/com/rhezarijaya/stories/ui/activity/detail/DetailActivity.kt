package com.rhezarijaya.stories.ui.activity.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.rhezarijaya.stories.R
import com.rhezarijaya.stories.databinding.ActivityDetailBinding
import com.rhezarijaya.stories.model.Story
import com.rhezarijaya.stories.service.formatCreatedAt
import com.rhezarijaya.stories.util.Constants

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        val story = intent.getParcelableExtra<Story>(Constants.INTENT_MAIN_TO_DETAIL)

        story?.let {
            Glide.with(this)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_baseline_broken_image_24)
                .error(R.drawable.ic_baseline_broken_image_24)
                .into(binding.detailIvPhoto)
            binding.detailIvPhoto.contentDescription = story.description

            binding.detailTvName.text = story.name
            binding.detailTvCreatedAt.text = String.format(
                getString(R.string.created_at_format),
                formatCreatedAt(story.createdAt ?: "")
            )
            binding.detailTvDescription.text = story.description
            binding.detailTvLocation.text = String.format(getString(R.string.location_format), "")
        } ?: run {
            Toast.makeText(this, getString(R.string.data_invalid), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}