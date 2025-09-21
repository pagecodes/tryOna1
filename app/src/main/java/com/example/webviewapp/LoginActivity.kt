package com.example.webviewapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.webviewapp.databinding.ActivityLoginBinding
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val client = OkHttpClient()
    
    companion object {
        const val PREFS_NAME = "WebViewAppPrefs"
        const val TOKEN_KEY = "auth_token"
        const val BASE_URL = "https://your-website.com" // Replace with actual URL
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        // Check if user is already logged in
        val token = sharedPreferences.getString(TOKEN_KEY, null)
        if (!token.isNullOrEmpty()) {
            navigateToMain()
            return
        }
        
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (username.isEmpty() || password.isEmpty()) {
                showError("Please enter both username and password")
                return@setOnClickListener
            }
            
            performLogin(username, password)
        }
    }
    
    private fun performLogin(username: String, password: String) {
        showLoading(true)
        
        val json = """{"username":"$username","password":"$password"}"""
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        
        val request = Request.Builder()
            .url("$BASE_URL/api-token-auth/")
            .post(body)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    showLoading(false)
                    showError(getString(R.string.network_error))
                }
            }
            
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    showLoading(false)
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        try {
                            val gson = Gson()
                            val tokenResponse = gson.fromJson(responseBody, TokenResponse::class.java)
                            
                            // Store token
                            sharedPreferences.edit()
                                .putString(TOKEN_KEY, tokenResponse.token)
                                .apply()
                            
                            navigateToMain()
                        } catch (e: Exception) {
                            showError("Invalid response format")
                        }
                    } else {
                        showError(getString(R.string.login_error))
                    }
                }
            }
        })
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
    }
    
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    data class TokenResponse(val token: String)
}