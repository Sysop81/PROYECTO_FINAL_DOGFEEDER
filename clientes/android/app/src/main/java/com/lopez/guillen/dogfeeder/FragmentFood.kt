package com.lopez.guillen.dogfeeder

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.lopez.guillen.dogfeeder.model.AuditFoodItem
import com.lopez.guillen.dogfeeder.model.Pet
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.SocketException

/**
 * Clase FragmentFood
 * Clase encargada de la lógica de negocio para la vista de suministro de alimento a la mascota asociada a DOG-FEEDER
 */
class FragmentFood(val _context: Activity) : Fragment(){
    // Propiedades de clase
    private val session = Session.getInstance()
    private lateinit var btnSetFood: Button
    private var pet : Pet? = null
    private lateinit var auditFood : AuditFoodItem
    private lateinit var wvLastSupplyFood : WebView
    private lateinit var wvHooperState : WebView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_food, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Step 0. Manejamos la visibilidad de la searchView y comprobamos si en este fragment es necesaria la acción de
        //         refresco.
        (activity as MainActivity).manageSearchView(false)
        Tools.activateMainSwipeRefresh(_context,true)

        // Step 1. Instanciado de controles de la vista y demas objetos
        sharedPreferences = view.context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        wvLastSupplyFood = view.findViewById(R.id.webViewLastSupplyFood)
        wvHooperState = view.findViewById(R.id.webViewHooperState)
        btnSetFood = view.findViewById(R.id.btnSetFood)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        // Step 2. Definición de manejadores de eventos
        btnSetFood.setOnClickListener{ handleBtnSetFood() }

        // Step 3. Obtenemos de la sesión los datos o consultamos al servidor
        session.getSessionPet()?.let { pet ->
            this.pet = pet
            setDataInForm()
        } ?: getPetData()
    }


    /**
     * Método getPetData
     * Este método se encarga de recuperar los datos almacenados de la mascota asociada al comedero
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPetData(){
        var response = Session.ServerResponseCodes.ERROR.code
        val handler = Handler(Looper.getMainLooper())
        var msg = _context.getString(R.string.info_error_connection)
        var isConError = false
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(null, Session.ServerStates.PETDATA)

                // Step 2. Obtenemos la respuesta del servidor
                response = session.readResponseFromServer()
            } catch (se: SocketException) {
                session.clientSocket.close()
                isConError = true
            } finally {
                handler.post{
                    if(!isConError){
                        if(!response.equals(Session.ServerResponseCodes.ERROR.code)){
                            // Establecemos la mascota en la sesion
                            val job = JSONObject(response)
                            session.setSessionPet(Pet(
                                job.getInt("ID"),
                                job.getString("name"),
                                job.getString("breed"),
                                job.getString("birthday"),
                                job.getDouble("weight"),
                                job.getString("type"),
                                job.getBoolean("vaccineNotify"),
                                job.getInt("vaccineDay") -1,
                                job.getInt("vaccineMonth")
                            ))
                            // Seteamos los valores en el formulario y realizamos un retorno para completar la accón
                            setDataInForm()
                            return@post
                        }else{
                            msg = getString(R.string.info_error_not_register_pet)
                        }
                    }
                    progressBar.visibility = View.GONE
                    Tools.showAlertDialog(_context,msg,R.drawable.ic_baseline_error)
                }
            }
        }
    }


    /**
     * Método setDataInForm
     * Este método se encarga de setear los valores obtenidos para la mascota y última toma de alimento registrado en la
     * vista.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataInForm(){
        // Step 1. Instanciamos la mascota desde el singletone
        pet = session.getSessionPet()
        // Step 2. Recuperamos el último registro de comida suministrado y el estado de la tolva
        getLastSupplyFoodAudit()
    }


    /**
     * Método getLastSupplyFoodAudit
     * Este método se encarga de recuperar el último registro de alimento suministrado y el estado de la tolva, con la
     * finalidad de cargar los controles de tipo webview
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLastSupplyFoodAudit(){
        var response = Session.ServerResponseCodes.ERROR.code
        val handler = Handler(Looper.getMainLooper())
        var msg = _context.getString(R.string.info_error_connection)
        var isConError = false
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(null, Session.ServerStates.LASTAUDIT)

                // Step 2. Obtenemos la respuesta del servidor
                response = session.readResponseFromServer()
            } catch (se: SocketException) {
                session.clientSocket.close()
                isConError = true
            } finally {
                handler.post{
                    progressBar.visibility = View.GONE
                    if(!isConError){
                        if(!response.equals(Session.ServerResponseCodes.ERROR.code)){
                            // Obtenemos los datos de la respuesta del servidor
                            val job = JSONObject(response)
                            // Instanciamos un registro de auditoría y el estado de la tolva
                            var hopperStatus = job.getString("hopperStatus")
                            auditFood = AuditFoodItem(
                                job.getInt("ID"),
                                job.getString("user"),
                                Tools.formatDate(job.getString("date"),"-"),
                                job.getString("time"),
                                job.getDouble("weight")
                            )

                            // Realizamos la carga de los controles webView
                            wvLastSupplyFood.loadData(Tools.generateWebViewLastSupplyFoodContent(auditFood, getString(R.string.txt_last_food_supplied, pet?.name)),
                                "text/html", "UTF-8")
                            wvHooperState.loadData(Tools.generateWebViewHopperState(hopperStatus,getString(R.string.txt_hopper_state)),
                                "text/html", "UTF-8")
                            return@post
                        }else{
                            msg = getString(R.string.info_error_not_register_pet)
                        }
                    }
                    Tools.showAlertDialog(_context,msg,R.drawable.ic_baseline_error)
                }
            }
        }
    }


    /**
     * Manejador de eventos handleBtnSetFood
     * Este manejador se encarga de gestionar el suministro de alimento a la mascota haciendo girar el servoMotor de
     * 360 grados encargado de arrastrar el alimento desde la tolva hasta el cuenco contenedor de alimento.
     */
    private fun handleBtnSetFood(){
        btnSetFood.isEnabled = false
        var statusCode = Session.ServerResponseCodes.ERROR.code
        val handler = Handler(Looper.getMainLooper())
        var msg = _context.getString(R.string.info_error_connection)
        var isConError = false
        var icon = R.drawable.ic_baseline_error
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(null, Session.ServerStates.FOOD)

                // Step 2. Obtenemos la respuesta del servidor
                statusCode = session.readResponseFromServer()
            } catch (se: SocketException) {
                session.clientSocket.close()
                isConError = true
            } finally {
                handler.post {
                    if(!isConError){
                        msg = when(statusCode){
                            Session.ServerResponseCodes.ERROR.code -> getString(R.string.info_error_food_supplied)
                            Session.ServerResponseCodes.OK.code ->  getString(R.string.info_ok_food_supplied)
                            Session.ServerResponseCodes.WAIT.code-> getString(R.string.info_wait_food_supplied)
                            Session.ServerResponseCodes.HOPPER_DANGER.code -> getString(R.string.info_error_empty_hopper)

                            else -> getString(R.string.info_error_stuck_hopper)
                        }
                        // Cambiamos el icono a info
                        if(statusCode.equals(Session.ServerResponseCodes.OK.code)) icon = R.drawable.ic_baseline_info
                    }
                    // Informamos al usuario
                    Tools.showAlertDialog(_context,msg,icon)
                    btnSetFood.isEnabled = true
                }
            }
        }
    }
}