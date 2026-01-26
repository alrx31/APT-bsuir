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

class FragmentUpdate : Fragment() {

    private lateinit var etNoteNumber: EditText
    private lateinit var etNoteDescription: EditText
    private lateinit var btnUpdate: Button
    private lateinit var dbHelper: NoteDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHelper = NoteDatabaseHelper(requireContext())
        etNoteNumber = view.findViewById(R.id.etNoteNumber)
        etNoteDescription = view.findViewById(R.id.etNoteDescription)
        btnUpdate = view.findViewById(R.id.btnUpdate)
        
        btnUpdate.setOnClickListener {
            updateNote()
        }
    }

    private fun updateNote() {
        val noteNumberText = etNoteNumber.text.toString().trim()
        val description = etNoteDescription.text.toString().trim()
        
        if (noteNumberText.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.enter_note_number), Toast.LENGTH_SHORT).show()
            return
        }
        
        if (description.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.enter_note_description), Toast.LENGTH_SHORT).show()
            return
        }
        
        val noteId = try {
            noteNumberText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), getString(R.string.invalid_note_number), Toast.LENGTH_SHORT).show()
            return
        }
        
        // Проверяем существование заметки
        val existingNote = dbHelper.getNoteById(noteId)
        if (existingNote == null) {
            Toast.makeText(
                requireContext(), 
                getString(R.string.note_not_found_number, noteId),
                Toast.LENGTH_LONG
            ).show()
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
        
        val result = dbHelper.updateNote(noteId, title, description, currentDate)
        
        if (result > 0) {
            Toast.makeText(requireContext(), getString(R.string.note_updated), Toast.LENGTH_SHORT).show()
            etNoteNumber.text.clear()
            etNoteDescription.text.clear()
            (activity as? PagerNavigator)?.openPage(0) // Show
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_updating_note), Toast.LENGTH_SHORT).show()
        }
    }
}
