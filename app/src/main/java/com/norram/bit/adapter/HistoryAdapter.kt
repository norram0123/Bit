package com.norram.bit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class HistoryAdapter(
    private val activity: FragmentActivity?,
    private val historyList: ArrayList<HashMap<String, String>>
    ): RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.itemHistoryCard)
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
        holder.card.radius = (screen.width / 8).toFloat()
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
        holder.liner.setOnLongClickListener {
            val dialogFragment = DeleteDialogFragment(this, historyList, position)
            activity?.let { dialogFragment.show(it.supportFragmentManager,  "help_dialog") }
            true // choose whether to interfere with setOnClickListener
        }
    }

    override fun getItemCount() = historyList.size
}
