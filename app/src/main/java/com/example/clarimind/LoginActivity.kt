package com.example.clarimind

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.clarimind.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                firebaseAuthWithGoogle(idToken)
            } else {
                Toast.makeText(this, "Google sign-in failed: No ID token found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e("LoginActivity", "Google sign-in failed: ${e.statusCode}", e)
            Toast.makeText(this, "Google sign-in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize Firebase Auth
            auth = FirebaseAuth.getInstance()
            
            // Initialize Google Sign-In
            oneTapClient = Identity.getSignInClient(this)

            signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .build()
                )
                .setAutoSelectEnabled(false)
                .build()

            binding.btnGoogleSignIn.setOnClickListener {
                signInWithGoogle()
            }

            // Check if user is already signed in
            if (auth.currentUser != null) {
                goToHome()
            }
            
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error in onCreate: ${e.message}", e)
            // Fallback to MainActivity if there's an error
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender)
                        .build()
                    signInLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Couldn't start One Tap UI: ${e.localizedMessage}")
                    Toast.makeText(this, "Couldn't start sign-in", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener(this) { e ->
                Log.e("LoginActivity", "One Tap sign-in failed", e)
                Toast.makeText(this, "Sign-in failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "signInWithCredential:success")
                    goToHome()
                } else {
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToHome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
} 