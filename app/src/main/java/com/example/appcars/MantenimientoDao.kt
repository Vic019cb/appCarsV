package com.example.appcars

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

// Interfaz DAO (Data Access Object) con las operaciones que nos pide el guion de Room.
@Dao
interface MantenimientoDao {

    // 1. Insertar un nuevo mantenimiento
    @Insert
    suspend fun insertar(mantenimiento: Mantenimiento)

    // 2. Obtener todos los mantenimientos ordenados del último al primero
    @Query("SELECT * FROM mantenimientos ORDER BY id DESC")
    suspend fun obtenerTodas(): List<Mantenimiento>

    // 3. Borrar un mantenimiento concreto (para cumplir el reto de la sesión)
    @Delete
    suspend fun borrar(mantenimiento: Mantenimiento)

    // Operación extra para saber cuántos mantenimientos hay en la base de datos
    @Query("SELECT COUNT(*) FROM mantenimientos")
    suspend fun contarMantenimientos(): Int
}
