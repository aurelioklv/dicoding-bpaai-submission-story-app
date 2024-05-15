package com.aurelioklv.dicodingstoryapp.presentation.home

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
import com.aurelioklv.dicodingstoryapp.databinding.ActivityHomeBinding
import com.aurelioklv.dicodingstoryapp.presentation.add.AddActivity
import com.aurelioklv.dicodingstoryapp.presentation.auth.login.LoginActivity
import com.aurelioklv.dicodingstoryapp.presentation.maps.MapsActivity
import com.aurelioklv.dicodingstoryapp.presentation.utils.LoadingStateAdapter
import com.aurelioklv.dicodingstoryapp.presentation.utils.StoryAdapter
import com.aurelioklv.dicodingstoryapp.presentation.utils.ViewModelFactory
import com.aurelioklv.dicodingstoryapp.presentation.utils.getFrontName

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val adapter = StoryAdapter()

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AddActivity.ADD_SUCCESS) {
                adapter.refresh()
            } else {
                Toast.makeText(this, getString(R.string.no_story_added), Toast.LENGTH_SHORT).show()
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

        binding.root.setOnRefreshListener { adapter.refresh() }
        adapter.addOnPagesUpdatedListener { binding.root.isRefreshing = false }
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            launcher.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                val confirmationDialog = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.logout))
                    .setPositiveButton(
                        getString(R.string.dialog_continue),
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                logout()
                            }
                        })
                    .setNegativeButton(
                        getString(R.string.dialog_negative),
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                            }
                        })
                confirmationDialog.show()
            }

            R.id.action_settings -> startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))

            R.id.action_maps -> startActivity(Intent(this, MapsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager
    }

    private fun observeLiveData() {
        binding.rvStories.adapter = adapter.withLoadStateFooter(
            LoadingStateAdapter { adapter.retry() }
        )
        viewModel.stories.observe(this) {
            Log.d(TAG, "observeLiveData: $it")
            adapter.submitData(lifecycle, it)
        }

        viewModel.getName().observe(this) {
            if (it != null) {
                supportActionBar?.title = getString(R.string.greet_user, getFrontName(it))
            }
        }
    }

    private fun logout() {
        viewModel.logout()
        Toast.makeText(this, getString(R.string.logout), Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "HomeActivity"
    }
}