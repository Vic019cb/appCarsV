package com.example.appcars

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Nuevo Proyecto V2: DAO de Room para gestionar la caché offline de los modelos descargados desde la API.
@Dao
interface ModeloCacheDao {

    // Guarda o actualiza una lista de modelos descargados de Internet (REPLACE evita errores si ya existen)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(modelos: List<ModeloCache>)

    // Recupera los modelos de una marca concreta guardados en local (para el modo sin conexión)
    @Query("SELECT * FROM modelos_cache WHERE marcaBuscada = :marca ORDER BY esFavorito DESC, modelName ASC")
    suspend fun obtenerPorMarca(marca: String): List<ModeloCache>

    // Recupera todos los modelos que alguna vez se consultaron y quedaron en Room
    @Query("SELECT * FROM modelos_cache ORDER BY esFavorito DESC, makeName ASC, modelName ASC")
    suspend fun obtenerTodosGuardados(): List<ModeloCache>

    // Mejora propia: devuelve solo los modelos marcados con estrella (favoritos)
    @Query("SELECT * FROM modelos_cache WHERE esFavorito = 1 ORDER BY makeName ASC, modelName ASC")
    suspend fun obtenerFavoritos(): List<ModeloCache>

    // Mejora propia: cambia el estado de favorito de un modelo concreto en Room
    @Query("UPDATE modelos_cache SET esFavorito = :favorito WHERE modelId = :modelId")
    suspend fun actualizarFavorito(modelId: Int, favorito: Boolean)

    // Nos dice qué IDs estaban marcados como favoritos para no perder la estrella al refrescar desde Internet
    @Query("SELECT modelId FROM modelos_cache WHERE esFavorito = 1")
    suspend fun obtenerIdsFavoritos(): List<Int>
}
