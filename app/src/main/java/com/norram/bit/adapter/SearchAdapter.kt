package com.norram.bit

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso


class SearchAdapter(
    private val context: Context,
    private val instaMediaList: ArrayList<InstaMedia>,
    private val searchView: SearchView
    ): RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    private val spanCount = 3

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.itemSearchImage)
        val expand: Button = view.findViewById(R.id.itemSearchButton)
        var isExpanded = false
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item_search, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val screen = Screen.getInstance()
        val instaMedia = instaMediaList[position]
        holder.isExpanded = instaMedia.flag
        Picasso.get()
            .load(instaMedia.url)
            .resize(screen.width / spanCount, screen.width / spanCount)
            .centerCrop() // trim from the center
            .into(holder.image)

        if(instaMedia.flag) {
            holder.expand.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_baseline_drag_indicator_36_white, 0, 0)
        } else {
            holder.expand.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_baseline_drag_indicator_36_orange, 0, 0)
        }
        if(instaMedia.type == "CAROUSEL_ALBUM") {
            holder.expand.visibility = View.VISIBLE
            holder.expand.setOnClickListener {
                if(instaMedia.flag) {
                    instaMedia.flag = false
                    expandAlbum(position, instaMedia)
                } else {
                    instaMedia.flag = true
                    closeAlbum(position, instaMedia)
                }
            }
        } else {
            holder.expand.visibility = View.INVISIBLE
        }

        holder.image.setOnClickListener {
            //clear focus
            searchView.clearFocus()
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)

            val intent = Intent(context, ViewerActivity::class.java)
            intent.putExtra("IMAGE_URL", instaMedia.url)
            intent.putExtra("IMAGE_PERMALINK", instaMedia.permalink)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = instaMediaList.size

    private fun expandAlbum(position: Int, instaMedia: InstaMedia) {
        for(i in 0 until instaMedia.childrenUrls.size) {
            instaMediaList.add(position+1 + i,
                InstaMedia(instaMedia.childrenUrls[i],
                    instaMedia.permalink,
                    "IMAGE",
                    arrayListOf(""),
                    false)
            )
        }
        notifyItemRangeInserted(position+1, instaMedia.childrenUrls.size)
        notifyItemRangeChanged(position, instaMediaList.size - position)
    }

    private fun closeAlbum(position: Int, instaMedia: InstaMedia) {
        for(i in 0 until instaMedia.childrenUrls.size) {
            instaMediaList.removeAt(position + 1)
        }
        notifyItemRangeRemoved(position+1, instaMedia.childrenUrls.size)
        notifyItemRangeChanged(position, instaMediaList.size - position)
    }
}