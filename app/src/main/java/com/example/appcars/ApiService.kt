package com.example.appcars

import retrofit2.http.GET
import retrofit2.http.Path

// Nuevo Proyecto V2: Interfaz de Retrofit donde definimos las rutas (endpoints) de la API pública NHTSA.
// No hace falta programar la conexión a mano: Retrofit genera el código automáticamente.
interface ApiService {

    // Petición GET con corrutina (suspend) para traer los modelos de una marca sin bloquear la pantalla
    @GET("api/vehicles/GetModelsForMake/{marca}?format=json")
    suspend fun obtenerModelosPorMarca(
        @Path("marca") marca: String
    ): RespuestaVehiculos
}
