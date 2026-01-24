package com.example.lt3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckedTextView

class ProductAdapter(
    private val products: List<Product>
) : BaseAdapter() {

    override fun getCount(): Int = products.size

    override fun getItem(position: Int): Product = products[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: CheckedTextView = if (convertView == null) {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false) as CheckedTextView
        } else {
            convertView as CheckedTextView
        }

        val product = getItem(position)
        view.text = product.toString()

        return view
    }
}
