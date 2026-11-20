package com.example.appcars

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Nuevo Proyecto V2: Pequeña clase resultado que devuelve el Repository al ViewModel.
// Así la pantalla sabe si los datos vinieron de Internet o de la caché de Room (modo sin conexión).
data class ResultadoConsulta(
    val modelos: List<ModeloCache>,
    val desdeInternet: Boolean,
    val mensaje: String,
    val esError: Boolean = false
)

// Repository (Hito 3) que actúa como "cerebro" de los datos.
// Es la única clase que habla con Retrofit (Internet) y con ModeloCacheDao (Room),
// separando la lógica de acceso a datos de la interfaz de usuario.
class VehiculoRepository(
    private val dao: ModeloCacheDao
) {

    // Intenta consultar la API online; si funciona guarda en Room, y si falla lee lo guardado en Room
    suspend fun buscarModelosPorMarca(marca: String): ResultadoConsulta {
        val marcaLimpia = marca.trim()
        val marcaClave = marcaLimpia.lowercase(Locale.ROOT)

        return try {
            // 1. Llamada real a la API pública NHTSA mediante Retrofit y corrutinas
            val respuesta = RetrofitClient.api.obtenerModelosPorMarca(marcaLimpia)

            // 2. Recuperamos los favoritos actuales de Room para no perder las estrellas al actualizar
            val favoritosPrevios = dao.obtenerIdsFavoritos().toSet()
            val ahora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            // 3. Convertimos la respuesta JSON de la API a nuestra tabla ModeloCache de Room
            val listaParaRoom = respuesta.results
                .distinctBy { it.modelId }
                .map { item ->
                    ModeloCache(
                        modelId = item.modelId,
                        makeId = item.makeId,
                        makeName = item.makeName,
                        modelName = item.modelName,
                        marcaBuscada = marcaClave,
                        esFavorito = favoritosPrevios.contains(item.modelId),
                        fechaConsulta = ahora
                    )
                }

            if (listaParaRoom.isEmpty()) {
                ResultadoConsulta(
                    modelos = emptyList(),
                    desdeInternet = true,
                    mensaje = "No se encontraron modelos para \"$marcaLimpia\" en la API.",
                    esError = true
                )
            } else {
                // 4. Guardamos en Room para que estén disponibles más adelante sin conexión
                dao.guardarTodos(listaParaRoom)
                val listaOrdenada = dao.obtenerPorMarca(marcaClave)
                ResultadoConsulta(
                    modelos = listaOrdenada,
                    desdeInternet = true,
                    mensaje = "Conectado · ${listaOrdenada.size} modelos de ${marcaLimpia.uppercase(Locale.ROOT)} guardados en Room."
                )
            }
        } catch (e: Exception) {
            // 5. Si no hay Internet o el servidor falla, nunca cerramos la app: consultamos la caché local de Room
            val guardadosMarca = dao.obtenerPorMarca(marcaClave)
            if (guardadosMarca.isNotEmpty()) {
                ResultadoConsulta(
                    modelos = guardadosMarca,
                    desdeInternet = false,
                    mensaje = "Sin conexión · Mostrando ${guardadosMarca.size} modelos de ${marcaLimpia.uppercase(Locale.ROOT)} guardados en Room."
                )
            } else {
                // Si esa marca concreta no se había consultado antes, enseñamos todos los modelos que haya en caché
                val todosGuardados = dao.obtenerTodosGuardados()
                if (todosGuardados.isNotEmpty()) {
                    ResultadoConsulta(
                        modelos = todosGuardados,
                        desdeInternet = false,
                        mensaje = "Sin conexión · No había caché de \"$marcaLimpia\", mostrando ${todosGuardados.size} modelos guardados en Room."
                    )
                } else {
                    ResultadoConsulta(
                        modelos = emptyList(),
                        desdeInternet = false,
                        mensaje = "Sin conexión a Internet y todavía no hay modelos guardados en Room. Conéctate una vez para descargar el catálogo.",
                        esError = true
                    )
                }
            }
        }
    }

    // Devuelve todos los modelos guardados en Room (útil al abrir la pantalla o sin conexión)
    suspend fun obtenerTodosGuardados(): List<ModeloCache> = dao.obtenerTodosGuardados()

    // Mejora propia: devuelve solo los modelos marcados como favoritos en Room
    suspend fun obtenerFavoritos(): List<ModeloCache> = dao.obtenerFavoritos()

    // Mejora propia: marca o desmarca un modelo como favorito directamente en Room
    suspend fun cambiarFavorito(modelId: Int, esFavorito: Boolean) {
        dao.actualizarFavorito(modelId, esFavorito)
    }
}
