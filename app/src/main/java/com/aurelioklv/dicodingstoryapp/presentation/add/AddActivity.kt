package com.aurelioklv.dicodingstoryapp.presentation.add

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aurelioklv.dicodingstoryapp.R
import com.aurelioklv.dicodingstoryapp.data.Result
import com.aurelioklv.dicodingstoryapp.databinding.ActivityAddBinding
import com.aurelioklv.dicodingstoryapp.presentation.utils.ViewModelFactory
import com.aurelioklv.dicodingstoryapp.presentation.utils.getImageUri
import com.aurelioklv.dicodingstoryapp.presentation.utils.reduceFileSize
import com.aurelioklv.dicodingstoryapp.presentation.utils.uriToFile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
    private var lat: Float? = null
    private var long: Float? = null
    private lateinit var loadingDialog: AlertDialog

    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_SHORT).show()
            } else {
                showPermissionSettingsDialog()
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        loadingDialog = AlertDialog.Builder(this).setView(R.layout.dialog_loading).create()

        binding.btnAddGallery.setOnClickListener { startGallery() }
        binding.btnAddCamera.setOnClickListener { startCamera() }
        binding.buttonAdd.setOnClickListener { submit() }
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getMyLocation()
            } else {
                lat = null
                long = null
            }
        }

        observeLiveData()

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isChanged()) showConfirmationDialog() else exit()
            }
        })

        supportActionBar?.hide()
    }

    private fun observeLiveData() {
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

    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
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
                    override fun onClick(dialog: DialogInterface?, which: Int) {}
                }).show()
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.request_location_permission))
            .setMessage(getString(R.string.request_location_permission_message))
            .setPositiveButton(getString(R.string.yes), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.setData(uri)
                    startActivity(intent)
                }
            })
            .setNegativeButton(getString(R.string.no), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {}
            })
            .setOnDismissListener {
                binding.switchLocation.isChecked = false
            }.create().show()
    }


    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun makePermissionRequest() {
        requestPermissionLauncher.launch(PERMISSION_LOCATION)
    }

    private fun getMyLocation() {
        if (checkPermission(PERMISSION_LOCATION)) {
            binding.switchLocation.isChecked = true
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    lat = it.latitude.toFloat()
                    long = it.longitude.toFloat()
                    Toast.makeText(this, "lat: $lat, long: $long", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            binding.switchLocation.isChecked = false
            makePermissionRequest()
        }
    }

    private fun submit() {
        currentImageUri?.let { uri ->
            loadingDialog.show()
            val imageFile = uriToFile(this, uri).reduceFileSize()
            val description = binding.edAddDescription.text.toString()

            val fileRequestBody = imageFile.asRequestBody("image/jpeg".toMediaType())
            val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())
            val latRequestBody = lat?.toString()?.toRequestBody("text/plain".toMediaType())
            val longRequestBody = long?.toString()?.toRequestBody("text/plain".toMediaType())

            val multipartBody =
                MultipartBody.Part.createFormData("photo", imageFile.name, fileRequestBody)

            viewModel.addStory(
                multipartBody,
                descriptionRequestBody,
                latRequestBody,
                longRequestBody
            )
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
        const val PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        const val ADD_SUCCESS = 1
        const val NO_CHANGES = -1
    }
}