package com.example.lt3

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.ComponentActivity

class SelectedItemsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selected_items)

        val selectedListView = findViewById<ListView>(R.id.selectedListView)
        val button = findViewById<Button>(R.id.button);

        val selectedItems = intent.getStringArrayListExtra("selected_items") ?: arrayListOf()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            selectedItems
        )

        selectedListView.adapter = adapter

        button.setOnClickListener {
            finish()
        }
    }
}
