package com.example.lt4

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class NoteAdapter(private val context: Context, private val notes: MutableList<Note>) : BaseAdapter() {

    override fun getCount(): Int = notes.size

    override fun getItem(position: Int): Note = notes[position]

    override fun getItemId(position: Int): Long = notes[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val note = notes[position]
        viewHolder.numberTextView.text = "${note.id}"
        viewHolder.descriptionTextView.text = note.content

        return view
    }

    private class ViewHolder(view: View) {
        val numberTextView: TextView = view.findViewById(R.id.tvNoteNumber)
        val descriptionTextView: TextView = view.findViewById(R.id.tvNoteDescription)
    }
}
