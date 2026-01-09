package com.example.lt2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

class RouteActivity : AppCompatActivity() {

    private lateinit var editTextStartStreet: EditText
    private lateinit var editTextStartHouse: EditText
    private lateinit var editTextStartApartment: EditText
    private lateinit var editTextEndStreet: EditText
    private lateinit var editTextEndHouse: EditText
    private lateinit var editTextEndApartment: EditText
    private lateinit var buttonOk: Button
    private lateinit var buttonGetLocation: Button
    private lateinit var buttonSelectOnMap: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val TAG = "RouteActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val MAP_ACTIVITY_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_route)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        editTextStartStreet = findViewById(R.id.editTextStartStreet)
        editTextStartHouse = findViewById(R.id.editTextStartHouse)
        editTextStartApartment = findViewById(R.id.editTextStartApartment)
        editTextEndStreet = findViewById(R.id.editTextEndStreet)
        editTextEndHouse = findViewById(R.id.editTextEndHouse)
        editTextEndApartment = findViewById(R.id.editTextEndApartment)
        buttonOk = findViewById(R.id.buttonOk)
        buttonGetLocation = findViewById(R.id.buttonGetLocation)
        buttonSelectOnMap = findViewById(R.id.buttonSelectOnMap)

        buttonGetLocation.setOnClickListener {
            getCurrentLocation()
        }

        buttonSelectOnMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivityForResult(intent, MAP_ACTIVITY_REQUEST_CODE)
        }

        buttonOk.setOnClickListener {
            val startStreet = editTextStartStreet.text.toString()
            val startHouse = editTextStartHouse.text.toString()
            val startApartment = editTextStartApartment.text.toString()
            val endStreet = editTextEndStreet.text.toString()
            val endHouse = editTextEndHouse.text.toString()
            val endApartment = editTextEndApartment.text.toString()

            // Возврат во второе Activity с результатом
            val resultIntent = Intent().apply {
                putExtra("startStreet", startStreet)
                putExtra("startHouse", startHouse)
                putExtra("startApartment", startApartment)
                putExtra("endStreet", endStreet)
                putExtra("endHouse", endHouse)
                putExtra("endApartment", endApartment)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        Toast.makeText(this, getString(R.string.getting_location), Toast.LENGTH_SHORT).show()

        // Используем getCurrentLocation для более надежного получения локации
        val cancellationTokenSource = CancellationTokenSource()
        
        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d(TAG, "Location received: ${location.latitude}, ${location.longitude}")
                    val address = getAddressFromLocation(location.latitude, location.longitude)
                    if (address != null) {
                        // Извлечение адреса из Address объекта
                        val addressLine = address.getAddressLine(0) ?: ""
                        val parts = addressLine.split(",")
                        
                        if (parts.isNotEmpty()) {
                            editTextStartStreet.setText(parts[0].trim())
                            if (parts.size > 1) {
                                editTextStartHouse.setText(parts[1].trim())
                            }
                        } else {
                            // Если адрес не разбит запятыми, используем полный адрес
                            editTextStartStreet.setText(addressLine)
                        }
                        
                        Toast.makeText(
                            this,
                            "Местоположение определено: ${location.latitude}, ${location.longitude}",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // Если адрес не получен, все равно заполняем координатами
                        editTextStartStreet.setText("Широта: ${location.latitude}, Долгота: ${location.longitude}")
                        Toast.makeText(
                            this,
                            "Координаты получены, но адрес не определен",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Если getCurrentLocation вернул null, пробуем lastLocation
                    Log.d(TAG, "getCurrentLocation returned null, trying lastLocation")
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation: Location? ->
                        if (lastLocation != null) {
                            val address = getAddressFromLocation(lastLocation.latitude, lastLocation.longitude)
                            if (address != null) {
                                val addressLine = address.getAddressLine(0) ?: ""
                                val parts = addressLine.split(",")
                                if (parts.isNotEmpty()) {
                                    editTextStartStreet.setText(parts[0].trim())
                                    if (parts.size > 1) {
                                        editTextStartHouse.setText(parts[1].trim())
                                    }
                                }
                                Toast.makeText(
                                    this,
                                    "Использовано последнее известное местоположение",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Не удалось определить адрес",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Включите GPS и попробуйте снова",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error getting location", exception)
                Toast.makeText(
                    this,
                    "Ошибка: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException", e)
            Toast.makeText(
                this,
                "Нет разрешения на доступ к местоположению",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): Address? {
        return try {
            if (!Geocoder.isPresent()) {
                Log.e(TAG, "Geocoder is not present")
                return null
            }
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                addresses[0]
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting address", e)
            null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && 
                (grantResults[0] == PackageManager.PERMISSION_GRANTED || 
                 (grantResults.size > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
                getCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_required),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val endStreet = it.getStringExtra("endStreet") ?: ""
                val endHouse = it.getStringExtra("endHouse") ?: ""
                val endApartment = it.getStringExtra("endApartment") ?: ""

                editTextEndStreet.setText(endStreet)
                editTextEndHouse.setText(endHouse)
                editTextEndApartment.setText(endApartment)

                Toast.makeText(
                    this,
                    "Конечная точка выбрана на карте",
                    Toast.LENGTH_SHORT
                ).show()
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
