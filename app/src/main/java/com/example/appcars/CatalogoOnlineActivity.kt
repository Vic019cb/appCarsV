package com.example.appcars

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Nuevo Proyecto V2: Pantalla del Catálogo Online (Hitos 1, 2 y 3).
// Esta Activity solo se encarga de la interfaz y de escuchar al CatalogoViewModel:
// no hace llamadas a Internet ni a Room directamente.
class CatalogoOnlineActivity : ComponentActivity() {

    private lateinit var viewModel: CatalogoViewModel
    private lateinit var adaptador: CatalogoAdapter
    private var primeraCarga = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo_online)

        // Obtenemos el ViewModel que gestiona la lógica y conserva el estado
        viewModel = ViewModelProvider(this)[CatalogoViewModel::class.java]

        val editMarca = findViewById<EditText>(R.id.editBuscarMarca)
        val botonBuscar = findViewById<Button>(R.id.botonBuscarApi)
        val editFiltro = findViewById<EditText>(R.id.editFiltroRapido)
        val botonFavoritos = findViewById<Button>(R.id.botonSoloFavoritos)
        val textoEstado = findViewById<TextView>(R.id.textoEstadoApi)
        val progressBar = findViewById<ProgressBar>(R.id.progressCargandoApi)
        val textoSinResultados = findViewById<TextView>(R.id.textoSinResultadosApi)
        val recycler = findViewById<RecyclerView>(R.id.recyclerCatalogoApi)

        recycler.layoutManager = LinearLayoutManager(this)

        // Configuramos el adaptador con dos acciones: abrir detalle y marcar favorito
        adaptador = CatalogoAdapter(
            lista = emptyList(),
            alPulsarModelo = { modelo ->
                val intent = Intent(this, DetalleModeloActivity::class.java).apply {
                    putExtra("EXTRA_MODEL_ID", modelo.modelId)
                    putExtra("EXTRA_MAKE_ID", modelo.makeId)
                    putExtra("EXTRA_MAKE_NAME", modelo.makeName)
                    putExtra("EXTRA_MODEL_NAME", modelo.modelName)
                    putExtra("EXTRA_ES_FAVORITO", modelo.esFavorito)
                    putExtra("EXTRA_FECHA_CONSULTA", modelo.fechaConsulta)
                }
                startActivity(intent)
            },
            alPulsarFavorito = { modelo ->
                viewModel.cambiarFavorito(modelo)
            }
        )
        recycler.adapter = adaptador

        // Botón principal de búsqueda en la API
        botonBuscar.setOnClickListener {
            editFiltro.setText("")
            viewModel.buscarMarca(editMarca.text.toString())
        }

        // Botones de marcas rápidas para consultar la API con un solo clic
        fun buscarMarcaRapida(marca: String) {
            editMarca.setText(marca)
            editFiltro.setText("")
            viewModel.buscarMarca(marca)
        }
        findViewById<Button>(R.id.btnMarcaToyota).setOnClickListener { buscarMarcaRapida("Toyota") }
        findViewById<Button>(R.id.btnMarcaAudi).setOnClickListener { buscarMarcaRapida("Audi") }
        findViewById<Button>(R.id.btnMarcaHonda).setOnClickListener { buscarMarcaRapida("Honda") }
        findViewById<Button>(R.id.btnMarcaFord).setOnClickListener { buscarMarcaRapida("Ford") }

        // Mejora propia: filtro en tiempo real sobre los modelos mostrados
        editFiltro.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filtrarPorTexto(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Mejora propia: botón para alternar la vista de solo favoritos guardados en Room
        botonFavoritos.setOnClickListener {
            viewModel.alternarSoloFavoritos()
        }

        findViewById<Button>(R.id.botonVolverCatalogo).setOnClickListener { finish() }

        // Observamos los LiveData del ViewModel para actualizar carga, lista, errores y favoritos
        viewModel.cargando.observe(this) { estaCargando ->
            progressBar.visibility = if (estaCargando) View.VISIBLE else View.GONE
            botonBuscar.isEnabled = !estaCargando
        }

        viewModel.modelosMostrados.observe(this) { lista ->
            adaptador.actualizarDatos(lista)
            val cargandoAhora = viewModel.cargando.value == true
            if (lista.isEmpty() && !cargandoAhora) {
                textoSinResultados.visibility = View.VISIBLE
                recycler.visibility = View.GONE
            } else {
                textoSinResultados.visibility = View.GONE
                recycler.visibility = View.VISIBLE
            }
        }

        viewModel.mensajeEstado.observe(this) { mensaje ->
            textoEstado.text = mensaje
        }

        viewModel.esError.observe(this) { hayError ->
            textoEstado.setTextColor(
                if (hayError) Color.parseColor("#FFAB91") else Color.parseColor("#80CBC4")
            )
        }

        viewModel.soloFavoritos.observe(this) { viendoFavoritos ->
            botonFavoritos.text = if (viendoFavoritos) "★ Ver todos" else "⭐ Favoritos"
        }

        // Al entrar por primera vez consultamos "Toyota" automáticamente para ver resultados reales al instante
        editMarca.setText("Toyota")
        viewModel.buscarMarca("Toyota")
    }

    // Si volvemos de la pantalla de detalle, refrescamos la caché por si se cambió el favorito allí
    override fun onResume() {
        super.onResume()
        if (primeraCarga) {
            primeraCarga = false
        } else if (viewModel.soloFavoritos.value == true) {
            viewModel.cargarTodosDeCache()
        }
    }
}
