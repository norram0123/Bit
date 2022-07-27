package com.norram.bit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class HistoryAdapter(
    private val historyList: ArrayList<HashMap<String, String>>): RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.itemHistoryImage)
        val text: TextView = view.findViewById(R.id.itemHistoryText)
        val liner: LinearLayout = view.findViewById(R.id.itemHistoryLinear)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item_history, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val screen = Screen.getInstance()
        val history = historyList[position]
        Picasso.get()
            .load(history["url"])
            .resize(screen.width / 4, screen.width / 4)
            .centerCrop() // trim from the center
            .into(holder.image)
        holder.text.text = history["name"]

        holder.liner.setOnClickListener { view ->
            history["name"]?.let {
                val action = ModeFragmentDirections.actionModeFragmentToSearchFragment(it)
                view.findNavController().navigate(action)
            }
        }
    }

    override fun getItemCount() = historyList.size
}
