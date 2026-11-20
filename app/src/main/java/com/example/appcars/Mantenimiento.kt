package com.example.appcars

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entidad propia adaptada al tema de nuestra app (mantenimientos de vehículos).
// Con @Entity le indicamos a Room que esta clase representa una tabla en SQLite.
@Entity(tableName = "mantenimientos")
data class Mantenimiento(
    // Clave primaria única autogenerada (1, 2, 3...)
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehiculo: String,
    val tipo: String,
    val kilometros: Int,
    val fecha: String,
    val coste: Double,
    // Modificacion: Campos opcionales con valor por defecto null para guardar las coordenadas GPS y la ruta de la foto en Room
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoPath: String? = null
)
