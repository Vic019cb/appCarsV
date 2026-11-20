package com.example.appcars

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Nuevo Proyecto V2: Adaptador del RecyclerView para mostrar los modelos descargados de la API (o leídos de Room).
// Sigue el mismo patrón sencillo que MantenimientoAdapter para que el código sea coherente y fácil de entender.
class CatalogoAdapter(
    private var lista: List<ModeloCache>,
    private val alPulsarModelo: (ModeloCache) -> Unit,
    private val alPulsarFavorito: (ModeloCache) -> Unit
) : RecyclerView.Adapter<CatalogoAdapter.ModeloViewHolder>() {

    // Guarda las referencias a los textos y al botón de estrella de cada fila
    class ModeloViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val textoNombre: TextView = vista.findViewById(R.id.textoNombreModeloApi)
        val textoMarca: TextView = vista.findViewById(R.id.textoMarcaModeloApi)
        val textoOrigen: TextView = vista.findViewById(R.id.textoOrigenModeloApi)
        val botonFavorito: TextView = vista.findViewById(R.id.botonFavoritoItemApi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeloViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_modelo_api, parent, false)
        return ModeloViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ModeloViewHolder, position: Int) {
        val item = lista[position]

        holder.textoNombre.text = item.modelName
        holder.textoMarca.text = "Marca: ${item.makeName} · ID: ${item.modelId}"
        holder.textoOrigen.text = if (item.esFavorito) {
            "★ Favorito · Guardado en Room (${item.fechaConsulta})"
        } else {
            "Guardado en Room (${item.fechaConsulta})"
        }

        // Mejora propia: estrella llena si es favorito, vacía si no
        holder.botonFavorito.text = if (item.esFavorito) "★" else "☆"

        // Al tocar la fila completa abrimos la pantalla de detalle ampliado (Hito 2)
        holder.itemView.setOnClickListener {
            alPulsarModelo(item)
        }

        // Al tocar solo la estrella cambiamos el estado de favorito en Room
        holder.botonFavorito.setOnClickListener {
            alPulsarFavorito(item)
        }
    }

    override fun getItemCount(): Int = lista.size

    // Actualiza la lista del RecyclerView cuando llegan nuevos datos del ViewModel
    fun actualizarDatos(nuevaLista: List<ModeloCache>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
