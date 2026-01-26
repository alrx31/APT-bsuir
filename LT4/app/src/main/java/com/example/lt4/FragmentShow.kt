package com.example.lt4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment

class FragmentShow : Fragment() {

    private lateinit var listView: ListView
    private lateinit var adapter: NoteAdapter
    private lateinit var dbHelper: NoteDatabaseHelper
    private val notes = mutableListOf<Note>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_show, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHelper = NoteDatabaseHelper(requireContext())
        listView = view.findViewById(R.id.listViewNotes)
        
        loadNotesFromDatabase()
        
        adapter = NoteAdapter(requireContext(), notes)
        listView.adapter = adapter
    }

    private fun loadNotesFromDatabase() {
        notes.clear()
        notes.addAll(dbHelper.getAllNotes())
    }

    override fun onResume() {
        super.onResume()
        // Обновляем список при возврате на фрагмент
        loadNotesFromDatabase()
        adapter.notifyDataSetChanged()
    }
}
