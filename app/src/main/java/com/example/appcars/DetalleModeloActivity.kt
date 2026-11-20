package com.example.appcars

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// Nuevo Proyecto V2: Pantalla de detalle ampliado de un modelo consultado en la API (Hito 2).
// También integra la mejora propia de favoritos en Room y el acceso directo a registrar un mantenimiento con ese modelo.
class DetalleModeloActivity : ComponentActivity() {

    private var esFavoritoActual: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_modelo)

        // Recibimos los datos del modelo pulsado en el RecyclerView mediante el Intent
        val modelId = intent.getIntExtra("EXTRA_MODEL_ID", 0)
        val makeId = intent.getIntExtra("EXTRA_MAKE_ID", 0)
        val makeName = intent.getStringExtra("EXTRA_MAKE_NAME") ?: "Desconocida"
        val modelName = intent.getStringExtra("EXTRA_MODEL_NAME") ?: "Modelo"
        esFavoritoActual = intent.getBooleanExtra("EXTRA_ES_FAVORITO", false)
        val fechaConsulta = intent.getStringExtra("EXTRA_FECHA_CONSULTA") ?: "Sin fecha"

        // Rellenamos la ficha técnica ampliada del modelo
        findViewById<TextView>(R.id.textoDetalleNombreModelo).text = "Modelo: $modelName"
        findViewById<TextView>(R.id.textoDetalleMarcaModelo).text = "Fabricante / Marca: $makeName"
        findViewById<TextView>(R.id.textoDetalleModelId).text = "ID oficial del modelo (NHTSA): $modelId"
        findViewById<TextView>(R.id.textoDetalleMakeId).text = "ID oficial del fabricante: $makeId"
        findViewById<TextView>(R.id.textoDetalleFechaCache).text = "Guardado en caché Room: $fechaConsulta"

        val botonFavorito = findViewById<Button>(R.id.botonDetalleFavorito)
        val botonUsarEnMantenimiento = findViewById<Button>(R.id.botonUsarEnMantenimiento)
        val botonVolver = findViewById<Button>(R.id.botonVolverDetalleModelo)

        fun actualizarTextoFavorito() {
            botonFavorito.text = if (esFavoritoActual) {
                "★ En favoritos (toca para quitar)"
            } else {
                "☆ Guardar en favoritos"
            }
        }
        actualizarTextoFavorito()

        // Mejora propia: permite marcar o quitar el favorito directamente desde el detalle
        botonFavorito.setOnClickListener {
            esFavoritoActual = !esFavoritoActual
            actualizarTextoFavorito()
            lifecycleScope.launch {
                val repo = VehiculoRepository(AppDatabase.obtener(this@DetalleModeloActivity).modeloCacheDao())
                repo.cambiarFavorito(modelId, esFavoritoActual)
                val aviso = if (esFavoritoActual) "Guardado en favoritos" else "Quitado de favoritos"
                Toast.makeText(this@DetalleModeloActivity, aviso, Toast.LENGTH_SHORT).show()
            }
        }

        // Mejora propia: conecta la API con el formulario real de mantenimientos de appCarsV
        botonUsarEnMantenimiento.setOnClickListener {
            val intentMantenimiento = Intent(this, AgregarMantenimientoActivity::class.java).apply {
                putExtra("EXTRA_MODELO_API", "$makeName $modelName")
            }
            startActivity(intentMantenimiento)
        }

        botonVolver.setOnClickListener { finish() }
    }
}
