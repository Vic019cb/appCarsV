package com.example.appcars

import com.google.gson.annotations.SerializedName

// Nuevo Proyecto V2: Clases de datos para traducir el JSON que nos devuelve la API pública NHTSA VPIC.
// Cada campo con @SerializedName coincide exactamente con el nombre que viene en la respuesta de Internet.

// Representa cada modelo de vehículo individual dentro de la lista "Results" de la API
data class ModeloVehiculo(
    @SerializedName("Make_ID")
    val makeId: Int,

    @SerializedName("Make_Name")
    val makeName: String,

    @SerializedName("Model_ID")
    val modelId: Int,

    @SerializedName("Model_Name")
    val modelName: String
)

// Representa el paquete completo que envía el servidor (total, mensaje y lista de modelos)
data class RespuestaVehiculos(
    @SerializedName("Count")
    val count: Int,

    @SerializedName("Message")
    val message: String,

    @SerializedName("Results")
    val results: List<ModeloVehiculo>
)
