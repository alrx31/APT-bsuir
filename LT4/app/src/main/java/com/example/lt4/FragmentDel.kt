package com.example.lt4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class FragmentDel : Fragment() {

    private lateinit var etNoteNumber: EditText
    private lateinit var btnDel: Button
    private lateinit var dbHelper: NoteDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_del, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHelper = NoteDatabaseHelper(requireContext())
        etNoteNumber = view.findViewById(R.id.etNoteNumber)
        btnDel = view.findViewById(R.id.btnDel)
        
        btnDel.setOnClickListener {
            deleteNote()
        }
    }

    private fun deleteNote() {
        val noteNumberText = etNoteNumber.text.toString().trim()
        
        if (noteNumberText.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.enter_note_number), Toast.LENGTH_SHORT).show()
            return
        }
        
        val noteId = try {
            noteNumberText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), getString(R.string.invalid_note_number), Toast.LENGTH_SHORT).show()
            return
        }
        
        // Проверяем существование заметки
        val note = dbHelper.getNoteById(noteId)
        if (note == null) {
            Toast.makeText(
                requireContext(), 
                getString(R.string.note_not_found_number, noteId),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        
        val result = dbHelper.deleteNote(noteId)
        
        if (result > 0) {
            Toast.makeText(requireContext(), getString(R.string.note_deleted), Toast.LENGTH_SHORT).show()
            etNoteNumber.text.clear()
            (activity as? PagerNavigator)?.openPage(0) // Show
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_deleting_note), Toast.LENGTH_SHORT).show()
        }
    }
}
