package com.example.appcars

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class ProximaRevisionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proxima_revision)
        findViewById<Button>(R.id.botonVolverRevision).setOnClickListener { finish() }

        val resultado = findViewById<TextView>(R.id.textoProximaRevision)

        // Consultamos la base de datos Room para calcular la fecha del último mantenimiento registrado
        lifecycleScope.launch {
            val dao = AppDatabase.obtener(this@ProximaRevisionActivity).mantenimientoDao()
            // Como obtenerTodas() devuelve ORDER BY id DESC, el primer elemento es el más reciente
            val mantenimientos = dao.obtenerTodas()
            val ultimo = mantenimientos.firstOrNull()

            if (ultimo == null) {
                resultado.text = "Todavía no hay mantenimientos registrados en la base de datos. Añade uno primero."
                return@launch
            }

            val tipo = ultimo.tipo.lowercase(Locale.ROOT)
            // Las ruedas se controlan por kilometraje; las demás revisiones por fecha.
            if (tipo.contains("ruedas")) {
                val objetivo = ultimo.kilometros + 40000
                val faltan = (objetivo - ultimo.kilometros).coerceAtLeast(0)
                resultado.text = "Último registro: ${ultimo.vehiculo}, ${ultimo.tipo}, ${ultimo.fecha}.\n\nPróximo cambio orientativo de ruedas: a los $objetivo km.\nDesde el registro faltan $faltan km. Actualiza el registro cuando cambie el kilometraje para recalcularlo.\n\nEs una orientación; sigue las indicaciones del fabricante."
                return@launch
            }

            val anos = when {
                tipo.contains("anual") -> 1
                tipo.contains("luces") -> 2
                tipo.contains("piezas") -> 3
                else -> 0
            }

            if (anos == 0) {
                resultado.text = "Último registro: ${ultimo.vehiculo}, ${ultimo.tipo}, ${ultimo.fecha}.\n\nPara ‘Otras reparaciones’ no se fija un plazo automático. Consulta la recomendación del taller o del fabricante."
                return@launch
            }

            try {
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }
                val fechaRegistro = formato.parse(ultimo.fecha) ?: throw IllegalArgumentException()
                val siguiente = Calendar.getInstance().apply { time = fechaRegistro; add(Calendar.YEAR, anos) }
                val hoy = Calendar.getInstance()
                val dias = TimeUnit.MILLISECONDS.toDays(siguiente.timeInMillis - hoy.timeInMillis)
                val fechaSiguiente = formato.format(siguiente.time)
                val detalle = if (tipo.contains("piezas")) "Aviso orientativo de garantía de piezas a los 3 años; revisa las condiciones de la garantía." else "Fecha orientativa para la próxima revisión."
                resultado.text = "Último registro: ${ultimo.vehiculo}, ${ultimo.tipo}, ${ultimo.fecha}.\n\nPróxima fecha orientativa: $fechaSiguiente\n${if (dias >= 0) "Faltan $dias días." else "La fecha orientativa pasó hace ${-dias} días."}\n\n$detalle\n\nSigue siempre el plan de mantenimiento del fabricante o taller."
            } catch (_: Exception) {
                resultado.text = "No se pudo interpretar la fecha del último mantenimiento. Registra la fecha con formato dd/MM/aaaa."
            }
        }
    }
}
