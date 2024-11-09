import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project6.R

class CustomAdapter(
    private var dataSet: Array<String>,
    private val listener: RecyclerViewEvent
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    interface RecyclerViewEvent {
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val textView: TextView = view.findViewById(R.id.newsTitle)

        init {
            view.setOnClickListener(this)  // Set the click listener for the item view
        }

        override fun onClick(view: View) {
            listener.onItemClick(adapterPosition)  // Notify the listener with the position
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_layout, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = dataSet[position]
    }

    fun updateItems(newItems: List<String>) {
        dataSet = newItems.toTypedArray()
        notifyDataSetChanged()
    }

    override fun getItemCount() = dataSet.size
}