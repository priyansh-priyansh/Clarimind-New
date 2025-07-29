package com.example.clarimind

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.clarimind.databinding.ActivitySetupWizardBinding

class SetupWizardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupWizardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupWizardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // For now, just navigate to MainActivity
        // In a real app, this would be a setup wizard with multiple steps
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
} 