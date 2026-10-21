package com.example.appcars

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.text.NumberFormat
import java.util.Locale

// Actividad para mostrar la ficha completa de un mantenimiento recibido a través del Intent.
class DetalleMantenimientoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_mantenimiento)

        // Recuperamos los datos enviados desde el RecyclerView mediante getExtra (requisito del proyecto).
        val vehiculo = intent.getStringExtra("EXTRA_VEHICULO") ?: "Coche"
        val tipo = intent.getStringExtra("EXTRA_TIPO") ?: "Mantenimiento general"
        val kilometros = intent.getIntExtra("EXTRA_KM", 0)
        val fecha = intent.getStringExtra("EXTRA_FECHA") ?: ""
        val coste = intent.getDoubleExtra("EXTRA_COSTE", 0.0)

        // Referencias a los elementos del diseño.
        val imagenVehiculo = findViewById<ImageView>(R.id.imagenDetalleVehiculo)
        val textoVehiculo = findViewById<TextView>(R.id.textoDetalleVehiculo)
        val textoTipo = findViewById<TextView>(R.id.textoDetalleTipo)
        val textoKm = findViewById<TextView>(R.id.textoDetalleKm)
        val textoFecha = findViewById<TextView>(R.id.textoDetalleFecha)
        val textoCoste = findViewById<TextView>(R.id.textoDetalleCoste)
        val botonSonido = findViewById<Button>(R.id.botonDetalleSonido)
        val botonVolver = findViewById<Button>(R.id.botonVolverDetalle)

        // Mostramos la imagen deportiva según el tipo de vehículo (sentido funcional del ImageView).
        val esMoto = vehiculo.equals("Moto", ignoreCase = true)
        if (esMoto) {
            imagenVehiculo.setImageResource(R.drawable.moto_deportiva)
            textoVehiculo.text = "Vehículo: 🏍️ Moto"
        } else {
            imagenVehiculo.setImageResource(R.drawable.coche_deportivo)
            textoVehiculo.text = "Vehículo: 🚗 Coche"
        }

        textoTipo.text = "Operación: $tipo"
        textoKm.text = "Kilometraje al registrar: $kilometros km"
        textoFecha.text = "Fecha realizada: $fecha"

        val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
        textoCoste.text = "Coste total: ${formatoMoneda.format(coste)}"

        // Botón para reproducir el sonido del motor de este vehículo específico.
        botonSonido.setOnClickListener {
            if (esMoto) {
                SonidoUtil.reproducirMoto(this)
            } else {
                SonidoUtil.reproducirCoche(this)
            }
        }

        botonVolver.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        SonidoUtil.liberar()
    }
}
