package com.danimodder.dumper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.danimodder.dumper.adapter.TutorialPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TutorialActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        viewPager = findViewById(R.id.viewPagerTutorial)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = TutorialPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Install Frida"
                1 -> "Extract libgame.so"
                2 -> "Using App"
                3 -> "Troubleshooting"
                else -> "Tutorial"
            }
        }.attach()
    }
}