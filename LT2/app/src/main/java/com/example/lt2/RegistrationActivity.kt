package com.example.lt2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RegistrationActivity : AppCompatActivity() {

    private lateinit var textViewTitle: TextView
    private lateinit var editTextPhone: EditText
    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var buttonRegistration: Button
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "RegistrationActivity"
        private const val PREFS_NAME = "TaxiPrefs"
        private const val KEY_PHONE = "phone"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_registration)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        textViewTitle = findViewById(R.id.textViewTitle)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextFirstName = findViewById(R.id.editTextFirstName)
        editTextLastName = findViewById(R.id.editTextLastName)
        buttonRegistration = findViewById(R.id.buttonRegistration)

        // Восстановление сохраненных данных
        val savedPhone = sharedPreferences.getString(KEY_PHONE, "")
        val savedFirstName = sharedPreferences.getString(KEY_FIRST_NAME, "")
        val savedLastName = sharedPreferences.getString(KEY_LAST_NAME, "")

        val isRegistered = savedPhone?.isNotEmpty() == true && 
                          savedFirstName?.isNotEmpty() == true && 
                          savedLastName?.isNotEmpty() == true

        if (isRegistered) {
            // При повторном запуске - показываем "Вход" и "Log in"
            textViewTitle.text = getString(R.string.login_title)
            editTextPhone.setText(savedPhone)
            editTextFirstName.setText(savedFirstName)
            editTextLastName.setText(savedLastName)
            buttonRegistration.text = getString(R.string.button_log_in)
        } else {
            // При первом запуске - показываем "Регистрация" и "Register"
            textViewTitle.text = getString(R.string.registration_title)
            buttonRegistration.text = getString(R.string.button_registration)
        }

        buttonRegistration.setOnClickListener {
            val phone = editTextPhone.text.toString()
            val firstName = editTextFirstName.text.toString()
            val lastName = editTextLastName.text.toString()

            if (phone.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty()) {
                // Сохранение данных в SharedPreferences
                // При первом запуске (регистрация) - сохраняем новые данные
                // При последующих запусках (логин) - обновляем данные, если они изменились
                sharedPreferences.edit().apply {
                    putString(KEY_PHONE, phone)
                    putString(KEY_FIRST_NAME, firstName)
                    putString(KEY_LAST_NAME, lastName)
                    apply()
                }

                // Запуск второго Activity с передачей данных
                val intent = Intent(this, TaxiActivity::class.java).apply {
                    putExtra("phone", phone)
                    putExtra("firstName", firstName)
                    putExtra("lastName", lastName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart")
    }
}
