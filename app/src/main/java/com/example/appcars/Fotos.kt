package com.example.appcars

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// Modificacion: Objeto ayudante Fotos copiado del Bestiario y adaptado al paquete de nuestra app para gestionar archivos de cámara y galería
object Fotos {

    // Modificacion: Prepara un archivo vacío en la carpeta interna "fotos" donde la cámara guardará la imagen
    fun crearArchivo(context: Context): File {
        val carpeta = File(context.filesDir, "fotos")
        carpeta.mkdirs()
        return File(carpeta, "foto_" + System.currentTimeMillis() + ".jpg")
    }

    // Modificacion: Convierte el archivo en una dirección Uri segura que la aplicación de cámara entiende mediante FileProvider
    fun uriDe(context: Context, archivo: File): Uri {
        return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", archivo)
    }

    // Modificacion: Copia una imagen elegida de la galería a un archivo propio de la app y devuelve su ruta completa
    fun copiarDesdeUri(context: Context, uri: Uri): String? {
        return try {
            val destino = crearArchivo(context)
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            }
            destino.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    // Modificacion: A partir de una ruta guardada, recupera la imagen reducida (1/4 de tamaño) para enseñarla sin agotar la memoria
    fun cargar(ruta: String?): Bitmap? {
        if (ruta == null) return null
        if (!File(ruta).exists()) return null
        val opciones = BitmapFactory.Options()
        opciones.inSampleSize = 4
        return BitmapFactory.decodeFile(ruta, opciones)
    }
}
