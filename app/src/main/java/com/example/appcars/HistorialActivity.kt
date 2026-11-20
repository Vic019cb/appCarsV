package com.example.appcars

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HistorialActivity : ComponentActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adaptador: MantenimientoAdapter
    private lateinit var textoContador: TextView
    private lateinit var textoVacio: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        textoContador = findViewById(R.id.textoContador)
        textoVacio = findViewById(R.id.textoVacioHistorial)
        recycler = findViewById(R.id.recyclerHistorial)

        // Configuramos el RecyclerView con un administrador de diseño lineal.
        recycler.layoutManager = LinearLayoutManager(this)

        // Inicializamos el adaptador con dos funciones lambda:
        // 1. Clic corto: abre la pantalla de detalles.
        // 2. Clic largo: diálogo para borrar de la base de datos Room (reto 8).
        adaptador = MantenimientoAdapter(
            lista = emptyList(),
            alPulsarElemento = { mantenimiento ->
                val intent = Intent(this, DetalleMantenimientoActivity::class.java).apply {
                    putExtra("EXTRA_VEHICULO", mantenimiento.vehiculo)
                    putExtra("EXTRA_TIPO", mantenimiento.tipo)
                    putExtra("EXTRA_KM", mantenimiento.kilometros)
                    putExtra("EXTRA_FECHA", mantenimiento.fecha)
                    putExtra("EXTRA_COSTE", mantenimiento.coste)
                    // Modificacion: Enviamos también la ruta de la foto y las coordenadas GPS a la pantalla de detalle
                    putExtra("EXTRA_FOTO_PATH", mantenimiento.fotoPath)
                    if (mantenimiento.latitud != null && mantenimiento.longitud != null) {
                        putExtra("EXTRA_TIENE_UBICACION", true)
                        putExtra("EXTRA_LATITUD", mantenimiento.latitud)
                        putExtra("EXTRA_LONGITUD", mantenimiento.longitud)
                    } else {
                        putExtra("EXTRA_TIENE_UBICACION", false)
                    }
                }
                startActivity(intent)
            },
            alMantenerPulsado = { mantenimiento ->
                // Confirmamos antes de borrar para evitar eliminar por error
                AlertDialog.Builder(this)
                    .setTitle("Eliminar mantenimiento")
                    .setMessage("¿Deseas eliminar el registro de ${mantenimiento.vehiculo} (${mantenimiento.tipo})?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        // Borramos de Room en segundo plano
                        lifecycleScope.launch {
                            val dao = AppDatabase.obtener(this@HistorialActivity).mantenimientoDao()
                            dao.borrar(mantenimiento)
                            Toast.makeText(this@HistorialActivity, "Mantenimiento eliminado", Toast.LENGTH_SHORT).show()
                            cargarHistorialDesdeBd() // Refrescamos la lista para que desaparezca
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        recycler.adapter = adaptador

        findViewById<Button>(R.id.botonVolver).setOnClickListener { finish() }
    }

    // Regla del apartado 6: cargamos la lista en onResume para que siempre se refresque al volver de otra pantalla.
    override fun onResume() {
        super.onResume()
        cargarHistorialDesdeBd()
    }

    // Lee los datos desde Room usando corrutinas (lifecycleScope.launch) para no congelar la pantalla.
    private fun cargarHistorialDesdeBd() {
        lifecycleScope.launch {
            val dao = AppDatabase.obtener(this@HistorialActivity).mantenimientoDao()
            val lista = dao.obtenerTodas()
            textoContador.text = "Historial (${lista.size})"

            if (lista.isEmpty()) {
                textoVacio.visibility = View.VISIBLE
                recycler.visibility = View.GONE
            } else {
                textoVacio.visibility = View.GONE
                recycler.visibility = View.VISIBLE
                // Como el DAO ya ordena por "ORDER BY id DESC", pasamos la lista directamente
                adaptador.actualizarDatos(lista)
            }
        }
    }
}
