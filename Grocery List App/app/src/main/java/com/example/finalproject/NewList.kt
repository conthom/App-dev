package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class NewList : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load saved theme
        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val savedTheme = sharedPreferences.getString("theme", "Default") ?: "Default"

        // Apply theme before setting content view
        when (savedTheme) {
            "Default Theme" -> setTheme(R.style.Theme_FinalProject)
            "Light Theme" -> setTheme(R.style.Theme_App_Light)
            "Dark Theme" -> setTheme(R.style.Theme_App_Dark)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_list)

        val saveButton: Button = findViewById(R.id.saveButton)
        val listNameEditText: EditText = findViewById(R.id.listNameInput)

        saveButton.setOnClickListener {
            val listName = listNameEditText.text.toString()
            if (listName.isEmpty()) {
                Toast.makeText(this, "Please enter a list name", Toast.LENGTH_SHORT).show()
            } else {
                createGroceryList(listName)
            }
        }
    }
    private fun createGroceryList(listName: String) {
        // Generate a short random token (e.g., 6 alphanumeric characters)
        val token = (1..6)
            .map { Random.nextInt(0, 36).toString(36) }
            .joinToString("")
            .uppercase()

        val groceryList = hashMapOf(
            "name" to listName,
            "token" to token,
        )

        db.collection("lists")
            .add(groceryList)
            .addOnSuccessListener { documentReference ->
                val documentID = documentReference.id // Retrieve the document ID of the new list

                // Save token and documentID to shared preferences
                val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("tokenID", token)
                    putString("documentID", documentID)
                    apply()
                }

                Toast.makeText(this, "List created! List Token: $token", Toast.LENGTH_LONG).show()

                // Redirect to ListHome with documentID
                val intent = Intent(this, ListHome::class.java).apply {
                    putExtra("documentID", documentID)
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error creating list: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
