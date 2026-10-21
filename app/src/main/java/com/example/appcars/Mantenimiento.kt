package com.example.appcars

// Un mantenimiento guarda todos los datos que necesitamos enseñar en el historial.
data class Mantenimiento(
    val vehiculo: String,
    val tipo: String,
    val kilometros: Int,
    val fecha: String,
    val coste: Double
)
