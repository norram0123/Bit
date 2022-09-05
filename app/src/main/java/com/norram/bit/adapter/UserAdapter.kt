package com.norram.bit

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UserAdapter(
    private val activity: FragmentActivity?,
    private val userList: ArrayList<HashMap<String, String>>,
    private val tableName: String
    ): RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    private val split = 4

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.itemUserCard)
        val image: ImageView = view.findViewById(R.id.itemUserImage)
        val text: TextView = view.findViewById(R.id.itemUserText)
        val liner: LinearLayout = view.findViewById(R.id.itemUserLinear)
        val favorite: ImageView = view.findViewById(R.id.itemFavoriteImage)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item_user, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val screen = Screen.getInstance()
        val user = userList[position]
        holder.image.layoutParams.width = screen.width / split
        holder.image.layoutParams.height = screen.width / split
        holder.card.radius = (screen.width / (split * 2)).toFloat()
        Picasso.get()
            .load(user["url"])
            .resize(screen.width / split, screen.width / split)
            .centerCrop() // trim from the center
            .into(holder.image, object : Callback {
                override fun onSuccess() {}

                override fun onError(e: Exception?) {
                    updateUrl(user["name"], holder.image)
                }
            })
        holder.text.text = user["name"]
        if(user["isFavorite"] == "true") holder.favorite.visibility = View.VISIBLE

        holder.liner.setOnClickListener { view ->
            user["name"]?.let {
                val action = ModeFragmentDirections.actionModeFragmentToSearchFragment(it)
                view.findNavController().navigate(action)
            }
        }
        holder.liner.setOnLongClickListener {
            val dialogFragment = DeleteDialogFragment(this, userList, position)
            activity?.let {
                if(tableName != "FAVORITE_TABLE") dialogFragment.show(it.supportFragmentManager,  "delete_dialog")
            }
            true // choose whether to interfere with setOnClickListener
        }
    }

    override fun getItemCount() = userList.size

    private fun updateUrl(name: String?, imageView: ImageView) {
        val mContext: Context = activity?.baseContext ?: return
        val requestUrl = String.format(Secret.requestUrlFormatter(), name, "")
        val url = URL(requestUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        val connectivityService = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityService.getNetworkCapabilities(connectivityService.activeNetwork) ?: return
        } else {
            @Suppress("DEPRECATION")
            connectivityService.activeNetworkInfo ?: return
        }

        runCatching {
            CoroutineScope(Dispatchers.IO).launch {
                connection.errorStream?.let { return@launch }

                val screen = Screen.getInstance()
                val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonObj = JSONObject(bufferedReader.readText())
                val bdJSON = jsonObj.getJSONObject("business_discovery")
                val iconUrl =
                    if(bdJSON.has("profile_picture_url"))
                        bdJSON.getString("profile_picture_url")
                    else ""

                val helper = when(tableName) {
                    "HISTORY_TABLE" -> {
                        HistoryOpenHelper(mContext)
                    }
                    else -> {
                        FavoriteOpenHelper(mContext)
                    }
                }
                helper.writableDatabase.use { db ->
                    db.execSQL("UPDATE '$tableName' SET url = '$iconUrl' WHERE name = '$name'")
                }
                withContext(Dispatchers.Main) {
                    Picasso.get()
                        .load(iconUrl)
                        .resize(screen.width / split, screen.width / split)
                        .centerCrop() // trim from the center
                        .into(imageView)
                }
            }
        }.fold(
            onSuccess = {},
            onFailure = {}
        ).also { connection.disconnect() }
    }
}
