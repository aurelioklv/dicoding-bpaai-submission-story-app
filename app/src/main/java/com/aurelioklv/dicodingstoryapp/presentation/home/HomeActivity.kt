package com.aurelioklv.dicodingstoryapp.presentation.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aurelioklv.dicodingstoryapp.R
import com.aurelioklv.dicodingstoryapp.data.Result
import com.aurelioklv.dicodingstoryapp.data.remote.api.StoryItem
import com.aurelioklv.dicodingstoryapp.databinding.ActivityHomeBinding
import com.aurelioklv.dicodingstoryapp.presentation.add.AddActivity
import com.aurelioklv.dicodingstoryapp.presentation.auth.login.LoginActivity
import com.aurelioklv.dicodingstoryapp.presentation.utils.StoryAdapter
import com.aurelioklv.dicodingstoryapp.presentation.utils.ViewModelFactory
import com.aurelioklv.dicodingstoryapp.presentation.utils.getFrontName

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AddActivity.ADD_SUCCESS) {
                viewModel.getAllStories()
            } else {
                Toast.makeText(this, "No story added", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeLiveData()

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            launcher.launch(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> logout()
            R.id.action_settings -> Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setStories(stories: List<StoryItem?>) {
        val adapter = StoryAdapter()
        adapter.submitList(stories)
        binding.rvStories.adapter = adapter
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager
    }

    private fun observeLiveData() {
        val loadingDialog = AlertDialog.Builder(this).setView(R.layout.dialog_loading).create()
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        viewModel.getName().observe(this) {
            if (it != null) {
                supportActionBar?.title = getString(R.string.greet_user, getFrontName(it))
            }
        }
        viewModel.getToken().observe(this) {
            Log.d(TAG, "observe token: $it")
            if (!it.isNullOrEmpty()) {
                Log.d(TAG, "getAllStories()")
                viewModel.getAllStories()
            }
        }
        viewModel.stories.observe(this) {
            when (it) {
                is Result.Loading -> loadingDialog.show()
                is Result.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, it.error, Toast.LENGTH_SHORT).show()
                }

                is Result.Success -> {
                    loadingDialog.dismiss()
                    setStories(it.data)
                }

                else -> loadingDialog.dismiss()
            }
        }
    }

    private fun logout() {
        viewModel.logout()
        Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "HomeActivity"
    }
}