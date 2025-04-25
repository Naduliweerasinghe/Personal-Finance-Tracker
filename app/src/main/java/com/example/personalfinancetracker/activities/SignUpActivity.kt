package com.example.personalfinancetracker.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.databinding.ActivitySignupBinding
import com.example.personalfinancetracker.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val sharedPreferences by lazy { getSharedPreferences("user_prefs", MODE_PRIVATE) }
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (validateInput(name, email, password)) {
                if (isEmailAvailable(email)) {
                    val user = User(name, email, password)
                    saveUser(user)
                    Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    binding.tilEmail.error = "Email already registered"
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.tilName.error = "Full name is required"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        return isValid
    }

    private fun isEmailAvailable(email: String): Boolean {
        val usersJson = sharedPreferences.getString("users", "[]")
        val type = object : TypeToken<List<User>>() {}.type
        val users: List<User> = gson.fromJson(usersJson, type)
        return users.none { it.email == email }
    }

    private fun saveUser(user: User) {
        val usersJson = sharedPreferences.getString("users", "[]")
        val type = object : TypeToken<List<User>>() {}.type
        val users: MutableList<User> = gson.fromJson(usersJson, type)
        users.add(user)
        sharedPreferences.edit().putString("users", gson.toJson(users)).apply()
    }
} 