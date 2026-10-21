package com.example.appcars

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Centralizamos aquí la lectura y escritura para que las pantallas usen los mismos datos.
object DatosMantenimiento {
    private const val ARCHIVO = "appCars_datos"
    private const val CLAVE = "mantenimientos"

    fun obtener(context: Context): MutableList<Mantenimiento> {
        val texto = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .getString(CLAVE, "[]") ?: "[]"
        val lista = mutableListOf<Mantenimiento>()
        try {
            val json = JSONArray(texto)
            for (i in 0 until json.length()) {
                val item = json.getJSONObject(i)
                lista.add(Mantenimiento(
                    item.optString("vehiculo"), item.optString("tipo"),
                    item.optInt("kilometros"), item.optString("fecha"),
                    item.optDouble("coste")
                ))
            }
        } catch (_: Exception) {
            // Si hubiera datos antiguos dañados, arrancamos con un historial vacío en vez de cerrar la app.
        }
        return lista
    }

    fun guardar(context: Context, mantenimiento: Mantenimiento) {
        val lista = obtener(context)
        lista.add(mantenimiento)
        val json = JSONArray()
        lista.forEach { dato ->
            json.put(JSONObject().apply {
                put("vehiculo", dato.vehiculo)
                put("tipo", dato.tipo)
                put("kilometros", dato.kilometros)
                put("fecha", dato.fecha)
                put("coste", dato.coste)
            })
        }
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .edit().putString(CLAVE, json.toString()).apply()
    }
}
