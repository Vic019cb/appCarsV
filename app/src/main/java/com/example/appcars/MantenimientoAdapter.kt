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
        // Modificacion: Referencia al ImageView de la fila donde se muestra la foto capturada si el mantenimiento tiene una
        val ivFoto: ImageView = vista.findViewById(R.id.ivFotoFila)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MantenimientoViewHolder {
        // Inflamos el diseño individual de cada elemento de la lista.
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mantenimiento, parent, false)
        return MantenimientoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: MantenimientoViewHolder, position: Int) {
        val item = lista[position]

        // Modificacion: Si el mantenimiento tiene coordenadas GPS guardadas, las formateamos con 4 decimales como en el Bestiario
        val lat = item.latitud
        val lon = item.longitud
        val coordenadas = if (lat != null && lon != null) {
            "  (" + "%.4f".format(lat) + ", " + "%.4f".format(lon) + ")"
        } else {
            ""
        }

        // Mostramos el título con el vehículo y tipo de mantenimiento.
        holder.textoTitulo.text = "${item.vehiculo} · ${item.tipo}"
        // Modificacion: Añadimos las coordenadas al texto de detalle de la fila si existen
        holder.textoDetalle.text = "Fecha: ${item.fecha} | ${item.kilometros} km$coordenadas"

        // Formateamos el coste a moneda euros.
        val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
        holder.textoCoste.text = formatoMoneda.format(item.coste)

        // Asignamos la imagen deportiva según el tipo de vehículo (coche o moto).
        if (item.vehiculo.equals("Moto", ignoreCase = true)) {
            holder.imagenVehiculo.setImageResource(R.drawable.moto_deportiva)
        } else {
            holder.imagenVehiculo.setImageResource(R.drawable.coche_deportivo)
        }

        // Modificacion: Cargamos la foto del mantenimiento con Fotos.cargar y usamos VISIBLE / GONE obligatorio para que al reciclar filas no se mezclen fotos (Apartado 7 del PDF)
        val bitmap = Fotos.cargar(item.fotoPath)
        if (bitmap != null) {
            holder.ivFoto.setImageBitmap(bitmap)
            holder.ivFoto.visibility = View.VISIBLE
        } else {
            holder.ivFoto.visibility = View.GONE
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
