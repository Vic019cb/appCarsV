package com.example.appcars

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Nuevo Proyecto V2: Cliente Singleton de Retrofit que centraliza la conexión con el servidor de NHTSA.
// Al ser un 'object', solo se crea una vez en toda la app y ahorra memoria.
object RetrofitClient {

    // Dirección base de la API pública y gratuita (no requiere clave ni registro)
    private const val BASE_URL = "https://vpic.nhtsa.dot.gov/"

    // Instancia lista para usar con el convertidor Gson que pasa el JSON a clases Kotlin
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
