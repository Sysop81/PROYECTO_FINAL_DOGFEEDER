package com.lopez.guillen.dogfeeder

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.lopez.guillen.dogfeeder.model.DogFeederSettings
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.SocketException

/**
 * Clase FragmentSettings
 * Clase encargada de manejar la lógica de negocio para la vista de parámetros de configuración generales
 */
class FragmentSettings(val _context: Activity) : Fragment() {
    // Propiedades de clase
    private var session = Session.getInstance()
    private lateinit var settings : DogFeederSettings
    private lateinit var progressBar: ProgressBar
    private lateinit var inSwitchLedOn : Switch
    private lateinit var inSwitchNotifyHopperLow : Switch
    private lateinit var inSwitchNotifyFeederEmpty :Switch
    private lateinit var btnWeightStop : Button
    private lateinit var btnReport : Button
    private lateinit var inMaxFoodRation : Spinner
    private var loading : Boolean = true
    private lateinit var rationList : List<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Step 0. Manejamos la visibilidad de la searchView y comprobamos si en este fragment es necesaria la acción de
        //         refresco.
        (activity as MainActivity).manageSearchView(false)
        Tools.activateMainSwipeRefresh(_context,true)

        // Step 1. Instanciado de controles del formulario
        progressBar = view.findViewById(R.id.progressBar)
        inSwitchLedOn = view.findViewById(R.id.switchLedOn)
        inSwitchNotifyHopperLow = view.findViewById(R.id.switchNotifyHopperLow)
        inSwitchNotifyFeederEmpty = view.findViewById(R.id.switchNotifyFeederEmpty)
        inMaxFoodRation = view.findViewById(R.id.spinnerMaxFoodRation)
        btnReport = view.findViewById(R.id.btnReport)
        btnWeightStop = view.findViewById(R.id.btnWeightStop)
        rationList = listOf("100","130","150","180","200","230","250","280","300")

        // Step 2. Cargas de datos y manejadores de eventos
        Tools.loadSpinner(inMaxFoodRation, rationList,_context)

        // Step 3. Obtenemos de la sesión los datos o consultamos al servidor
        getData()

