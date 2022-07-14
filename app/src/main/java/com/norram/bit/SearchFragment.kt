package com.norram.bit

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
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
    private val requestUrlFormatter = Secret.requestUrlFormatter()

    private lateinit var binding: FragmentSearchBinding
    private var username = ""
    private var iconUrl = ""
    private var name = ""
    private var afterToken = ""
    private var instaMediaList = ArrayList<InstaMedia>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        binding.searchView.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    username = it
                    dataReset()
                    getMediaInfo()
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { username = it }
                return false
            }
        })

        binding.searchButton.setOnClickListener {
            //clear focus
            binding.searchView.clearFocus()
            val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)

            dataReset()
            getMediaInfo()
        }

        getMediaInfo() // tmp
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.options_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId) {
//            R.id.help -> { val dialogFragment = HelpDialogFragment() dialogFragment.show(supportFragmentManager,  "help_dialog") true }
                    R.id.expandAll -> {
                        binding.recyclerView.adapter?.let { it1 ->
                            for(i in it1.itemCount downTo 0) {
                                binding.recyclerView.findViewHolderForAdapterPosition(i)?.let { it2 ->
                                    val holder = it2 as CustomAdapter.ViewHolder
                                    if(holder.expand.isVisible && holder.isExpanded) holder.expand.performClick()
                                }}}
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun getMediaInfo() {
        val requestUrl = String.format(requestUrlFormatter, username, afterToken)
        val url = URL(requestUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        val connectivityService = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityService.getNetworkCapabilities(connectivityService.activeNetwork) ?: run {
            Toast.makeText(requireContext(), resources.getString(R.string.error0), Toast.LENGTH_SHORT).show()
            return
        }

        getImages(connection)
    }

    private fun getImages(connection: HttpURLConnection) {
        var isNormal = true // determine whether url is correct
        try {
            CoroutineScope(Dispatchers.IO).launch {
                if (connection.errorStream != null) {
                    isNormal = false
                } else {
                    val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                    val jsonObj = JSONObject(bufferedReader.readText())
                    val bdJSON = jsonObj.getJSONObject("business_discovery")
                    iconUrl = bdJSON.getString("profile_picture_url")
                    name = if(bdJSON.has("name")) bdJSON.getString("name") else ""
                    val mediaJSON = bdJSON.getJSONObject("media")
                    val mediaArray = mediaJSON.getJSONArray("data")
                    val cursorsJSON = mediaJSON.getJSONObject("paging").getJSONObject("cursors")

                    for (i in 0 until mediaArray.length()) {
                        val mediaData = mediaArray.getJSONObject(i)
                        val childrenUrls = ArrayList<String>()
                        if (mediaData.getString("media_type") == "CAROUSEL_ALBUM") {
                            val childrenDataArray =
                                mediaData.getJSONObject("children").getJSONArray("data")
                            for (j in 1 until childrenDataArray.length()) {
                                val childrenData = childrenDataArray.getJSONObject(j)
                                if (childrenData.getString("media_type") == "IMAGE")
                                    childrenUrls.add(childrenData.getString("media_url"))
                            }
                        }
                        if (mediaData.getString("media_type") != "VIDEO")
                            instaMediaList.add(
                                InstaMedia(
                                    mediaData.getString("media_url"),
                                    mediaData.getString("media_type"),
                                    childrenUrls,
                                    true
                                )
                            )
                    }

                    binding.addButton.setOnClickListener {
                        if (cursorsJSON.has("after")) {
                            afterToken = ".after(" + cursorsJSON.getString("after") + ")"
                            getMediaInfo()
                        } else {
                            Toast.makeText(requireContext(), resources.getString(R.string.finish), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (!isNormal) Toast.makeText(requireContext(), resources.getString(R.string.error1), Toast.LENGTH_SHORT).show()
                    else {
                        if(afterToken == "") {
                            binding.nestedScrollView.fullScroll(ScrollView.FOCUS_UP) // return to the top
                            Picasso.get()
                                .load(iconUrl)
                                .resize(binding.mainLinear.width / 3, binding.mainLinear.width / 3) // 表示範囲の指定
                                .centerCrop() // trim from the center
                                .into(binding.iconImageView)
                            if(name == "") binding.usernameText.visibility = View.GONE
                            else {
                                binding.usernameText.visibility = View.VISIBLE
                                binding.usernameText.text = name
                            }
                        }
                        binding.recyclerView.adapter = CustomAdapter(requireContext(), instaMediaList, binding.mainLinear.width, binding.searchView)
                        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
                    }
                }
            }
        } catch(e: Exception) {
            Toast.makeText(requireContext(), resources.getString(R.string.error2), Toast.LENGTH_LONG).show()
        } finally { connection.disconnect() }
    }

    private fun dataReset() {
        afterToken = ""
        instaMediaList = ArrayList()
    }
}