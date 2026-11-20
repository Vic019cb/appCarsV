package com.example.appcars

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Locale

// Nuevo Proyecto V2: ViewModel (Hito 3) encargado de mantener el estado de la pantalla del catálogo online.
// Usa LiveData para avisar a la Activity cuando está cargando, cuando llegan datos o si ocurre un error.
class CatalogoViewModel(application: Application) : AndroidViewModel(application) {

    // Conectamos el ViewModel con el Repository (nunca directamente con Retrofit desde la Activity)
    private val repository = VehiculoRepository(
        AppDatabase.obtener(application).modeloCacheDao()
    )

    // Estados observables que la Activity escucha para actualizar la interfaz
    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _modelosMostrados = MutableLiveData<List<ModeloCache>>(emptyList())
    val modelosMostrados: LiveData<List<ModeloCache>> = _modelosMostrados

    private val _mensajeEstado = MutableLiveData<String>("Elige o escribe una marca para consultar la API.")
    val mensajeEstado: LiveData<String> = _mensajeEstado

    private val _esError = MutableLiveData(false)
    val esError: LiveData<Boolean> = _esError

    private val _soloFavoritos = MutableLiveData(false)
    val soloFavoritos: LiveData<Boolean> = _soloFavoritos

    // Guardamos la lista base actual y el texto del filtro rápido (mejora propia)
    private var listaCompletaActual: List<ModeloCache> = emptyList()
    private var textoFiltroActual: String = ""

    // Lanza la consulta a la API (o a Room si no hay conexión) usando viewModelScope
    fun buscarMarca(marca: String) {
        val marcaLimpia = marca.trim()
        if (marcaLimpia.isEmpty()) {
            _esError.value = true
            _mensajeEstado.value = "Escribe el nombre de una marca (por ejemplo: Seat, Toyota o Audi)."
            return
        }

        _soloFavoritos.value = false
        _cargando.value = true
        _esError.value = false
        _mensajeEstado.value = "Consultando modelos de $marcaLimpia en Internet..."

        viewModelScope.launch {
            val resultado = repository.buscarModelosPorMarca(marcaLimpia)
            listaCompletaActual = resultado.modelos
            _esError.value = resultado.esError
            _mensajeEstado.value = resultado.mensaje
            aplicarFiltroLocal()
            _cargando.value = false
        }
    }

    // Mejora propia: filtra en tiempo real los modelos ya cargados sin gastar Internet
    fun filtrarPorTexto(texto: String) {
        textoFiltroActual = texto.trim()
        aplicarFiltroLocal()
    }

    // Mejora propia: alterna entre ver la lista actual o ver solo los favoritos guardados en Room
    fun alternarSoloFavoritos() {
        val nuevoEstado = !(_soloFavoritos.value ?: false)
        _soloFavoritos.value = nuevoEstado

        viewModelScope.launch {
            if (nuevoEstado) {
                val favoritos = repository.obtenerFavoritos()
                listaCompletaActual = favoritos
                _esError.value = false
                _mensajeEstado.value = if (favoritos.isEmpty()) {
                    "Todavía no tienes modelos favoritos. Toca la estrella ☆ de cualquier modelo para guardarlo."
                } else {
                    "Mostrando ${favoritos.size} modelos favoritos guardados en Room (disponibles sin conexión)."
                }
                aplicarFiltroLocal()
            } else {
                cargarTodosDeCache()
            }
        }
    }

    // Mejora propia: guarda o quita un modelo de favoritos en Room y refresca la lista
    fun cambiarFavorito(modelo: ModeloCache) {
        val nuevoValor = !modelo.esFavorito
        viewModelScope.launch {
            repository.cambiarFavorito(modelo.modelId, nuevoValor)

            // Actualizamos la lista en memoria para que la estrella cambie al instante en pantalla
            listaCompletaActual = if (_soloFavoritos.value == true) {
                repository.obtenerFavoritos()
            } else {
                listaCompletaActual.map {
                    if (it.modelId == modelo.modelId) it.copy(esFavorito = nuevoValor) else it
                }
            }
            aplicarFiltroLocal()
        }
    }

    // Carga directamente todos los modelos ya guardados en Room (ideal al volver del detalle o sin red)
    fun cargarTodosDeCache() {
        viewModelScope.launch {
            val guardados = if (_soloFavoritos.value == true) {
                repository.obtenerFavoritos()
            } else {
                repository.obtenerTodosGuardados()
            }
            listaCompletaActual = guardados
            _esError.value = false
            _mensajeEstado.value = if (guardados.isEmpty()) {
                "Todavía no hay datos en caché. Pulsa una marca para descargarla de Internet."
            } else if (_soloFavoritos.value == true) {
                "Mostrando ${guardados.size} modelos favoritos guardados en Room."
            } else {
                "Caché local · ${guardados.size} modelos guardados en Room listos para consultar sin conexión."
            }
            aplicarFiltroLocal()
        }
    }

    // Aplica el texto del buscador rápido sobre la lista actual
    private fun aplicarFiltroLocal() {
        if (textoFiltroActual.isEmpty()) {
            _modelosMostrados.value = listaCompletaActual
        } else {
            val q = textoFiltroActual.lowercase(Locale.ROOT)
            _modelosMostrados.value = listaCompletaActual.filter {
                it.modelName.lowercase(Locale.ROOT).contains(q) ||
                    it.makeName.lowercase(Locale.ROOT).contains(q)
            }
        }
    }
}
