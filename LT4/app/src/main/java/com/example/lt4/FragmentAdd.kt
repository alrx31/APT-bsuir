package com.example.lt4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentAdd : Fragment() {

    private lateinit var etNoteDescription: EditText
    private lateinit var btnAdd: Button
    private lateinit var dbHelper: NoteDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHelper = NoteDatabaseHelper(requireContext())
        etNoteDescription = view.findViewById(R.id.etNoteDescription)
        btnAdd = view.findViewById(R.id.btnAdd)
        
        btnAdd.setOnClickListener {
            addNote()
        }
    }

    private fun addNote() {
        val description = etNoteDescription.text.toString().trim()
        
        if (description.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.enter_note_description), Toast.LENGTH_SHORT).show()
            return
        }
        
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        // Используем описание как заголовок и содержимое
        val title = if (description.length > 50) {
            description.substring(0, 50) + "..."
        } else {
            description
        }
        
        val result = dbHelper.insertNote(title, description, currentDate)
        
        if (result != -1L) {
            Toast.makeText(requireContext(), getString(R.string.note_added), Toast.LENGTH_SHORT).show()
            etNoteDescription.text.clear()
            (activity as? PagerNavigator)?.openPage(0) // Show
        } else {
            val details = dbHelper.lastError?.let { "\n$it" }.orEmpty()
            Toast.makeText(
                requireContext(),
                getString(R.string.error_adding_note) + details,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
