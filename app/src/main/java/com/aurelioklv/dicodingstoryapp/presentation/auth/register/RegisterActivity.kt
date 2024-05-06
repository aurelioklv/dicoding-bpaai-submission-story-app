package com.aurelioklv.dicodingstoryapp.presentation.auth.register

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aurelioklv.dicodingstoryapp.R
import com.aurelioklv.dicodingstoryapp.data.Result
import com.aurelioklv.dicodingstoryapp.databinding.ActivityRegisterBinding
import com.aurelioklv.dicodingstoryapp.presentation.utils.ViewModelFactory

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.tvRedirectLogin.setOnClickListener {
            finish()
        }
        binding.btnRegister.setOnClickListener { register() }

        observeLiveData()

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
                    finish()
                }

                else -> loadingDialog.dismiss()
            }
        }
    }

    private fun register() {
        if (isValidInput()) {
            viewModel.register(
                binding.edRegisterName.text.toString(),
                binding.edRegisterEmail.text.toString(),
                binding.edRegisterPassword.text.toString(),
            )
        } else {
            Toast.makeText(this, getString(R.string.please_fill_the_form), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun isValidInput(): Boolean {
        if (binding.edRegisterEmail.text.isEmpty() ||
            binding.edRegisterName.text.isEmpty() ||
            binding.edRegisterPassword.text.isNullOrEmpty()
        ) return false
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.edRegisterEmail.text).matches()) return false

        return true
    }
}