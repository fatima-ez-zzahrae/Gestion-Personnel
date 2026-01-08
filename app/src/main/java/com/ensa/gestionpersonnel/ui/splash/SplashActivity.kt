package com.ensa.gestionpersonnel.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ensa.gestionpersonnel.databinding.ActivitySplashBinding
import com.ensa.gestionpersonnel.data.local.PreferencesManager
import com.ensa.gestionpersonnel.ui.auth.LoginActivity
import com.ensa.gestionpersonnel.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val splashDelay: Long = 2000 // 2 secondes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Vérifier l'état de connexion et rediriger après le délai
        lifecycleScope.launch {
            delay(splashDelay)
            checkAuthAndNavigate()
        }
    }

    private suspend fun checkAuthAndNavigate() {
        val isLoggedIn = preferencesManager.isLoggedIn()
        
        val intent = if (isLoggedIn) {
            Intent(this@SplashActivity, MainActivity::class.java)
        } else {
            Intent(this@SplashActivity, LoginActivity::class.java)
        }
        
        startActivity(intent)
        finish()
    }
}

