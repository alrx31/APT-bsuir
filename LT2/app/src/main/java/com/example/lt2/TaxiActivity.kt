package com.example.lt2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TaxiActivity : AppCompatActivity() {

    private lateinit var textViewUserName: TextView
    private lateinit var textViewPhone: TextView
    private lateinit var textViewRoute: TextView
    private lateinit var buttonSetPath: Button
    private lateinit var buttonCallTaxi: Button
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "TaxiActivity"
        private const val REQUEST_CODE_ROUTE = 1
        private const val PREFS_NAME = "TaxiPrefs"
        private const val KEY_ROUTE_START_STREET = "route_start_street"
        private const val KEY_ROUTE_START_HOUSE = "route_start_house"
        private const val KEY_ROUTE_START_APARTMENT = "route_start_apartment"
        private const val KEY_ROUTE_END_STREET = "route_end_street"
        private const val KEY_ROUTE_END_HOUSE = "route_end_house"
        private const val KEY_ROUTE_END_APARTMENT = "route_end_apartment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_taxi)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        textViewUserName = findViewById(R.id.textViewUserName)
        textViewPhone = findViewById(R.id.textViewPhone)
        textViewRoute = findViewById(R.id.textViewRoute)
        buttonSetPath = findViewById(R.id.buttonSetPath)
        buttonCallTaxi = findViewById(R.id.buttonCallTaxi)

        // Получение данных из Intent
        val phone = intent.getStringExtra("phone") ?: ""
        val firstName = intent.getStringExtra("firstName") ?: ""
        val lastName = intent.getStringExtra("lastName") ?: ""

        // Вывод информации о пользователе
        textViewUserName.text = getString(R.string.user_name_default) + " $firstName $lastName"
        textViewPhone.text = getString(R.string.phone_default) + " $phone"

        // Восстановление сохраненного маршрута
        restoreRoute()

        buttonSetPath.setOnClickListener {
            // Неявный вызов третьего Activity через startActivityForResult
            val intent = Intent().apply {
                action = "com.example.lt2.ACTION_ROUTE"
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            startActivityForResult(intent, REQUEST_CODE_ROUTE)
        }

        buttonCallTaxi.setOnClickListener {
            Toast.makeText(this, getString(R.string.taxi_called_message), Toast.LENGTH_LONG).show()
        }
    }

    private fun restoreRoute() {
        val startStreet = sharedPreferences.getString(KEY_ROUTE_START_STREET, "")
        val startHouse = sharedPreferences.getString(KEY_ROUTE_START_HOUSE, "")
        val startApartment = sharedPreferences.getString(KEY_ROUTE_START_APARTMENT, "")
        val endStreet = sharedPreferences.getString(KEY_ROUTE_END_STREET, "")
        val endHouse = sharedPreferences.getString(KEY_ROUTE_END_HOUSE, "")
        val endApartment = sharedPreferences.getString(KEY_ROUTE_END_APARTMENT, "")

        if (startStreet?.isNotEmpty() == true && endStreet?.isNotEmpty() == true) {
            val routeInfo = """
                    От: $startStreet, д. $startHouse, кв. $startApartment
                    До: $endStreet, д. $endHouse, кв. $endApartment
                    
                    Вы можете вызвать такси.
                """.trimIndent()

            textViewRoute.text = routeInfo
            buttonCallTaxi.isEnabled = true
        }
    }

    private fun saveRoute(
        startStreet: String,
        startHouse: String,
        startApartment: String,
        endStreet: String,
        endHouse: String,
        endApartment: String
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_ROUTE_START_STREET, startStreet)
            putString(KEY_ROUTE_START_HOUSE, startHouse)
            putString(KEY_ROUTE_START_APARTMENT, startApartment)
            putString(KEY_ROUTE_END_STREET, endStreet)
            putString(KEY_ROUTE_END_HOUSE, endHouse)
            putString(KEY_ROUTE_END_APARTMENT, endApartment)
            apply()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == REQUEST_CODE_ROUTE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val startStreet = it.getStringExtra("startStreet") ?: ""
                val startHouse = it.getStringExtra("startHouse") ?: ""
                val startApartment = it.getStringExtra("startApartment") ?: ""
                val endStreet = it.getStringExtra("endStreet") ?: ""
                val endHouse = it.getStringExtra("endHouse") ?: ""
                val endApartment = it.getStringExtra("endApartment") ?: ""

                // Сохранение маршрута в SharedPreferences
                saveRoute(startStreet, startHouse, startApartment, endStreet, endHouse, endApartment)

                val routeInfo = """
                    От: $startStreet, д. $startHouse, кв. $startApartment
                    До: $endStreet, д. $endHouse, кв. $endApartment
                    
                    Вы можете вызвать такси.
                """.trimIndent()

                textViewRoute.text = routeInfo
                buttonCallTaxi.isEnabled = true
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
