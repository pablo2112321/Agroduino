package com.example.agroduino

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity2 : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    // Views
    private lateinit var tvSueloHumedad: TextView
    private lateinit var tvAireHumedad: TextView
    private lateinit var tvTemperatura: TextView
    private lateinit var tvUltimaActualizacion: TextView
    private lateinit var pbSuelo: ProgressBar
    private lateinit var pbAire: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // Inicializar vistas
        initViews()

        // Configurar Firebase
        setupFirebase()
    }

    private fun initViews() {
        tvSueloHumedad = findViewById(R.id.tv_suelo_humedad)
        tvAireHumedad = findViewById(R.id.tv_aire_humedad)
        tvTemperatura = findViewById(R.id.tv_temperatura)
        tvUltimaActualizacion = findViewById(R.id.tv_ultima_actualizacion)
        pbSuelo = findViewById(R.id.pb_suelo)
        pbAire = findViewById(R.id.pb_aire)

        // Configurar progress bars
        pbSuelo.max = 100
        pbAire.max = 100
    }

    private fun setupFirebase() {
        database = FirebaseDatabase.getInstance("https://agroduino-1b35c-default-rtdb.firebaseio.com/").reference

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Obtener todos los datos
                val sueloHumedad = snapshot.child("suelo_humedad").getValue(Int::class.java) ?: 0
                val aireHumedad = snapshot.child("aire_humedad").getValue(Double::class.java) ?: 0.0
                val temperatura = snapshot.child("aire_temp").getValue(Double::class.java) ?: 0.0

                // Actualizar UI
                updateUI(sueloHumedad, aireHumedad, temperatura)

                // Registrar última actualización
                updateLastRefreshTime()
            }

            override fun onCancelled(error: DatabaseError) {
                showError("Error al leer datos: ${error.message}")
            }
        })
    }

    private fun updateUI(sueloHumedad: Int, aireHumedad: Double, temperatura: Double) {
        // Actualizar TextViews
        tvSueloHumedad.text = "Humedad Suelo: $sueloHumedad%"
        tvAireHumedad.text = "Humedad Aire: ${"%.1f".format(aireHumedad)}%"
        tvTemperatura.text = "Temperatura: ${"%.1f".format(temperatura)}°C"

        // Actualizar ProgressBars
        pbSuelo.progress = sueloHumedad
        pbAire.progress = aireHumedad.toInt()

        // Cambiar color según valores
        setProgressBarColor(pbSuelo, sueloHumedad)
        setProgressBarColor(pbAire, aireHumedad.toInt())

        // Mostrar alertas si es necesario
        checkForAlerts(sueloHumedad, aireHumedad, temperatura)
    }

    private fun setProgressBarColor(progressBar: ProgressBar, value: Int) {
        val color = when {
            value < 30 -> R.color.dry_color // Rojo para valores bajos
            value < 70 -> R.color.normal_color // Verde para valores normales
            else -> R.color.high_humidity_color // Azul para valores altos
        }
        progressBar.progressDrawable.setTint(ContextCompat.getColor(this, color))
    }

    private fun checkForAlerts(sueloHumedad: Int, aireHumedad: Double, temperatura: Double) {
        if (sueloHumedad < 30) {
            showAlert("¡Suelo seco! Considera regar las plantas.")
        } else if (sueloHumedad > 85) {
            showAlert("¡Suelo muy húmedo! Reduce el riego.")
        }

        if (aireHumedad > 80 && temperatura > 28) {
            showAlert("¡Ambiente cálido y húmedo! Ventila el invernadero.")
        }
    }

    private fun updateLastRefreshTime() {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentTime = timeFormat.format(Date())
        tvUltimaActualizacion.text = "Últ. actualización: $currentTime"
    }

    private fun showError(message: String) {
        tvSueloHumedad.text = message
        tvAireHumedad.text = ""
        tvTemperatura.text = ""
    }

    private fun showAlert(message: String) {
        // Aquí puedes implementar notificaciones o Toast
        // Ejemplo simple:
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }
}