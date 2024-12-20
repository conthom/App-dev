package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "MYTAG"
    private lateinit var usernameInput: EditText
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var sharedPreferences: SharedPreferences
    // 3EPWNM
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        // Load saved theme
        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        checkAutoLogin()

        val savedTheme = sharedPreferences.getString("theme", "Default Theme") ?: "Default Theme"

        // Apply theme before setting content view
        when (savedTheme) {
            "Default Theme" -> setTheme(R.style.Theme_FinalProject)
            "Light Theme" -> setTheme(R.style.Theme_App_Light)
            "Dark Theme" -> setTheme(R.style.Theme_App_Dark)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize views
        val createButton: Button = findViewById(R.id.createButton)
        val joinButton: Button = findViewById(R.id.joinButton)
        val tokenEditText: EditText = findViewById(R.id.tokenInput)
        usernameInput = findViewById(R.id.usernameInput) // Initialize username input
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox) // Initialize checkbox

        // Load saved preferences
        loadSavedPreferences()


        // Create List Button Listener
        createButton.setOnClickListener {
            if (isUsernameValid()) {
                val intent = Intent(this, NewList::class.java)
                savePreferences() // Save the username before proceeding
                startActivity(intent)
            }
        }

        // Join List Button Listener
        joinButton.setOnClickListener {
            val token = tokenEditText.text.toString()
            if (!isUsernameValid()) {
                return@setOnClickListener // Exit if the username is invalid
            }

            if (token.isEmpty()) {
                Toast.makeText(this, "Please enter a token", Toast.LENGTH_SHORT).show()
            } else {
                savePreferences() // Save preferences before joining
                joinGroceryList(token)
            }
        }

    }
    private fun isUsernameValid(): Boolean {
        val username = usernameInput.text.toString().trim()
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    private fun checkAutoLogin() {
        // Check if the user has "Remember Me" checked and a saved username
        val savedName = sharedPreferences.getString("username", "")
        val isRemembered = sharedPreferences.getBoolean("rememberMe", false)

        if (isRemembered && !savedName.isNullOrEmpty()) {
            // Attempt to auto-login by checking the user's associated list
            val token = sharedPreferences.getString("userToken", "")
            if (!token.isNullOrEmpty()) {
                joinGroceryList(token)
            }
        }
    }

    private fun loadSavedPreferences() {
        val savedName = sharedPreferences.getString("username", "")
        val isRemembered = sharedPreferences.getBoolean("rememberMe", false)

        if (isRemembered) {
            usernameInput.setText(savedName ?: "") // Use safe call to prevent null
            rememberMeCheckbox.isChecked = true
        }
    }

    private fun savePreferences() {
        val editor = sharedPreferences.edit()
        val username = usernameInput.text.toString()

        if (rememberMeCheckbox.isChecked) {
            editor.putString("username", username)
            editor.putBoolean("rememberMe", true)
        } else {
            editor.clear() // Clear saved data if "Remember Me" is unchecked
        }
        editor.apply()
    }

    private fun joinGroceryList(token: String) {
        Log.w(TAG, token)
        val editor = sharedPreferences.edit()
        editor.putString("userToken",token)
        editor.apply()
        // Search for the document with the given token in the Firestore collection
        db.collection("lists")
            .whereEqualTo("token", token)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Token found, open ListHome activity
                    val document = documents.documents[0]
                    val documentID = document.id
                    Log.d(TAG, documentID)
                    val intent = Intent(this, ListHome::class.java)
                    intent.putExtra("documentID", documentID) // Pass the document ID
                    intent.putExtra("tokenID", token) // Pass the token ID
                    startActivity(intent)
                } else {
                    // Token not found
                    Toast.makeText(this, "Invalid token. List not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, e.toString())
                Toast.makeText(this, "Error searching list: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
