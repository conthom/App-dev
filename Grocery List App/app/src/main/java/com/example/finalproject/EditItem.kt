package com.example.finalproject

import MyFragment
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class EditItem : AppCompatActivity() {
    private val TAG = "MYTAG"
    lateinit var nameInput: EditText
    lateinit var costInput: EditText
    lateinit var quantityInput: EditText
    lateinit var datePickerButton: Button
    private lateinit var documentID: String
    private lateinit var itemName: String
    private var itemCost: Double = 0.0
    private lateinit var itemDate: String
    private lateinit var itemQuantity: String

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
        setContentView(R.layout.edit_item)

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
                    val intent = Intent(this@EditItem, EditItem::class.java).apply {
                        putExtra("item_name", nameInput.text.toString().trim())
                        putExtra("item_cost", costInput.text.toString().trim().toDoubleOrNull() ?: 0.0)
                        putExtra("item_date", datePickerButton.text.toString().trim())
                        putExtra("item_quantity", quantityInput.text.toString().trim())
                        putExtra("documentID",documentID)
                    }
                    startActivity(intent)
                    finish()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Initialize views
        nameInput = findViewById(R.id.itemNameInput)
        costInput = findViewById(R.id.itemCostInput)
        quantityInput = findViewById(R.id.itemQuantityInput)
        datePickerButton = findViewById(R.id.datePickerButton)
        val helpButton : Button = findViewById(R.id.helpButton)

        // Retrieve data from Intent
        documentID = intent.getStringExtra("documentID") ?: ""
        itemName = intent.getStringExtra("item_name") ?: ""
        itemCost = intent.getDoubleExtra("item_cost", 0.0)
        itemDate = intent.getStringExtra("item_date") ?: "Select Date"
        itemQuantity = intent.getStringExtra("item_quantity") ?: ""

        // Populate fields with existing data
        nameInput.setText(itemName)
        costInput.setText(itemCost.toString())
        quantityInput.setText(itemQuantity)
        datePickerButton.text = itemDate

        // Date Picker functionality
        datePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "${selectedYear}-${selectedMonth + 1}-${selectedDay}"
                    datePickerButton.text = formattedDate
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // Save Button functionality
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            val updatedName = nameInput.text.toString().trim()
            val updatedCost = costInput.text.toString().trim().toDoubleOrNull() ?: 0.0
            val updatedQuantity = quantityInput.text.toString().trim()
            val updatedDate = datePickerButton.text.toString().trim()
            val savedName = sharedPreferences.getString("username", "")

            if (updatedName.isEmpty() || updatedCost == 0.0 || updatedQuantity.isEmpty() || updatedDate == "Select Date") {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (documentID.isEmpty()) {
                Log.e(TAG, "Invalid documentID: $documentID")
                Toast.makeText(this, "Error: Document ID is invalid.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedItemData = mapOf(
                "itemName" to updatedName,
                "buyBefore" to updatedDate,
                "cost" to updatedCost,
                "quantity" to updatedQuantity,
                "user" to savedName,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            val db = FirebaseFirestore.getInstance()
            db.collection("lists")
                .document(documentID) // Ensure documentID is valid
                .collection("items")
                .whereEqualTo("itemName", itemName)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        document.reference.update(updatedItemData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, ListHome::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error updating item", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, e.toString())
                            }
                    } else {
                        Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error finding item", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, e.toString())
                }
        }


        // Launch webview with link to google support
        helpButton.setOnClickListener{
            Toast.makeText(this, "Help Button Clicked", Toast.LENGTH_SHORT).show()
            showHelpPage() // Call a function to open the help page

        }
    }
    private fun showHelpPage() {
        val webView = WebView(this)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true // Enable JavaScript if needed
        webView.loadUrl("https://support.google.com/android/#topic=7313011")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Help")
            .setView(webView)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()
    }
}
