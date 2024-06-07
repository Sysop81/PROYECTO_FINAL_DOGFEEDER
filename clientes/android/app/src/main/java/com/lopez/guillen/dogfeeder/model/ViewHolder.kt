package com.lopez.guillen.dogfeeder.model

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lopez.guillen.dogfeeder.R

/**
 * Clase ViewHolder
 * Clase encargada de realizar el binding entre los registros de auditoría y los crontroles del formulario a mostrar en
 * el rv
 */
class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
    // Propiedades de clase. Controles del layout_item.xml seteado en el rv del fragment_audit_food.xml
    private var id = 0
    private val user = view.findViewById<TextView>(R.id.txtAuditUser)
    private val date = view.findViewById<TextView>(R.id.txtAuditDate)
    private val time = view.findViewById<TextView>(R.id.txtAuditTime)
    private val weight = view.findViewById<TextView>(R.id.txtTitleWeight)

    /**
     * Se encarga de mapear los datos de cada item en el recyclerView
     */
    fun bind(item: AuditFoodItem){
        id = item.id
        user.text = item.user
        date.text = item.date
        time.text = item.time
        weight.text = item.weight.toString()
    }
}