package com.lopez.guillen.dogfeeder.model

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Clase TabPagerAdapter
 * Adaptador para el menu Tabs o adaptador para el viewPager2. Se encarga de alternar entre el fragment para auditorías
 * de registro de alimentos y el fragment encargado de mostrar la gráfica anual.
 */
class TabPagerAdapter(fragmentActivity: FragmentActivity, val fragmentAuditFood: Fragment, val fragmentGraphSupplyFood : Fragment) : FragmentStateAdapter(fragmentActivity) {

    // Devuelve el total de pestañas del menutab
    override fun getItemCount(): Int {
        return 2 // [HARDCORED] Al ser simplemente dos pestañas.
    }

    // Carga el fragment asociado a la pestaña pulsada por el usuario.
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> fragmentAuditFood
            1 -> fragmentGraphSupplyFood
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}