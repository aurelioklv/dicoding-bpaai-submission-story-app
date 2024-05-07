package com.aurelioklv.dicodingstoryapp.presentation.add

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aurelioklv.dicodingstoryapp.R
import com.aurelioklv.dicodingstoryapp.data.Result
import com.aurelioklv.dicodingstoryapp.databinding.ActivityAddBinding
import com.aurelioklv.dicodingstoryapp.presentation.utils.ViewModelFactory
import com.aurelioklv.dicodingstoryapp.presentation.utils.getImageUri
import com.aurelioklv.dicodingstoryapp.presentation.utils.reduceFileSize
import com.aurelioklv.dicodingstoryapp.presentation.utils.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private var currentImageUri: Uri? = null
    private val viewModel: AddViewModel by viewModels<AddViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val launcherGallery =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                currentImageUri = uri
                showImage()
            } else {
                Toast.makeText(this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private val launcherIntentCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                showImage()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAddGallery.setOnClickListener { startGallery() }
        binding.btnAddCamera.setOnClickListener { startCamera() }
        binding.btnAddSubmit.setOnClickListener { submit() }

        observeLiveData()

        val confirmationDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.discard_changes))
            .setMessage(getString(R.string.discard_dialog_message))
            .setPositiveButton(
                getString(R.string.discard),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        exit()
                    }
                })
            .setNegativeButton(
                getString(R.string.dialog_negative),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                    }
                })


        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isChanged()) confirmationDialog.show() else exit()
            }
        })

        supportActionBar?.hide()
    }

    private fun observeLiveData() {
        val loadingDialog = AlertDialog.Builder(this).setView(R.layout.dialog_loading).create()
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        viewModel.response.observe(this) {
            when (it) {
                is Result.Loading -> loadingDialog.show()
                is Result.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, it.error, Toast.LENGTH_SHORT).show()
                }

                is Result.Success -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, it.data.message, Toast.LENGTH_SHORT).show()
                    setResult(ADD_SUCCESS)
                    finish()
                }

                else -> loadingDialog.dismiss()
            }
        }
    }

    private fun submit() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(this, uri).reduceFileSize()
            val description = binding.edAddDescription.text.toString()

            val fileRequestBody = imageFile.asRequestBody("image/jpeg".toMediaType())
            val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())

            val multipartBody =
                MultipartBody.Part.createFormData("photo", imageFile.name, fileRequestBody)

            viewModel.addStory(multipartBody, descriptionRequestBody)
        } ?: showToast(getString(R.string.please_fill_the_form))
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.ivAddPhotoPreview.setImageURI(it)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun isChanged(): Boolean {
        return currentImageUri != null || binding.edAddDescription.text.toString().isNotEmpty()
    }

    private fun exit() {
        setResult(NO_CHANGES)
        finish()
    }

    companion object {
        const val ADD_SUCCESS = 1
        const val NO_CHANGES = -1
    }
}