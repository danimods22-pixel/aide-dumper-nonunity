package com.danimodder.dumper.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.danimodder.dumper.fragment.*

class TutorialPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> InstallFridaFragment()
            1 -> ExtractLibraryFragment()
            2 -> UsingAppFragment()
            3 -> TroubleshootingFragment()
            else -> InstallFridaFragment()
        }
    }
}