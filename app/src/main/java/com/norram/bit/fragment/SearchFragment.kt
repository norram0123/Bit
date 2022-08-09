package com.norram.bit

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.norram.bit.databinding.FragmentSearchBinding
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

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding

    private val requestUrlFormatter = Secret.requestUrlFormatter()
    private var username = ""
    private var usernameTmp = ""
    private var iconUrl = ""
    private var name = ""
    private var afterToken = ""
    private var instaMediaList = ArrayList<InstaMedia>()
    private var favoriteFlag = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // prevent UI error
        binding.profileConstraint.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.addButton.visibility = View.GONE

        val args: SearchFragmentArgs by navArgs()
        binding.searchView.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        binding.searchView.setQuery(args.username, false)
        username = args.username
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    username = it
                    resetData()
                    getMediaInfo()
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { usernameTmp = it }
                return false
            }
        })

        binding.searchButton.setOnClickListener {
            //clear focus
            binding.searchView.clearFocus()
            val inputMethodManager
                    = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)

            if(usernameTmp == "") return@setOnClickListener
            username = usernameTmp
            resetData()
            getMediaInfo()
        }

        val screen = Screen.getInstance()
        binding.searchCard.radius = (screen.width / 6).toFloat()

        binding.favoriteImageView.setOnClickListener {
            val helper = FavoriteOpenHelper(requireContext())
            helper.writableDatabase.use { db ->
                favoriteFlag = if(favoriteFlag) {
                    db.execSQL("DELETE FROM FAVORITE_TABLE WHERE name = '$username'")
                    binding.favoriteImageView.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                    false
                } else {
                    db.execSQL("INSERT INTO FAVORITE_TABLE(url, name) " +
                            "VALUES('$iconUrl', '$username')")
                    binding.favoriteImageView.setImageResource(R.drawable.ic_baseline_favorite_pink_24)
                    val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.touch_favorite)
                    it.startAnimation(animation)
                    true
                }
            }
        }

        binding.recyclerView.layoutManager =
            GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
        getMediaInfo()

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.options_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId) {
//            R.id.help -> { val dialogFragment = HelpDialogFragment() dialogFragment.show(supportFragmentManager,  "help_dialog") true }
                    R.id.expandAll -> {
                        binding.recyclerView.adapter?.let { adapter ->
                            for(i in adapter.itemCount downTo 0) {
                                binding.recyclerView.findViewHolderForAdapterPosition(i)?.let {
                                    val holder = it as SearchAdapter.ViewHolder
                                    if(holder.isExpanded) holder.expand.performClick()
                                }
                            }
                        }
                        true
                    }

                    else -> true
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun getMediaInfo() {
        cutURL()
        val requestUrl = String.format(requestUrlFormatter, username, afterToken)
        val url = URL(requestUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        val connectivityService =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityService.getNetworkCapabilities(connectivityService.activeNetwork) ?: run {
                Toast.makeText(requireContext(),
                    resources.getString(R.string.error0), Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            @Suppress("DEPRECATION")
            connectivityService.activeNetworkInfo ?: run {
                Toast.makeText(requireContext(),
                    resources.getString(R.string.error0), Toast.LENGTH_SHORT).show()
                return
            }
        }

        getImages(connection)
    }

    private fun getImages(connection: HttpURLConnection) {
        runCatching {
            CoroutineScope(Dispatchers.IO).launch {
                val mContext: Context = context ?: return@launch
                connection.errorStream?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(mContext,
                            resources.getString(R.string.error1), Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonObj = JSONObject(bufferedReader.readText())
                val bdJSON = jsonObj.getJSONObject("business_discovery")
                iconUrl = if(bdJSON.has("profile_picture_url")) bdJSON.getString("profile_picture_url") else ""
                name = if(bdJSON.has("name")) bdJSON.getString("name") else ""
                val mediaJSON = bdJSON.getJSONObject("media")
                val mediaArray = mediaJSON.getJSONArray("data")

                for (i in 0 until mediaArray.length()) {
                    var permalink = ""
                    val mediaData = mediaArray.getJSONObject(i)
                    val childrenUrls = ArrayList<String>()
                    if(mediaData.has("permalink"))
                        permalink = mediaData.getString("permalink")
                    if (mediaData.getString("media_type") == "CAROUSEL_ALBUM"
                        && mediaData.has("children")) {
                        val childrenDataArray =
                            mediaData.getJSONObject("children").getJSONArray("data")
                        for (j in 1 until childrenDataArray.length()) {
                            val childrenData = childrenDataArray.getJSONObject(j)
                            if (childrenData.getString("media_type") == "IMAGE")
                                childrenUrls.add(childrenData.getString("media_url"))
                        }
                    }
                    if (mediaData.getString("media_type") != "VIDEO"
                        && mediaData.has("media_url")
                        && mediaData.has("media_type"))
                        instaMediaList.add(
                            InstaMedia(
                                mediaData.getString("media_url"),
                                permalink,
                                mediaData.getString("media_type"),
                                childrenUrls,
                                true
                            )
                        )
                }

                val helper = HistoryOpenHelper(mContext)
                helper.writableDatabase.use { db ->
                    db.execSQL("INSERT INTO HISTORY_TABLE(url, name) " +
                            "VALUES('$iconUrl', '$username')")
                }

                binding.addButton.setOnClickListener {
                    if(mediaJSON.has("paging")) {
                        val cursorsJSON = mediaJSON.getJSONObject("paging").getJSONObject("cursors")
                        if(cursorsJSON.has("after")) {
                            afterToken = ".after(" + cursorsJSON.getString("after") + ")"
                            getMediaInfo()
                            return@setOnClickListener
                        }
                    }
                    Toast.makeText(mContext,
                        resources.getString(R.string.finish), Toast.LENGTH_SHORT).show()
                }

                withContext(Dispatchers.Main) {
                    val screen = Screen.getInstance()
                    if (afterToken == "") {
                        binding.nestedScrollView.fullScroll(ScrollView.FOCUS_UP) // return to the top
                        binding.usernameText.text = name
                        if(iconUrl != "") {
                            Picasso.get()
                                .load(iconUrl)
                                .resize(screen.width / 3, screen.width / 3)
                                .centerCrop() // trim from the center
                                .into(binding.iconImageView)
                        }
                    }

                    binding.recyclerView.adapter = SearchAdapter(
                        mContext,
                        instaMediaList,
                        binding.searchView
                    )

                    checkFavorite(mContext)
                    // prevent UI error
                    binding.profileConstraint.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.addButton.visibility = View.VISIBLE
                }
            }
        }.fold(
            onSuccess = {},
            onFailure = {Toast.makeText(requireContext(),
                resources.getString(R.string.error2), Toast.LENGTH_LONG).show() }
        ).also { connection.disconnect() }
    }

    private fun cutURL() {
        val prefix = "instagram.com/"
        if(username.indexOf(prefix) == -1) return
        val preIdx = username.indexOf(prefix) + prefix.length
        username = username.substring(preIdx, username.length-1)
        var sufIdx = username.indexOf("?")
        if(sufIdx == -1) sufIdx = username.indexOf("/")
        if(sufIdx == -1) return
        username = username.substring(0, sufIdx)
    }

    private fun resetData() {
        afterToken = ""
        instaMediaList = ArrayList()
    }

    private fun checkFavorite(context: Context) {
        val helper = FavoriteOpenHelper(context)
        helper.writableDatabase.use { db ->
            db.rawQuery("SELECT name FROM FAVORITE_TABLE ORDER BY id DESC", null).use { c ->
                var next = c.moveToFirst() // check cursor has first row or not
                while (next) {
                    val name = c.getString(0)
                    if(name.equals(username)) {
                        binding.favoriteImageView.setImageResource(R.drawable.ic_baseline_favorite_pink_24)
                        favoriteFlag = true
                        return
                    }
                    next = c.moveToNext() // check cursor has first row or not
                }
                binding.favoriteImageView.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                favoriteFlag = false
            }
        }
    }
}