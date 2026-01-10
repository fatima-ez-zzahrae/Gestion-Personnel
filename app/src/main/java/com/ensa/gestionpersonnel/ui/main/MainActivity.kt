package com.ensa.gestionpersonnel.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.data.local.PreferencesManager
import com.ensa.gestionpersonnel.databinding.ActivityMainBinding
import com.ensa.gestionpersonnel.ui.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            if (!preferencesManager.isLoggedIn()) {
                navigateToLogin()
                return@launch
            }
        }

        setupToolbar()
        setupNavigation()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment,
                R.id.personnelListFragment,
                R.id.absenceMenuFragment,
                R.id.missionListFragment,
                R.id.diplomeAvancementMenuFragment  // ← AJOUTÉ
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // Fragments sans bottom nav
                R.id.personnelFormFragment,
                R.id.personnelDetailFragment,
                R.id.profileRHFragment,
                R.id.absenceFormFragment,
                R.id.absenceDetailFragment,
                R.id.absenceListFragment,
                R.id.missionFormFragment,
                R.id.missionDetailFragment,
                    // ← AJOUTÉ : Diplômes & Avancements
                R.id.diplomeListFragment,
                R.id.diplomeFormFragment,
                R.id.diplomeDetailFragment,
                R.id.avancementListFragment,
                R.id.avancementFormFragment,
                R.id.avancementDetailFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }
            }

            supportActionBar?.title = destination.label
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                navController.navigate(R.id.profileRHFragment)
                true
            }
            R.id.action_notifications -> {
                Toast.makeText(this, "Notifications - À implémenter", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Déconnexion")
            .setMessage("Voulez-vous vraiment vous déconnecter ?")
            .setPositiveButton("Déconnexion") { _, _ ->
                logout()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun logout() {
        lifecycleScope.launch {
            preferencesManager.clearSession()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}