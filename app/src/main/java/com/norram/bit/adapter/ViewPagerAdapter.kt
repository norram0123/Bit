package com.norram.bit

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    private val list: Array<String>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = list.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HistoryFragment()
            else -> FavoriteFragment()
        }
    }
}