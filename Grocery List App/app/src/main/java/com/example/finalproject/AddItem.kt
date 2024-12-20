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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AddItem : AppCompatActivity() {
    private val TAG = "MYTAG"
    private val db = FirebaseFirestore.getInstance()
    lateinit var nameInput: EditText
    lateinit var costInput: EditText
    lateinit var quantityInput: EditText
    private lateinit var documentID: String

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
        setContentView(R.layout.add_item)

        // Initialize views
        nameInput = findViewById(R.id.itemNameInput)
        costInput = findViewById(R.id.itemCostInput)
        quantityInput = findViewById(R.id.itemQuantityInput)
        val datePickerButton: Button = findViewById(R.id.datePickerButton)
        val helpButton : Button = findViewById(R.id.helpButton)
        // Set up fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.navigationFragment, MyFragment())
            .commit()

        intent?.let {
            documentID = it.getStringExtra("documentID") ?: ""
            val itemName = it.getStringExtra("item_name") ?: ""
            val itemCost = it.getDoubleExtra("item_cost", 0.0)
            val itemDate = it.getStringExtra("item_date") ?: "Select Date"
            val itemQuantity = it.getStringExtra("item_quantity") ?: ""

            // Set values to views
            nameInput.setText(itemName)
            costInput.setText(if (itemCost == 0.0) "" else itemCost.toString())
            quantityInput.setText(itemQuantity)
            datePickerButton.text = itemDate
        }

        // Set up theme spinner
        val themeSpinner: Spinner = findViewById(R.id.themeSpinner)
        val spinnerItems = arrayOf("Default Theme", "Light Theme", "Dark Theme")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, spinnerItems)
        themeSpinner.adapter = adapter

        // Set initial selection in spinner based on saved theme
        val spinnerPosition = spinnerItems.indexOf(savedTheme)
        themeSpinner.setSelection(spinnerPosition, false) // Set initial selection without triggering listener

        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedTheme = spinnerItems[position]
                if (selectedTheme != savedTheme) {
                    // Save theme selection
                    sharedPreferences.edit()
                        .putString("theme", selectedTheme)
                        .apply()

                    // Restart the activity to apply the new theme
                    val intent = Intent(this@AddItem, AddItem::class.java).apply {
                        putExtra("documentID", documentID)
                        putExtra("item_name", nameInput.text.toString().trim())
                        putExtra("item_cost", costInput.text.toString().trim().toDoubleOrNull() ?: 0.0)
                        putExtra("item_date", findViewById<Button>(R.id.datePickerButton).text.toString().trim())
                        putExtra("item_quantity", quantityInput.text.toString().trim())
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Retrieve documentID from Intent
        documentID = intent.getStringExtra("documentID").toString()
        // Set up buttons

        // Code for setting date in date picker
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
        // Save button on click listener
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            // Gather inputs
            val itemName = nameInput.text.toString().trim()
            val totalCost = costInput.text.toString().trim().toDoubleOrNull() ?: 0.0
            val quantity = quantityInput.text.toString().trim()
            val buyBeforeDate = datePickerButton.text.toString().trim()
            val savedName = sharedPreferences.getString("username", "")

            if (buyBeforeDate == "Select Date" || totalCost.isNaN() || itemName.isEmpty() || quantity.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save item to Firestore
            val itemData = mapOf(
                "itemName" to itemName,
                "buyBefore" to buyBeforeDate,
                "cost" to totalCost,
                "quantity" to quantity,
                "user" to savedName,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp() // Add server timestamp
            )

            db.collection("lists")
                .document(documentID)
                .collection("items")
                .add(itemData) // Add new item to the collection
                .addOnSuccessListener {
                    Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error adding item", Toast.LENGTH_SHORT).show()
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
