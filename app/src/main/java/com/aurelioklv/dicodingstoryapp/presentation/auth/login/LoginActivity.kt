package com.aurelioklv.dicodingstoryapp.presentation.auth.login

import android.content.Intent
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
import com.aurelioklv.dicodingstoryapp.databinding.ActivityLoginBinding
import com.aurelioklv.dicodingstoryapp.presentation.auth.register.RegisterActivity
import com.aurelioklv.dicodingstoryapp.presentation.home.HomeActivity
import com.aurelioklv.dicodingstoryapp.presentation.utils.ViewModelFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkSession()

        binding.tvRedirectRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        binding.btnLogin.setOnClickListener { login() }

        observeLiveData()
        supportActionBar?.hide()
    }

    private fun checkSession() {
        viewModel.getToken().observe(this) {
            if (it != null) {
                redirectToHome()
            }
        }
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
                    redirectToHome()
                }

                else -> loadingDialog.dismiss()
            }
        }
    }

    private fun isValidInput(): Boolean {
        if (binding.edLoginEmail.text.isEmpty() ||
            binding.edLoginPassword.text.isNullOrEmpty()
        ) return false
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.edLoginEmail.text).matches()) return false

        return true
    }

    private fun login() {
        if (isValidInput()) {
            viewModel.login(
                binding.edLoginEmail.text.toString(),
                binding.edLoginPassword.text.toString()
            )
        } else {
            Toast.makeText(this, getString(R.string.please_fill_the_form), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun redirectToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}