        // step 4. Definicion de manejadores de eventos
        btnReport.setOnClickListener{ handleGetReport() }
        btnWeightStop.setOnClickListener{ handleWeightStop() }
        inSwitchLedOn.setOnCheckedChangeListener { _, isChecked ->
            if(!loading) setGenericConfig(isChecked,Session.ServerStates.LED)
        }
        inSwitchNotifyHopperLow.setOnCheckedChangeListener { _, isChecked ->
            if(!loading) setGenericConfig(isChecked,Session.ServerStates.HOPPERLOW)
        }
        inSwitchNotifyFeederEmpty.setOnCheckedChangeListener { _, isChecked ->
            if(!loading) setGenericConfig(isChecked,Session.ServerStates.FEEDEREMPTY)
        }
    }


    /**
     * Manejador de eventos handleGetReport
     * Este método se encarga de manejar la petición del usuario logueado para la obtención del reporte. El reporte se envía
     * por mail al correo del usuario logueado.
     */
    private fun handleGetReport(){
        progressBar.visibility = View.VISIBLE
        btnReport.setText(getString(R.string.txt_settings_get_get_report_wait))
        btnReport.isEnabled = false
        var response = Session.ServerResponseCodes.ERROR.code
        val handler = Handler(Looper.getMainLooper())
        var msg = _context.getString(R.string.info_error_connection)
        var isConError = false
        var icon = R.drawable.ic_baseline_error
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(null, Session.ServerStates.REPORT)

                // Step 2. Obtenemos la respuesta del servidor
                response = session.readResponseFromServer()
            } catch (se: SocketException) {
                session.clientSocket.close()
                isConError = true
            } finally {
                handler.post {
                    progressBar.visibility = View.GONE
                    if(!isConError){
                        if(!response.equals(Session.ServerResponseCodes.ERROR.code)){
                            msg = getString(R.string.info_ok_setting_request_report)
                            icon = R.drawable.ic_baseline_info
                        }else msg = getString(R.string.info_error_setting_request_report)
                    }
                    Tools.showAlertDialog(_context,msg,icon)
                    btnReport.setText(getString(R.string.txt_settings_get_get_report))
                    btnReport.isEnabled = true
                }
            }
        }
    }


    /**
     * Manejador de eventos handleWeightStop
     * Método encargado de manejar la solicitud para establecer un nuevo tope para el alimento a suministrar al comedero
     */
    private fun handleWeightStop(){
        btnWeightStop.isEnabled = false
        var response = Session.ServerResponseCodes.ERROR.code
        val handler = Handler(Looper.getMainLooper())
        var msg = _context.getString(R.string.info_error_connection)
        var isConError = false
        var icon = R.drawable.ic_baseline_error
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(inMaxFoodRation.selectedItem.toString(), Session.ServerStates.MAXFOODRATION)

                // Step 2. Obtenemos la respuesta del servidor
                response = session.readResponseFromServer()
            } catch (se: SocketException) {
                session.clientSocket.close()
                isConError = true
            } finally {
                handler.post {
                    if(!isConError){
                        if(!response.equals(Session.ServerResponseCodes.ERROR.code)) {
                            msg = getString(R.string.info_ok_setting_post)
                            icon = R.drawable.ic_baseline_info
                        } else msg = getString(R.string.info_error_setting_post)
                    }
                    Tools.showAlertDialog(_context,msg,icon)
                    btnWeightStop.isEnabled = true
                }
            }
        }
    }


    /**
     * Método setGenericConfig
     * Método encargado de setear de forma generica una configuracion aplicada, se utiliza para los input de tipo switch
     * del formulario de configuración. Al método llega el estado verdareo o falso para activar o desativar y el estado
     * de la maquina de estados para ir al ENDPOINT correcto del servicio
     */
    private fun setGenericConfig(state : Boolean, serverState : Session.ServerStates){
        var response = Session.ServerResponseCodes.ERROR.code
        val handler = Handler(Looper.getMainLooper())
        var msg = _context.getString(R.string.info_error_connection)
        var isConError = false
        var icon = R.drawable.ic_baseline_error
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(state.toString(), serverState)

                // Step 2. Obtenemos la respuesta del servidor
                response = session.readResponseFromServer()
            } catch (se: SocketException) {
                session.clientSocket.close()
                isConError = true
            } finally {
                handler.post {
                    if(!isConError){
                        if(!response.equals(Session.ServerResponseCodes.ERROR.code)){
                            msg = getString(R.string.info_ok_setting_post)
                            icon = R.drawable.ic_baseline_info
                        } else getString(R.string.info_error_setting_post)
                    }
                    Tools.showAlertDialog(_context,msg,icon)
                }
            }
        }
    }


    /**
     * Método getData
     * ESte método se encarga de realizar la petición de datos de configuración a servicio DOGFEEDER con el objeto de
     * cargar el formulario de la vista.
     */
    private fun getData(){
        progressBar.visibility = View.VISIBLE

        var response = Session.ServerResponseCodes.ERROR.code
        val handler = Handler(Looper.getMainLooper())
        var msg = _context.getString(R.string.info_error_connection)
        var isConError = false
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(null, Session.ServerStates.SETTINGSDATA)

                // Step 2. Obtenemos la respuesta del servidor
                response = session.readResponseFromServer()
            } catch (se: SocketException) {
                session.clientSocket.close()
                isConError = true
            } finally {
                handler.post {
                    progressBar.visibility = View.GONE
                    if(!isConError){
                        if(!response.equals(Session.ServerResponseCodes.ERROR.code)){

                            // Creamos la instancia JSONObject para manejar los datos obtenidos.
                            // Instanciamos un objeto Pet y lo ssseteamos en el formulario
                            val job = JSONObject(response)
                            settings = DogFeederSettings(
                                job.getInt("ID"),
                                job.getInt("foodRation"),
                                job.getBoolean("isLedOn"),
                                job.getBoolean("isNotifyHopperLow"),
                                job.getBoolean("isNotifyFeederWithOutFood")
                            )
                            setDataInForm(settings)
                            return@post
                        }else{
                            msg = getString(R.string.info_error_setting_post)
                        }
                    }
                    Tools.showAlertDialog(_context,msg,R.drawable.ic_baseline_error)
                }
            }
        }
    }

    /**
     * Método setDataInForm
     * Este método se encarga de establecer los valores recuperados desde la configuración establecida en DOGFEEDER en
     * el formulario de la vista.
     * @param _settings Instancia que representa los parametros de configuración de DOGFEEDER
     */
    private fun setDataInForm(_settings : DogFeederSettings){
        inSwitchLedOn.isChecked = _settings.ledOn
        inSwitchNotifyHopperLow.isChecked = _settings.notifyHopperLow
        inSwitchNotifyFeederEmpty.isChecked = _settings.notifyEmptyFeeder
        inMaxFoodRation.setSelection( rationList.indexOf(settings.foodRation.toString()))
        loading = false;
    }

}