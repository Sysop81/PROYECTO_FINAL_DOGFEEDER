package com.lopez.guillen.dogfeeder

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lopez.guillen.dogfeeder.model.AuditFoodItem
import com.lopez.guillen.dogfeeder.model.AuditFoodItemAdapter
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.SocketException

/**
 * Clase FragmentAuditFood
 * Clase encargada de manejar la lógica de negocio para la vista encargada de manejar los registros de auditoría para el
 * suminsitro de alimento a la mascota asociada a DOG-FEEDER
 */
class FragmentAuditFood(val _context: Activity) : Fragment(){
    // Propiedades de clase
    private var session = Session.getInstance();
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var auditFoodData: MutableList<AuditFoodItem>
    private lateinit var dataBackup: MutableList<AuditFoodItem>
    private lateinit var progressBar: ProgressBar
    private lateinit var rv: RecyclerView
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audit_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Step 0. Manejamos la visibilidad de la searchView y la instancia de la misma
        val mainActivity = activity as MainActivity;
        mainActivity.manageSearchView(true)
        searchView = mainActivity.getSearchView()

        // Step 1. Se instancian los controles a utilizar de nuestra vista
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        progressBar = view.findViewById(R.id.progressBar)
        rv = view.findViewById(R.id.recyclerView)

        // Step 2. Establecemos manejadores de eventos. Deshabilitamos la función de refresco de nuestro Activity para
        //         únicamente dejar en funcionamiento el swipe del rv
        Tools.activateMainSwipeRefresh(_context,false)

        swipeRefreshLayout.setOnRefreshListener{ getAuditData() }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(email: String?): Boolean {
                filterDataByEmail(email)
                return true
            }

        })

        // Step 2. Cargamos los datos de auditoría en el recyclerView
        getAuditData()
    }


    /**
     * Método getAuditData
     * Método encargado de lanzar una corrutina para lanzar uns request al servidor TCP y así poder obtener los registros
     * de auditoría para el suministro de alimentos.
     */
    private fun getAuditData(){

        // Instanciamos las listas mutables con una lista vacia
        auditFoodData = mutableListOf()
        dataBackup = mutableListOf()

        // Visualizamos nuestro spinner de carga para el recyclerView
        if(!this.swipeRefreshLayout.isRefreshing) this.progressBar.visibility = View.VISIBLE
        var response = Session.ServerResponseCodes.EMPTY_DATA.code
        val handler = Handler(Looper.getMainLooper())
        var msg = _context.getString(R.string.info_error_connection)
        var isConError = false
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(null, Session.ServerStates.AUDITS)

                // Step 2. Obtenemos la respuesta del servidor
                response = session.readResponseFromServer()
            } catch (se: SocketException) {
                session.clientSocket.close()
                isConError = true
            } finally {
                handler.post {
                    if (!swipeRefreshLayout.isRefreshing) progressBar.visibility = View.GONE
                    if (!isConError) {
                        if (!response.equals(Session.ServerResponseCodes.EMPTY_DATA.code)) {
                            // Generamos un JSON array con los registro recibidos
                            val joArr = JSONArray(response)
                            // Recorremos hasta el final instanciando en cada iteración JSONobject mediante el cual
                            // generaremos una instancia AuditFoodItem que llevaremos a la lista mutable a mapear en el
                            // recyclerview
                            for(i in 0 until joArr.length()){
                                val job = joArr.getJSONObject(i)
                                val auditItem = AuditFoodItem(
                                    job.getInt("ID"),
                                    job.getString("user"),
                                    Tools.formatDate(job.getString("date"),"-"),
                                    job.getString("time"),
                                    Tools.roundToDecimals(job.getDouble("weight"),2)
                                )
                                // Se añade cada instancia a las listas mutables
                                auditFoodData.add(auditItem)
                                dataBackup.add(auditItem)
                            }
                            // Caragamos el adaptador de la vista del RV con los registros
                            rv.adapter = loadAuditData()
                            rv.layoutManager = LinearLayoutManager(rv.context)
                            swipeRefreshLayout.isRefreshing = false
                            return@post

                        } else {
                            msg = getString(R.string.info_error_empty_audit_data)
                        }
                        Tools.showAlertDialog(_context, msg, R.drawable.ic_baseline_error)
                    }
                }
            }
        }
    }


    /**
     * Método loadPulseData
     * Este método devuelve un objeto de tipo AuditFoodItemAdapter que se corresponde con los datos y vista establecidos para los
     * registros de tomas de alimentos auditados para la mascota.
     * @return AuditFoodItemAdapter Adaptador para el RV
     */
    private fun loadAuditData(): AuditFoodItemAdapter {
        // Step 1. Datos del adaptador. Obtenemos nuestro listado de items
        var adapter = AuditFoodItemAdapter(auditFoodData) ;

        // Retornamos el adaptador
        return adapter
    }


    /**
     * Método filterDataByEmail
     * Este método se encarga de filtrar las actividades por el email de usuario.
     * @param query Cadena de texto procedente de la searchBar
     */
    private fun filterDataByEmail(query: String?){
        auditFoodData.clear()
        if(query.isNullOrEmpty()){
            // Reestablecemos todos los valores desde nuestro backup de la lista
            auditFoodData.addAll(dataBackup)
        } else{
            // Filtramos los registros en base a la query string con el valor introducido en la searchbar
            auditFoodData.addAll(dataBackup.filter { item -> item.user.lowercase().startsWith(query.lowercase()) })
            // Seteamos los registros en el adapter
            rv.adapter = AuditFoodItemAdapter(auditFoodData)
        }
    }

}