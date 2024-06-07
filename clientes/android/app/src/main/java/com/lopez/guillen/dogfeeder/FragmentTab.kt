package com.lopez.guillen.dogfeeder

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.lopez.guillen.dogfeeder.model.TabPagerAdapter

/**
 * Clase FragmentTab
 * Clase que maneja la lógica de negocio para el TabLayout que basicamente, será un contenedor que se montará en el frameLayout
 * del mainActivity y contendrá dos tab.
 * 1º Fragment auditFood -> Fragment para visualizar 100 últimos registros de auditoría para el suministro de alimentos
 * 2º Fragment GraphSupplyFood -> Fragment para visualizar la gráfica de estadítica para las tomas de alimentos distribuidas por
 *                                meses del año.
 */
class FragmentTab(val _context: Activity) : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Step 1. Instanciamos los controles para el menu Tab (Pestañas "Registros" y "Estadistica") y el viewPager asociado a
        //         cada tab o pestaña.
        val tabMenu = view.findViewById<TabLayout>(R.id.tabSelectMenu)
        val viewPagerTabMenu = view.findViewById<ViewPager2>(R.id.viewPagerTabMenu)

        // Step 2. Se configura el adaptador del viewPager2 utilizado por las pestañas (Instancia de la clase personalizada
        //         TabPagerAdapter). Despues para nuestra instancia viewPagerTabMenu se establece el custom adapter obtenido.
        val pagerTabAdapter = TabPagerAdapter(requireActivity(),FragmentAuditFood(_context) ,FragmentGraphSupplyFood(_context))
        viewPagerTabMenu.adapter = pagerTabAdapter

        // Step 3. Se establece un listener para cambiar el color al seleccionar un Tab
        tabMenu.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // Cambiar el color al seleccionar un Tab
                tab.view.setBackgroundColor(Color.LTGRAY)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Restaurar el color predeterminado cuando se selecciona otro Tab
                tab.view.setBackgroundColor(Color.TRANSPARENT)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Step 4. Por útimo, se conecta el ViewPager2 con el TabLayout mediante el mediador TabLayoutMediator
        TabLayoutMediator(tabMenu, viewPagerTabMenu) { tab, position ->
            // Se asigna el texto de la pestaña según la posición de cada tab
            tab.text = if (position == 0)  getString(R.string.tab_registers) else getString(R.string.tab_statistics)
        }.attach()
    }
}