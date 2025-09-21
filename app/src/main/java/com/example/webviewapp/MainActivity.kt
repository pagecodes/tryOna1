package com.example.webviewapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.example.webviewapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        const val PREFS_NAME = "WebViewAppPrefs"
        const val TOKEN_KEY = "auth_token"
        const val BASE_URL = "https://your-website.com" // Replace with actual URL
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        val token = sharedPreferences.getString(TOKEN_KEY, null)
        if (token.isNullOrEmpty()) {
            navigateToLogin()
            return
        }
        
        setupWebView(token)
    }
    
    private fun setupWebView(token: String) {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            // Add JavaScript interface
            addJavascriptInterface(WebAppInterface(token), "Android")
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Inject token into page if needed
                    view?.evaluateJavascript(
                        "if (typeof setAuthToken === 'function') { setAuthToken('$token'); }",
                        null
                    )
                }
                
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    // Handle errors appropriately
                }
            }
            
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    // Log console messages for debugging
                    return super.onConsoleMessage(consoleMessage)
                }
            }
            
            // Load the main page
            loadUrl(BASE_URL)
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    
    inner class WebAppInterface(private val token: String) {
        
        @JavascriptInterface
        fun getAuthToken(): String {
            return token
        }
        
        @JavascriptInterface
        fun logout() {
            runOnUiThread {
                // Clear stored token
                sharedPreferences.edit().remove(TOKEN_KEY).apply()
                navigateToLogin()
            }
        }
        
        @JavascriptInterface
        fun showToast(message: String) {
            runOnUiThread {
                android.widget.Toast.makeText(this@MainActivity, message, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}