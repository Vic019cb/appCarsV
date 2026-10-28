package com.example.appcars

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

// Adaptador para el RecyclerView que enlaza cada registro de mantenimiento con su diseño visual.
// Pasamos una segunda lambda para gestionar la pulsación larga y poder borrar el registro.
class MantenimientoAdapter(
    private var lista: List<Mantenimiento>,
    private val alPulsarElemento: (Mantenimiento) -> Unit,
    private val alMantenerPulsado: (Mantenimiento) -> Unit
) : RecyclerView.Adapter<MantenimientoAdapter.MantenimientoViewHolder>() {

    // ViewHolder que almacena las referencias a las vistas de cada fila.
    class MantenimientoViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val imagenVehiculo: ImageView = vista.findViewById(R.id.imagenItemVehiculo)
        val textoTitulo: TextView = vista.findViewById(R.id.textoTituloItem)
        val textoDetalle: TextView = vista.findViewById(R.id.textoDetalleItem)
        val textoCoste: TextView = vista.findViewById(R.id.textoCosteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MantenimientoViewHolder {
        // Inflamos el diseño individual de cada elemento de la lista.
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mantenimiento, parent, false)
        return MantenimientoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: MantenimientoViewHolder, position: Int) {
        val item = lista[position]

        // Mostramos el título con el vehículo y tipo de mantenimiento.
        holder.textoTitulo.text = "${item.vehiculo} · ${item.tipo}"
        holder.textoDetalle.text = "Fecha: ${item.fecha} | ${item.kilometros} km"

        // Formateamos el coste a moneda euros.
        val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
        holder.textoCoste.text = formatoMoneda.format(item.coste)

        // Asignamos la imagen deportiva según el tipo de vehículo (coche o moto).
        if (item.vehiculo.equals("Moto", ignoreCase = true)) {
            holder.imagenVehiculo.setImageResource(R.drawable.moto_deportiva)
        } else {
            holder.imagenVehiculo.setImageResource(R.drawable.coche_deportivo)
        }

        // Al hacer clic normal en la fila, abrimos los detalles.
        holder.itemView.setOnClickListener {
            alPulsarElemento(item)
        }

        // Al mantener pulsada la fila, ejecutamos la acción de borrado (requisito 8 del reto).
        holder.itemView.setOnLongClickListener {
            alMantenerPulsado(item)
            true
        }
    }

    override fun getItemCount(): Int = lista.size

    // Permite actualizar los datos del listado cuando cambia la base de datos Room.
    fun actualizarDatos(nuevaLista: List<Mantenimiento>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
