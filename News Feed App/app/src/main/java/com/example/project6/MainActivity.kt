package com.example.project6

import CustomAdapter
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var customAdapter: CustomAdapter
    private val titleList = mutableListOf<String>()
    private lateinit var webHelper: WebHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()

        // Initialize WebHelper and fetch article titles
        webHelper = WebHelper()
        webHelper.initialize(this)
        loadNews()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        customAdapter =
            CustomAdapter(titleList.toTypedArray(), object : CustomAdapter.RecyclerViewEvent {
                override fun onItemClick(position: Int) {
                    // Handle item click here
                }
            })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = customAdapter
    }

    private fun loadNews() {
        val url =
            "https://newsapi.org/v2/everything?q=Android&apiKey=cf16afcde2ee4280ae5ded1c2b82cb62"

        webHelper.fetchArticleTitle(url) { titleBytes ->
            if (titleBytes != null) {
                val responseString = String(titleBytes)
                Log.d("MainActivity", "API Response: $responseString")

                try {
                    // Parse the response into NewsResponse object
                    val newsResponse: NewsResponse =
                        Gson().fromJson(responseString, NewsResponse::class.java)

                    // Update the UI with article titles, filtering out titles with [Removed]
                    runOnUiThread {
                        titleList.clear()
                        titleList.addAll(newsResponse.articles
                            .map { it.title }
                            .filter { !it.contains("[Removed]", ignoreCase = true) } // Filter out titles containing [Removed]
                        )
                        customAdapter.updateItems(titleList)
                    }
            } catch (e: Exception) {
            Log.e("MainActivity", "Failed to parse title", e)
        }
        } else {
            Log.e("MainActivity", "Failed to fetch titles")
            }
        }
    }
    // Data classes for parsing JSON response
    data class NewsResponse(
        val status: String,
        val totalResults: Int,
        val articles: List<Article>
    )

    data class Article(
        @SerializedName("title") val title: String
    )
}