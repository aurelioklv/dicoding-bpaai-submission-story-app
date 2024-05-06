package com.aurelioklv.dicodingstoryapp.presentation.details

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aurelioklv.dicodingstoryapp.R
import com.aurelioklv.dicodingstoryapp.data.Result
import com.aurelioklv.dicodingstoryapp.databinding.ActivityDetailsBinding
import com.aurelioklv.dicodingstoryapp.presentation.utils.ViewModelFactory
import com.aurelioklv.dicodingstoryapp.presentation.utils.getReadingTimeMinute
import com.aurelioklv.dicodingstoryapp.presentation.utils.getTimeAgo
import com.aurelioklv.dicodingstoryapp.presentation.utils.getTimeMillisFromString
import com.bumptech.glide.Glide

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private val viewModel: DetailsViewModel by viewModels<DetailsViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val id = intent.getStringExtra(EXTRA_ID)!!
        viewModel.getDetails(id)

        viewModel.story.observe(this) {
            when (it) {
                is Result.Loading -> showLoading(true)
                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(this, it.error, Toast.LENGTH_SHORT).show()
                }

                is Result.Success -> {
                    showLoading(false)
                    Glide.with(binding.root)
                        .load(it.data.photoUrl)
                        .into(binding.ivDetailPhoto)
                    binding.tvDetailName.text = it.data.name
                    binding.tvTime.text =
                        getTimeAgo(getTimeMillisFromString(it.data.createdAt.toString()))
                    val minuteRead = getReadingTimeMinute(it.data.description.toString())
                    binding.tvReadingTime.text = getString(R.string.min_read, minuteRead)
                    binding.tvDetailDescription.text = it.data.description
                }

                else -> showLoading(false)
            }
        }

        supportActionBar?.hide()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressHorizontal.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        const val EXTRA_ID = "extra_id"
    }
}