package com.lopez.guillen.dogfeeder

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.SocketException

/**
 * Clase FragmentGraphSupplyFood
 * Clase encargada de la lógica de negocio para el fragment que maneja la vista con la gráfica de estadistica para;
 * 1º Contemos de tomas de alimento por mensualidad
 * 2º Sumatorio de gramos de alimento dispensado por mensualidad
 */
class FragmentGraphSupplyFood(val _context: Activity) : Fragment() {
    // Propiedades de clase
    private var session = Session.getInstance()
    private lateinit var progressBar: ProgressBar
    private lateinit var chartSupplyFood : BarChart
    private lateinit var radioGroupStatistics: RadioGroup
    private lateinit var radioBtnCount : RadioButton
    private lateinit var radioBtnSum : RadioButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_graph_supply_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Step 0. Manejamos la visibilidad de la searchView y comprobamos si en este fragment es necesaria la acción de
        //         refresco.
        (activity as MainActivity).manageSearchView(false)
        Tools.activateMainSwipeRefresh(_context,true)

        // Step 1. Se instancian los controles a utilizar de nuestra vista
        progressBar = view.findViewById(R.id.progressBar)
        chartSupplyFood = view.findViewById(R.id.chartSupplyFood)
        radioGroupStatistics = view.findViewById(R.id.radioGroup)
        radioBtnCount = view.findViewById(R.id.radioBtnCount)
        radioBtnSum = view.findViewById(R.id.radioBtnFoodSum)

        // Step 2. Se establece el manejador de eventos para los botones de radio encargados de seleccionar el tipo de
        //         gráfica a mostrar.
        radioGroupStatistics.setOnCheckedChangeListener { group, checkedId ->
            // Obtenemos el elemento checkeado y establecemos por defecto el tipo a SUM "Sumatorio de gramos dispensados
            // por mesualidad".
            val selectedRbtn = view.findViewById<RadioButton>(checkedId)
            var typeOfGraph = "SUM"
            // Si el radio seleccionado es tipo COUNT cambiamos el tipo de gráfica "Cantidad de tomas dispensadas por
            // mensualidad"
            if(selectedRbtn.id == radioBtnCount.id) typeOfGraph = "COUNT"
            // Finalmente realizamos la carga de datos desde el servidor
            loadData(typeOfGraph)
        }

        // Step 3. Lanzamos la gráfica al cargar el fragment con la opción "COUNT" como predeterminada
        loadData("COUNT")

    }

    /**
     * Método loadData
     * Método encargado de obtener los datos del servidor TCP para crear la gráfica de estadística.
     * @param graphType Tipo de gráfica a mostrar
     */
    private fun loadData(graphType : String){
        var response = Session.ServerResponseCodes.EMPTY_DATA.code
        val handler = Handler(Looper.getMainLooper())
        var msg = _context.getString(R.string.info_error_connection)
        var isConError = false
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(null, Session.ServerStates.GRAPHAUDITS)

                // Step 2. Obtenemos la respuesta del servidor
                response = session.readResponseFromServer()
            } catch (se: SocketException) {
                session.clientSocket.close()
                isConError = true
            } finally {
                handler.post {
                    if(!isConError){
                        if(!response.equals(Session.ServerResponseCodes.EMPTY_DATA.code)){
                            // Instanciamos el listado para mapear en la gráfica
                            val entries = ArrayList<BarEntry>()
                            // Generamos un JSON array con la respuesta del servidor TCP
                            val joArr = JSONArray(response)

                            // Recorremos el array obeniendo un JSONobject por cada iteración, en función de la gráfica a
                            // montar extraemos de los datos del array el sumartorio o el conteo de tomas
                            for(i in 0 until joArr.length()){
                                val job = joArr.getJSONObject(i)
                                var dataToShow = if(graphType.equals("SUM"))
                                                    (Tools.roundToDecimals(job.getDouble("totalWeight"),2)).toFloat()
                                                else job.getInt("CountTakes").toFloat()
                                // Finalmente añadimos al listado que se seterá en la grafica
                                entries.add(BarEntry(job.getInt("monthNumber").toFloat(),dataToShow))
                            }
                            // Realizamos la llamada al método encargado de montar la gráfica y realizamos un retorno del
                            // manejador post.
                            loadGraph(entries,graphType)
                            return@post
                        }else{
                            msg = getString(R.string.info_error_empty_graph_data)
                        }
                    }
                    // Mostramos información al usuario en caso de producirse algún error.
                    Tools.showAlertDialog(_context,msg,R.drawable.ic_baseline_error)
                }
            }
        }
    }

    /**
     * Método loadGraph
     * Método encargado de cargar la gráfica con los datos obtenidos del servidor DOG-FEEDER
     * @param entries Datos a cargar en la gráfica
     * @param graphType Tipo de gráfica a mostrar
     */
    private fun loadGraph(entries :  ArrayList<BarEntry>,graphType: String){
        // Step 1. Se establece el label para la gráfica
        val graphLabel = if(graphType.equals("SUM"))
            getString(R.string.label_graph_sum)  else getString(R.string.label_graph_count)
        // Step 2. Se crea el dataSet y se establece el color de las barras
        val dataSet = BarDataSet(entries, graphLabel)
        dataSet.color = _context.getColor(R.color.light_brown)

        // Step 3. Se transforma el dataSet
        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(dataSet)
        // Step 4. Se asocia con la gráfica
        val data = BarData(dataSets)
        chartSupplyFood.data = data

        // Step 5. Establecemos parametros de personalización sobre la gráfica
        chartSupplyFood.setFitBars(true)
        chartSupplyFood.description.isEnabled = false
        chartSupplyFood.animateY(1000)

        // Deshabilitamos la barra de progreso o spinner de carga
        progressBar.visibility = View.GONE
    }
}