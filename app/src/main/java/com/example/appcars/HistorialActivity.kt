package com.example.appcars

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

        // Inicializamos el adaptador pasando la lista y la acción de clic (navegación con Intent y putExtra).
        adaptador = MantenimientoAdapter(emptyList()) { mantenimiento ->
            // Requisito del proyecto: navegación con Intent enviando datos con putExtra.
            val intent = Intent(this, DetalleMantenimientoActivity::class.java).apply {
                putExtra("EXTRA_VEHICULO", mantenimiento.vehiculo)
                putExtra("EXTRA_TIPO", mantenimiento.tipo)
                putExtra("EXTRA_KM", mantenimiento.kilometros)
                putExtra("EXTRA_FECHA", mantenimiento.fecha)
                putExtra("EXTRA_COSTE", mantenimiento.coste)
            }
            startActivity(intent)
        }
        recycler.adapter = adaptador

        findViewById<Button>(R.id.botonVolver).setOnClickListener { finish() }
    }

    // Se recarga al entrar para mostrar también lo que se acaba de registrar en SharedPreferences.
    override fun onResume() {
        super.onResume()
        val lista = DatosMantenimiento.obtener(this)
        textoContador.text = "Historial (${lista.size})"

        if (lista.isEmpty()) {
            textoVacio.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        } else {
            textoVacio.visibility = View.GONE
            recycler.visibility = View.VISIBLE
            // Mostramos los registros en orden inverso para que el más reciente aparezca primero.
            adaptador.actualizarDatos(lista.asReversed())
        }
    }
}
