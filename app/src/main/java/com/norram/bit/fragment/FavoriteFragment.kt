package com.norram.bit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.norram.bit.databinding.FragmentFavoriteBinding

class FavoriteFragment : Fragment() {
    private lateinit var binding: FragmentFavoriteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager =
            GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)

        val helper = FavoriteOpenHelper(requireContext())
        val favoriteList = ArrayList<HashMap<String, String>>()
        helper.writableDatabase.use { db ->
            db.execSQL("CREATE TEMPORARY TABLE FAVORITE_TMP " +
                    "AS SELECT MAX(id), url, name FROM FAVORITE_TABLE GROUP BY name")
            db.execSQL("DELETE FROM FAVORITE_TABLE")
            db.execSQL("INSERT INTO FAVORITE_TABLE SELECT * FROM FAVORITE_TMP")
            db.rawQuery(
                "SELECT url, name FROM FAVORITE_TABLE ORDER BY id DESC", null
            ).use { c ->
                var next = c.moveToFirst() // check cursor has first row or not
                // get all rows
                while (next) {
                    val data = HashMap<String, String>()
                    val url = c.getString(0)
                    val name = c.getString(1)
                    data["url"] = url
                    data["name"] = name
                    favoriteList.add(data)
                    next = c.moveToNext() // check cursor has first row or not
                }
            }
        }

        binding.recyclerView.adapter = HistoryAdapter(favoriteList)
    }
}