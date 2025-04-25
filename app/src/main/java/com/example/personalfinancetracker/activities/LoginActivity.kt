package com.example.personalfinancetracker.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.models.User
import com.example.personalfinancetracker.utils.PreferencesManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var preferencesManager: PreferencesManager
    private val sharedPreferences by lazy { getSharedPreferences("user_prefs", MODE_PRIVATE) }
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is already logged in
        preferencesManager = PreferencesManager(this)
        if (preferencesManager.isUserLoggedIn()) {
            navigateToMainActivity()
            return
        }

        setContentView(R.layout.activity_login)
        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
    }

    private fun setupClickListeners() {
        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            if (validateInputs()) {
                if (authenticateUser()) {
                    val email = etEmail.text.toString().trim()
                    val password = etPassword.text.toString().trim()
                    
                    // Save user session
                    preferencesManager.saveUserEmail(email)
                    preferencesManager.setUserLoggedIn(true)
                    
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<TextView>(R.id.tvSignUp).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        // Clear errors when user starts typing
        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilEmail.error = null
        }
        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilPassword.error = null
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Please enter a valid email"
            isValid = false
        }

        if (password.isEmpty()) {
            tilPassword.error = "Password is required"
            isValid = false
        }

        return isValid
    }

    private fun authenticateUser(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Get all registered users
        val usersJson = sharedPreferences.getString("users", "[]")
        val type = object : TypeToken<List<User>>() {}.type
        val users: List<User> = gson.fromJson(usersJson, type)

        // Check if any user matches the credentials
        return users.any { user -> 
            user.email == email && user.password == password 
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 