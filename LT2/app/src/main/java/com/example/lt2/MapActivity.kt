package com.example.lt2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import java.util.Locale

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var map: GoogleMap
    private var selectedMarker: Marker? = null
    private var selectedLocation: LatLng? = null

    companion object {
        private const val TAG = "MapActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        
        // Проверка доступности Google Play Services
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Play Services not available. Result code: $resultCode")
            val errorMessage = when (resultCode) {
                ConnectionResult.SERVICE_MISSING -> "Google Play Services отсутствуют"
                ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> "Требуется обновление Google Play Services"
                ConnectionResult.SERVICE_DISABLED -> "Google Play Services отключены"
                ConnectionResult.SERVICE_INVALID -> "Google Play Services недействительны"
                else -> "Google Play Services недоступны (код: $resultCode)"
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            
            // Попытка исправить проблему
            val dialog = apiAvailability.getErrorDialog(this, resultCode, 9001)
            dialog?.show()
            return
        }
        
        Log.d(TAG, "Google Play Services available")
        setContentView(R.layout.activity_map)

        // Проверка API ключа
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
            if (apiKey.isNullOrEmpty() || apiKey == "YOUR_API_KEY_HERE") {
                Log.e(TAG, "Google Maps API key is not set or is placeholder")
                Toast.makeText(
                    this,
                    "API ключ Google Maps не установлен. Проверьте AndroidManifest.xml",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Log.d(TAG, "Google Maps API key is set")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking API key", e)
        }

        // Инициализация карты
        initializeMap()

        val buttonConfirm = findViewById<Button>(R.id.buttonConfirmLocation)
        buttonConfirm.setOnClickListener {
            selectedLocation?.let { location ->
                val address = getAddressFromLocation(location)
                if (address != null) {
                    // Извлечение адреса из Address объекта
                    val addressLine = address.getAddressLine(0) ?: ""
                    val parts = addressLine.split(",")
                    val street = if (parts.isNotEmpty()) parts[0].trim() else ""
                    val house = if (parts.size > 1) parts[1].trim() else ""
                    
                    val resultIntent = Intent().apply {
                        putExtra("endStreet", street)
                        putExtra("endHouse", house)
                        putExtra("endApartment", "")
                        putExtra("latitude", location.latitude)
                        putExtra("longitude", location.longitude)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this, "Не удалось определить адрес", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "Выберите точку на карте", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady called - map is ready!")
        try {
            map = googleMap
            
            // Проверка, что карта действительно инициализирована
            if (!::map.isInitialized) {
                Log.e(TAG, "Map initialization failed")
                Toast.makeText(this, "Ошибка инициализации карты", Toast.LENGTH_LONG).show()
                return
            }
            
            map.setOnMapClickListener(this)

            // Установка начальной позиции (например, Минск)
            val minsk = LatLng(53.9045, 27.5615)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(minsk, 12f))
            Log.d(TAG, "Camera moved to Minsk")

            // Включение кнопки "Мое местоположение"
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true
                    Log.d(TAG, "Location enabled on map")
                } catch (e: SecurityException) {
                    Log.e(TAG, "Error enabling location", e)
                }
            } else {
                Log.d(TAG, "Location permission not granted")
            }

            // Настройка UI карты
            map.uiSettings.isZoomControlsEnabled = true
            map.uiSettings.isCompassEnabled = true
            map.uiSettings.isZoomGesturesEnabled = true
            map.uiSettings.isScrollGesturesEnabled = true
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            
            // Обработчик успешной загрузки карты
            map.setOnMapLoadedCallback {
                Log.d(TAG, "Map tiles loaded successfully")
                Toast.makeText(this, "Карта загружена успешно", Toast.LENGTH_SHORT).show()
            }
            
            Log.d(TAG, "Map setup completed successfully")
            
            // Проверка через несколько секунд - если тайлы не загрузились
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    // Проверяем, что карта действительно работает
                    val testLocation = LatLng(53.9045, 27.5615)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(testLocation, 12f))
                    Log.d(TAG, "Test camera movement completed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error testing map", e)
                }
            }, 3000)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onMapReady", e)
            Toast.makeText(
                this,
                "Ошибка настройки карты: ${e.message}\nПроверьте API ключ в Google Cloud Console",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun initializeMap() {
        try {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
            if (mapFragment == null) {
                Log.e(TAG, "MapFragment is null - fragment not found in layout")
                Toast.makeText(
                    this,
                    "Ошибка: MapFragment не найден. Проверьте layout файл.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            
            Log.d(TAG, "MapFragment found, initializing map...")
            
            // Проверяем, не инициализирована ли карта уже
            if (::map.isInitialized) {
                Log.d(TAG, "Map already initialized")
                return
            }
            
            mapFragment.getMapAsync { googleMap ->
                try {
                    Log.d(TAG, "MapAsync callback received")
                    onMapReady(googleMap)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in MapAsync callback", e)
                    Toast.makeText(
                        this,
                        "Ошибка загрузки карты: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            
            Log.d(TAG, "getMapAsync called successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error calling getMapAsync", e)
            Toast.makeText(
                this,
                "Ошибка инициализации карты: ${e.message}\nПроверьте:\n1. API ключ активирован для Maps SDK\n2. Интернет подключение\n3. Google Play Services установлены",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onMapClick(latLng: LatLng) {
        selectedLocation = latLng

        // Удаление предыдущего маркера
        selectedMarker?.remove()

        // Добавление нового маркера
        selectedMarker = map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Конечная точка")
        )

        // Получение адреса для отображения
        val address = getAddressFromLocation(latLng)
        selectedMarker?.snippet = address?.getAddressLine(0) ?: "Адрес не определен"
        selectedMarker?.showInfoWindow()
    }

    private fun getAddressFromLocation(latLng: LatLng): Address? {
        return try {
            if (!Geocoder.isPresent()) {
                Log.e(TAG, "Geocoder is not present")
                return null
            }
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
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

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        
        // Проверка Google Play Services при возобновлении
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.w(TAG, "Google Play Services became unavailable. Result code: $resultCode")
        }
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
