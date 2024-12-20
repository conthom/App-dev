package com.example.finalproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import kotlin.math.roundToLong
import java.text.DecimalFormat


data class GroceryItem(
    val name: String = "",
    val cost: Double = 0.0,
    val date: String = "",
    val quantity: String = "",
    val user: String = ""
)

class CustomAdapter(
    private val items: MutableList<GroceryItem>,
    private val listener: RecyclerViewEvent
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    interface RecyclerViewEvent {
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        // Setup views
        val groceryItemTextView: TextView = view.findViewById(R.id.groceryItem)
        val costTextView: TextView = view.findViewById(R.id.cost)
        val dateTextView: TextView = view.findViewById(R.id.date)
        val userNameTextView: TextView = view.findViewById(R.id.userName)

        init {
            view.setOnClickListener(this) // Handle item click
        }

        override fun onClick(v: View?) {
            listener.onItemClick(adapterPosition)
        }
    }
    // Setup with item xml layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }
    // Set actual text values for each item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val groceryItem = items[position]
        val decimalFormat = DecimalFormat("#.00")
        holder.groceryItemTextView.text = groceryItem.name + ", "+ groceryItem.quantity
        holder.costTextView.text = "$" + decimalFormat.format(groceryItem.cost)
        holder.dateTextView.text = groceryItem.date
        holder.userNameTextView.text = groceryItem.user
    }

    override fun getItemCount(): Int = items.size

    // Update recycler view
    fun updateItems(newItems: List<GroceryItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
