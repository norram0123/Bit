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

        val helper = HistoryOpenHelper(requireContext())
        val historyList = ArrayList<HashMap<String, String>>()
        val db = helper.writableDatabase
        val c = db.rawQuery("select distinct url, name from HISTORY_TABLE order by id DESC", null)
        try {
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
        } finally {
            db.close()
            c.close()
        }

        // use viewTreeObserver to get accurate width
        binding.historyLinear.viewTreeObserver.addOnGlobalLayoutListener (object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.recyclerView.adapter = HistoryAdapter(
                    historyList,
                    binding.historyLinear.width
                )
                binding.historyLinear.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
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