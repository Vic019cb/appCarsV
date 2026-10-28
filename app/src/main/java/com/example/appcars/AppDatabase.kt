package com.example.appcars

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Base de datos de Room: reúne las entidades y proporciona acceso a los DAOs.
@Database(entities = [Mantenimiento::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // Función abstracta para que Room genere la implementación del DAO
    abstract fun mantenimientoDao(): MantenimientoDao

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
                ).build()
                instancia = nueva
                nueva
            }
        }
    }
}
