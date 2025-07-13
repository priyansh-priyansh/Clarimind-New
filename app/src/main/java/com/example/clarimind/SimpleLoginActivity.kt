package com.example.clarimind

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.clarimind.databinding.ActivityLoginBinding

class SimpleLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("SimpleLoginActivity", "onCreate started")
        
        try {
            Log.d("SimpleLoginActivity", "Inflating layout...")
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d("SimpleLoginActivity", "Layout set successfully")

            binding.btnGoogleSignIn.setOnClickListener {
                Log.d("SimpleLoginActivity", "Google Sign-In button clicked")
                Toast.makeText(this, "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show()
                // For now, just go to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            
            Log.d("SimpleLoginActivity", "LoginActivity setup complete")
            
        } catch (e: Exception) {
            Log.e("SimpleLoginActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            // If there's any error, go directly to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
} 