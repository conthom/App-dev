package com.example.finalproject

import MyFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class ItemDetails : AppCompatActivity() {
    private val TAG = "MYTAG"
    private val db = FirebaseFirestore.getInstance()
    private var documentID: String? = null
    private var itemName: String? = null
    private var itemCost: Double? = null
    private var  itemDate: String? = null
    private var itemQuantity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load saved theme
        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val savedTheme = sharedPreferences.getString("theme", "Default Theme") ?: "Default Theme"

        // Apply theme before setting content view
        when (savedTheme) {
            "Default Theme" -> setTheme(R.style.Theme_FinalProject)
            "Light Theme" -> setTheme(R.style.Theme_App_Light)
            "Dark Theme" -> setTheme(R.style.Theme_App_Dark)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_details)

        // Set up fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.navigationFragment, MyFragment())
            .commit()

        // Set up theme spinner
        val themeSpinner: Spinner = findViewById(R.id.themeSpinner)
        val spinnerItems = arrayOf("Default Theme", "Light Theme", "Dark Theme")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, spinnerItems)
        themeSpinner.adapter = adapter

        // Set initial selection in spinner based on saved theme
        val spinnerPosition = spinnerItems.indexOf(savedTheme)
        themeSpinner.setSelection(spinnerPosition, false) // Set initial selection without triggering listener

        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedTheme = spinnerItems[position]
                if (selectedTheme != savedTheme) {
                    // Save theme selection
                    sharedPreferences.edit()
                        .putString("theme", selectedTheme)
                        .apply()

                    // Restart the activity to apply the new theme
                    val intent = Intent(this@ItemDetails, ItemDetails::class.java).apply {
                        putExtra("item_name", itemName)
                        putExtra("item_cost", itemCost)
                        putExtra("item_date", itemDate)
                        putExtra("item_quantity", itemQuantity)
                        putExtra("documentID",documentID)

                    }
                    startActivity(intent)
                    finish()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Retrieve item details from the intent
        val intent = intent
        itemName = intent.getStringExtra("item_name")
        itemCost = intent.getDoubleExtra("item_cost", 0.0)
        itemDate = intent.getStringExtra("item_date")
        itemQuantity = intent.getStringExtra("item_quantity")
        documentID = intent.getStringExtra("documentID")

        // Populate UI elements
        findViewById<TextView>(R.id.titleText).text = itemName
        findViewById<TextView>(R.id.itemCost).text = String.format("$%.2f", itemCost)
        findViewById<TextView>(R.id.buyBeforeDate).text = itemDate
        findViewById<TextView>(R.id.itemQuantity).text = itemQuantity

        // Remove Button functionality
        findViewById<Button>(R.id.removeButton).setOnClickListener {
            removeItem()
        }

        // Edit Button functionality
        findViewById<Button>(R.id.editButton).setOnClickListener {
            val editIntent = Intent(this, EditItem::class.java).apply {
                putExtra("item_name", itemName)
                putExtra("item_cost", itemCost)
                putExtra("item_date", itemDate)
                putExtra("item_quantity", itemQuantity)
                putExtra("documentID", documentID)
            }
            startActivity(editIntent)
        }

        val helpButton: Button = findViewById(R.id.helpButton)
        // Launch webview with link to google support
        helpButton.setOnClickListener {
            Toast.makeText(this, "Help Button Clicked", Toast.LENGTH_SHORT).show()
            showHelpPage()
        }
    }

    private fun removeItem() {
        Log.e(TAG, documentID.toString())
        if (documentID.isNullOrEmpty() || itemName.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Missing required information.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("lists")
            .document(documentID!!)
            .collection("items")
            .whereEqualTo("itemName", itemName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        document.reference.delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Item removed successfully.", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error removing item", e)
                                Toast.makeText(this, "Failed to remove item.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Item not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching item to remove", e)
                Toast.makeText(this, "Error fetching item to remove.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showHelpPage() {
        val webView = WebView(this)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://support.google.com/android/#topic=7313011")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Help")
            .setView(webView)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()
    }
}