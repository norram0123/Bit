package com.norram.bit

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.norram.bit.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        binding.recyclerView.layoutManager =
            GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)

        val helper = HistoryOpenHelper(requireContext())
        val historyList = ArrayList<HashMap<String, String>>()
        helper.writableDatabase.use { db ->
            db.execSQL("CREATE TEMPORARY TABLE HISTORY_TMP AS SELECT MAX(id), url, name FROM HISTORY_TABLE GROUP BY name")
            db.execSQL("DELETE FROM HISTORY_TABLE")
            db.execSQL("INSERT INTO HISTORY_TABLE SELECT * FROM HISTORY_TMP")
            db.rawQuery(
                "SELECT url, name FROM HISTORY_TABLE ORDER BY id DESC", null
            ).use { c ->
                var next = c.moveToFirst() // check cursor has first row or not
                // get all rows
                while (next) {
                    val data = HashMap<String, String>()
                    val url = c.getString(0)
                    val name = c.getString(1)
                    data["url"] = url
                    data["name"] = name
                    historyList.add(data)
                    next = c.moveToNext() // check cursor has first row or not
                }
            }
        }

        val screen = Screen.getInstance()
        if(!screen.isMeasured) {
            screen.isMeasured = true
            // use ViewTreeObserver to get accurate width
            binding.historyLinear.viewTreeObserver.addOnGlobalLayoutListener (object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    screen.width = binding.historyLinear.width
                    binding.recyclerView.adapter = UserAdapter(activity, historyList, "HISTORY_TABLE")
                    binding.historyLinear.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        } else { binding.recyclerView.adapter = UserAdapter(activity, historyList, "HISTORY_TABLE") }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val action = ModeFragmentDirections.actionModeFragmentToSearchFragment(it)
                    findNavController().navigate(action)
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean { return false }
        })

        binding.searchButton.setOnClickListener {
            val username = binding.searchView.query.toString()
            if(username != "") {
                val action = ModeFragmentDirections.actionModeFragmentToSearchFragment(username)
                findNavController().navigate(action)
            }
        }

    }
}