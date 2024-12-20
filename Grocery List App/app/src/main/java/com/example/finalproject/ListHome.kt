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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ListHome : AppCompatActivity(), CustomAdapter.RecyclerViewEvent {
    private val TAG = "MYTAG"
    private val db = FirebaseFirestore.getInstance()
    private val groceryList = mutableListOf<GroceryItem>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomAdapter
    private var documentID: String? = null
    private var tokenID: String? = null
    private var totalCost = 0.0
    // 3EPWNM
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
    setContentView(R.layout.list_home)
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
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val selectedTheme = spinnerItems[position]
            if (selectedTheme != savedTheme) {
                // Save theme selection
                sharedPreferences.edit()
                    .putString("theme", selectedTheme)
                    .apply()

                // Restart the activity to apply the new theme
                val intent = Intent(this@ListHome, ListHome::class.java)
                startActivity(intent)
                finish()
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
        // Set up buttons
        val addGroceryButton : Button = findViewById(R.id.addItem)
        val helpButton : Button = findViewById(R.id.helpButton)

        // Retrieve tokenID from shared preferences
        val documentPrefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        documentID = documentPrefs.getString("documentID", null)
        tokenID = documentPrefs.getString("documentID", null)

        if (documentID == null) {
            redirectToAuthenticate()
        }
        recyclerView = findViewById(R.id.recyclerView)
        setupRecyclerView()


        if (documentID != null) {
            // Query Firestore to get the name of the list
            db.collection("lists")
                .document(documentID!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val listName = document.getString("name") // Fetch the 'name' field
                        if (!listName.isNullOrEmpty()) {
                            // Update the title TextView
                            val titleTextView: TextView = findViewById(R.id.titleText)
                            titleTextView.text = "Welcome to the $listName List"
                        } else {
                            Log.w(TAG, "List name is missing")
                        }
                    } else {
                        Log.w(TAG, "Document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching list name", exception)
                }
        }
        // On click listeners
        addGroceryButton.setOnClickListener{
            val intent = Intent(this, AddItem::class.java).apply {
                putExtra("documentID", documentID)
            }
            startActivity(intent)

        }
        // Launch webview with link to google support
        helpButton.setOnClickListener{
            Toast.makeText(this, "Help Button Clicked", Toast.LENGTH_SHORT).show()
            showHelpPage() // Call a function to open the help page

        }
    }

    override fun onResume() {
        super.onResume()

        loadGroceryItems()
    }

    private fun setupRecyclerView() {
        adapter = CustomAdapter(groceryList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    fun loadGroceryItems() {
        Log.e(TAG, "Attempting load grocery items $documentID")
        val groceryItems = mutableListOf<GroceryItem>()

        db.collection("lists")
            .document(documentID.toString()) // Replace with your actual document ID
            .collection("items")
            .orderBy("createdAt", Query.Direction.DESCENDING) // Sort by createdAt in descending order
            .get()
            .addOnSuccessListener { documents ->
                Log.e(TAG, "grocery items ${documents.size()}(")
                totalCost=0.0
                for (document in documents) {
                    Log.e(TAG, document.toString())
                    val name = document.getString("itemName") ?: ""
                    val cost = document.getDouble("cost") ?: 0.0
                    val date = document.getString("buyBefore") ?: ""
                    val quantity = document.getString("quantity") ?: ""
                    val user = document.getString("user") ?: ""
                    totalCost+=cost
                    // Add each item to the list
                    groceryItems.add(GroceryItem(name, cost, date, quantity, user))
                }
                // Update the adapter with the fetched items
                adapter.updateItems(groceryItems)
                // Update total
                findViewById<TextView>(R.id.total).text = String.format("Total: $%.2f", totalCost)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading items", e)
            }
    }

    override fun onItemClick(position: Int) {
        if (position < 0 || position >= groceryList.size) {
            Log.e(TAG, "Invalid position: $position for groceryList size: ${groceryList.size}")
            return // Safeguard against invalid positions
        }

        val selectedItem = groceryList[position]
        val intent = Intent(this, ItemDetails::class.java).apply {
            putExtra("item_name", selectedItem.name)
            putExtra("item_cost", selectedItem.cost)
            putExtra("item_date", selectedItem.date)
            putExtra("item_quantity", selectedItem.quantity)
            putExtra("documentID", documentID)

        }
        startActivity(intent)
    }

    private fun redirectToAuthenticate() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Prevent users from navigating back to ListHome without authentication
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
