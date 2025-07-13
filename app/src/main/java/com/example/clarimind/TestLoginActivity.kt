package com.example.clarimind

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TestLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("TestLoginActivity", "onCreate started")
        
        try {
            Log.d("TestLoginActivity", "Setting content view...")
            setContentView(R.layout.activity_simple_login)
            Log.d("TestLoginActivity", "Content view set successfully")

            val signInButton = findViewById<Button>(R.id.btnGoogleSignIn)
            signInButton.setOnClickListener {
                Log.d("TestLoginActivity", "Sign in button clicked")
                Toast.makeText(this, "Sign In clicked!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            
            Log.d("TestLoginActivity", "TestLoginActivity setup complete")
            
        } catch (e: Exception) {
            Log.e("TestLoginActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            // If there's any error, go directly to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
} 