package com.example.lt4

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(
    fm: FragmentManager,
    private val context: Context
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 4 // Количество фрагментов
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FragmentShow()
            1 -> FragmentAdd()
            2 -> FragmentDel()
            3 -> FragmentUpdate()
            else -> FragmentShow()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.tab_show)
            1 -> context.getString(R.string.tab_add)
            2 -> context.getString(R.string.tab_del)
            3 -> context.getString(R.string.tab_update)
            else -> null
        }
    }
}
