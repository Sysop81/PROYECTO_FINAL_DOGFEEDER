package com.lopez.guillen.dogfeeder.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lopez.guillen.dogfeeder.R

/**
 * Clase AuditFoodItemAdapter
 * Adaptador para el recyclerView encargado de visualizar los registros de auditoría para el suministro de alimentos
 */
class AuditFoodItemAdapter(private val items: List<AuditFoodItem>) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Infla el diseño de nuestro layout_item para devolver finalmente el viewHolder que contiene la vista
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item,parent,false)
        return ViewHolder(view)
    }

    /**
     * Método getItemCount
     * Retorna el número de elementos totales de la lista
     * @return Int Tamaño de la lista de registros
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Método onBindViewHolder
     * Este método se encarga de asociar datos de la lista. Obtiene el item de la lista en base a su indice y lo
     * mapea en el viewHolder.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    /**
     * Método getItemAtPosition
     * Se encarga de retornar el item en base a la posición de los elementos recibida por parametro
     * @param position Valor entero que representa la posición de item en el listado
     * @return AuditFoodItem Item del registro de auditoría
     */
    fun getItemAtPosition(position: Int): AuditFoodItem {
        return items[position]
    }

    /**
     * Método getList
     * Método encargado de devolver la lista de registros
     * @return List<AuditFoodItem> Listado completo
     */
    fun getList(): List<AuditFoodItem>{
        return items
    }
}