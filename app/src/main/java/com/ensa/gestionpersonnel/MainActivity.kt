package com.ensa.gestionpersonnel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, com.ensa.gestionpersonnel.ui.main.MainActivity::class.java))
        finish()
    }
}