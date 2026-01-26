package com.example.lt4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity(), PagerNavigator {

    private lateinit var viewPager: ViewPager
    private lateinit var adapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ensure SQLite DB is created/upgraded on first app start (before any inserts).
        NoteDatabaseHelper(this).writableDatabase.close()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        adapter = ViewPagerAdapter(supportFragmentManager, this)
        viewPager.adapter = adapter

        tabLayout.setupWithViewPager(viewPager)
    }

    override fun openPage(index: Int) {
        viewPager.currentItem = index.coerceIn(0, adapter.count - 1)
    }
}