package com.example.appcars

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Base de datos de Room: reúne las entidades y proporciona acceso a los DAOs.
// Modificacion: Anteriormente subimos a versión 2 tras añadir latitud, longitud y fotoPath a Mantenimiento.
// Nuevo Proyecto V2: Añadimos la tabla ModeloCache y subimos la versión a 3 para guardar en local los datos de la API.
@Database(entities = [Mantenimiento::class, ModeloCache::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    // Función abstracta para que Room genere la implementación del DAO de mantenimientos
    abstract fun mantenimientoDao(): MantenimientoDao

    // Nuevo Proyecto V2: DAO para guardar y leer los modelos de la API en modo sin conexión
    abstract fun modeloCacheDao(): ModeloCacheDao

    companion object {
        // @Volatile asegura que el valor sea inmediatamente visible en todos los hilos
        @Volatile
        private var instancia: AppDatabase? = null

        // Patrón Singleton: evita crear múltiples conexiones pesadas a la base de datos
        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                val nueva = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "appcars_database"
                )
                    // Modificacion: Evita que la app se cierre por error de migración si cambia la versión de la tabla
                    .fallbackToDestructiveMigration(true)
                    .build()
                instancia = nueva
                nueva
            }
        }
    }
}
