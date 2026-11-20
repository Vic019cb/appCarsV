package com.example.appcars

import androidx.room.Entity
import androidx.room.PrimaryKey

// Nuevo Proyecto V2: Entidad de Room para guardar en el móvil los modelos consultados en la API.
// Así, si nos quedamos sin Internet o ponemos el modo avión, podemos seguir viendo los datos guardados.
@Entity(tableName = "modelos_cache")
data class ModeloCache(
    // Usamos el Model_ID oficial de la API como clave primaria para no duplicar modelos
    @PrimaryKey val modelId: Int,
    val makeId: Int,
    val makeName: String,
    val modelName: String,
    // Guardamos la marca buscada en minúsculas para encontrarla rápido en modo sin conexión
    val marcaBuscada: String,
    // Mejora propia: permite marcar modelos como favoritos y conservarlos en Room
    val esFavorito: Boolean = false,
    // Fecha y hora en la que se descargó el modelo desde Internet (se enseña en el detalle)
    val fechaConsulta: String
)
