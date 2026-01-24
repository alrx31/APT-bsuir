package com.example.lt3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity

data class Product(
    val name: String,
    val price: Double
) {
    override fun toString(): String {
        return "$name — $price руб."
    }
}

class MainActivity : ComponentActivity() {

    private val products = listOf(
        Product("Яблоки", 120.0),
        Product("Бананы", 89.50),
        Product("Молоко", 75.0),
        Product("Хлеб", 45.0),
        Product("Сыр", 350.0),
        Product("Колбаса", 280.0),
        Product("Йогурт", 65.0)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val listView = findViewById<ListView>(R.id.listView)
        val showSelectedButton = findViewById<Button>(R.id.showSelectedButton)

        val adapter = ProductAdapter(products)
        listView.adapter = adapter

        showSelectedButton.setOnClickListener {
            val selectedProducts = mutableListOf<Product>()
            val checkedPositions = listView.checkedItemPositions

            for (i in 0 until checkedPositions.size()) {
                val position = checkedPositions.keyAt(i)
                if (checkedPositions.valueAt(i)) {
                    selectedProducts.add(products[position])
                }
            }

            if (selectedProducts.isEmpty()) {
                Toast.makeText(this, "Ничего не выбрано", Toast.LENGTH_SHORT).show()
            } else {
                val totalPrice = selectedProducts.sumOf { it.price }
                val selectedStrings = ArrayList(selectedProducts.map { it.toString() })

                val intent = Intent(this, SelectedItemsActivity::class.java)
                intent.putStringArrayListExtra("selected_items", selectedStrings)
                intent.putExtra("total_price", totalPrice)
                startActivity(intent)
            }
        }
    }
}
