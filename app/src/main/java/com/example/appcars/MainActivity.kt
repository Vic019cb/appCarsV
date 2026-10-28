package com.example.appcars

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtenemos la referencia a la imagen de portada que cambia según lo que pulsemos
        val imagenPortada = findViewById<ImageView>(R.id.imagenPortada)

        // Al pinchar en coche: mostramos la foto del coche deportivo y reproducimos su sonido de motor
        findViewById<Button>(R.id.botonSonidoCoche).setOnClickListener {
            imagenPortada.setImageResource(R.drawable.coche_deportivo)
            SonidoUtil.reproducirCoche(this)
        }

        // Al pinchar en moto: mostramos la foto de la moto deportiva y reproducimos su sonido de motor
        findViewById<Button>(R.id.botonSonidoMoto).setOnClickListener {
            imagenPortada.setImageResource(R.drawable.moto_deportiva)
            SonidoUtil.reproducirMoto(this)
        }

        // Navegación con Intent a cada una de las pantallas de la aplicación
        findViewById<Button>(R.id.botonAgregar).setOnClickListener {
            startActivity(Intent(this, AgregarMantenimientoActivity::class.java))
        }
        findViewById<Button>(R.id.botonHistorial).setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }
        findViewById<Button>(R.id.botonProximaRevision).setOnClickListener {
            startActivity(Intent(this, ProximaRevisionActivity::class.java))
        }
    }

    // Cada vez que volvemos a la pantalla principal, consultamos Room para actualizar el contador
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val dao = AppDatabase.obtener(this@MainActivity).mantenimientoDao()
            val total = dao.contarMantenimientos()
            val textoResumen = findViewById<TextView>(R.id.textoResumen)
            if (total == 1) {
                textoResumen.text = "Hay 1 mantenimiento guardado en la base de datos."
            } else {
                textoResumen.text = "Hay $total mantenimientos guardados en la base de datos."
            }
        }
    }

    // Liberamos el reproductor al salir para no consumir memoria
    override fun onDestroy() {
        super.onDestroy()
        SonidoUtil.liberar()
    }
}
