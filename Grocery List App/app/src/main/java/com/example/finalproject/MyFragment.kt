import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.finalproject.ListHome
import com.example.finalproject.MainActivity
import com.example.finalproject.R

class MyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_navigation, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons
        val homeButton: Button = view.findViewById(R.id.homeButton)
        val logoutButton: Button = view.findViewById(R.id.logoutButton)

        // Set click listeners for buttons
        homeButton.setOnClickListener {
            // Navigate to home
            val intent = Intent(activity, ListHome::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            // Perform logout action
            // This could include clearing session data, navigating to a login screen, etc.
            val sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("userToken")
                .apply()
            // Pass the theme as an extra to the intent
            val selectedTheme = sharedPreferences.getString("theme", "Default Theme") ?: "Default Theme"
            val intent = Intent(activity, MainActivity::class.java).apply {
                putExtra("theme", selectedTheme)
            }
            startActivity(intent)
            activity?.finish()
        }
    }
}
