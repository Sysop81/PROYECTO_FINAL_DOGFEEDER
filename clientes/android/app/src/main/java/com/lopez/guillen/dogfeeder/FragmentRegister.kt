package com.lopez.guillen.dogfeeder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textfield.TextInputLayout
import com.lopez.guillen.dogfeeder.model.RefreshInteraction
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.model.User
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketException


/**
 * Clase FragmentRegister
 * Clase encargada de manejar la lógica de negocio para la vista de registro
 */
class FragmentRegister() : Fragment(), RefreshInteraction {
    // Propiedades de clase
    private var session = Session.getInstance()
    private lateinit var _context: Activity
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh : SwipeRefreshLayout
    private lateinit var btnLogin : Button
    private lateinit var btnRegister : Button
    private lateinit var btnVerifyCode : Button
    private lateinit var titleEmail : TextView
    private lateinit var email: TextInputLayout
    private lateinit var titleVerifyCode : TextView
    private lateinit var verifyCode : TextInputLayout
    private lateinit var user: User
    private var isSendVerifyCode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loginActivity = activity as LoginActivity;
        this._context = loginActivity.getLoginContext()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Step 0. Carga de preferencias y swipe
        sharedPreferences = view.context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        swipeRefresh = view.findViewById(R.id.swipeRegisterFragment)

        // Step 1. Se obtiene y se deshabilita el botón de login. Además, se le añade su manejador de eventos.
        btnLogin = view.findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener{ handleBtnLogin() }
        btnLogin.isEnabled = false

        // Step 2. Se instancia el boton de registro y se establece su manejador de eventos
        btnRegister = view.findViewById(R.id.btnRegister)
        btnRegister.setOnClickListener{ handleBtnRegister() }

        // Step 3. Se instancia el botón para solicitar el código de validación
        btnVerifyCode = view.findViewById(R.id.btnGetVerifiCode)
        btnVerifyCode.setOnClickListener{ handleVerifyCode() }
        btnVerifyCode.isEnabled = false

        // Step 4. Se instancia el input para la entrada del correo electronico y un manejador de eventos para validar
        titleEmail = view.findViewById(R.id.textEmail)
        email = view.findViewById(R.id.txtRegisterEmail)
        email.editText?.doOnTextChanged { text, _, _, _ ->
            if (Tools.isValidEmail(text.toString())) {
                email.error = null
                btnVerifyCode.isEnabled = true
            } else {
                email.error = getString(R.string.form_error_email)
                btnVerifyCode.isEnabled = false
            }
        }

        // Step 5. Se instancian el resto de controles y se establece un manejador de eventos para cuando cambia el texto
        //         que se inserta en el input para el código de verificación
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE
        verifyCode = view.findViewById(R.id.txtRegisterVerifyCode)
        titleVerifyCode = view.findViewById(R.id.textVerifyCode)
        verifyCode.editText?.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                verifyCode.error = null
                btnVerifyCode.isEnabled = true
            } else {
                verifyCode.error = getString(R.string.form_error_not_empty_code)
                btnVerifyCode.isEnabled = false
            }
        }

        // Step 6. Se establece el manejador de eventos para el swipe del fragment
        swipeRefresh.setOnRefreshListener { handleRefreshLayout() }
    }


    /**
     * Manejador handleRefreshLayout
     * Manejador encargado de atender el refresco de la vista
     */
    override fun handleRefreshLayout() {
        val handler = Handler(Looper.getMainLooper())
        var msg = getString(R.string.info_ok_connection)
        var icon = R.drawable.ic_baseline_info
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                session.checkServerConnection(_context)
            }catch(e : Exception){
                msg = getString(R.string.info_error_connection)
                icon = R.drawable.ic_baseline_error
            }finally {
                handler.post{
                    Tools.showAlertDialog(_context,msg,icon)
                    swipeRefresh.isRefreshing = false
                }
            }
        }
    }


    /**
     * Manejador de eventos handleVerifyCode
     * Método encargado de manejar la solicitud de verificación de email, asi como la comprobación del mismo para que
     * un usuario pueda registrarse en DOG-FEEDER
     */
    private fun handleVerifyCode(){
        progressBar.visibility = View.VISIBLE
        var msg = _context.getString(R.string.info_error_connection)
        var statusCode = Session.ServerResponseCodes.ERROR.code
        val handler = Handler(Looper.getMainLooper())
        var isConError = false
        if(!isSendVerifyCode) {
            // Solicitud de envío de código
            val userMail = email.editText?.text.toString()
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Step 1.  Realizamos el envío de datos al servidor
                    session.sendRequestToServer(userMail, Session.ServerStates.REGISTER)

                    // Step 2. Obtenemos la respuesta del servidor
                    statusCode = session.readResponseFromServer()
                } catch (se: SocketException) {
                    session.clientSocket.close()
                    isConError = true
                } catch (e: UninitializedPropertyAccessException) {
                    isConError = true
                } finally {
                    handler.post {
                        progressBar.visibility = View.GONE
                        if (!isConError) {
                            if (statusCode.equals(Session.ServerResponseCodes.OK.code)) {

                                // Elementos a ocultar
                                email.visibility = View.INVISIBLE
                                titleEmail.visibility = View.INVISIBLE

                                // Elementos visibles
                                titleVerifyCode.visibility = View.VISIBLE
                                verifyCode.visibility = View.VISIBLE
                                btnVerifyCode.text = getString(R.string.btn_verify_code)

                                // Cambiamos el comportamiento del método que maneja el botón envio y recepción
                                isSendVerifyCode = true

                                return@post
                            }else if(statusCode.equals(Session.ServerResponseCodes.EMPTY_DATA.code)){
                                msg = getString(R.string.info_user_registeded)
                            }else {
                                msg = getString(R.string.info_error_code_not_send)
                            }
                        }
                        Tools.showAlertDialog(_context, msg, R.drawable.ic_baseline_error)
                    }
                }
            }
        }else{
            // Código recepcionado
            val code = verifyCode.editText?.text.toString()
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Step 1.  Realizamos el envío de datos al servidor
                    session.sendRequestToServer(code, null)

                    // Step 2. Obtenemos la respuesta del servidor
                    statusCode = session.readResponseFromServer()
                }catch(se : SocketException){
                    isConError = true
                }finally {
                    handler.post{
                        progressBar.visibility = View.GONE
                        if(!isConError){
                            if(statusCode.equals(Session.ServerResponseCodes.OK.code)){

                                // Elementos deshabilitados
                                verifyCode.isEnabled = false
                                btnVerifyCode.visibility = View.INVISIBLE

                                // Elemtos visibles
                                btnRegister.visibility = View.VISIBLE
                                _context.findViewById<TextView>(R.id.textPassword).visibility = View.VISIBLE
                                _context.findViewById<TextInputLayout>(R.id.txtPassword).visibility = View.VISIBLE
                                _context.findViewById<TextView>(R.id.textPassword2).visibility = View.VISIBLE
                                _context.findViewById<TextInputLayout>(R.id.txtPassword2).visibility = View.VISIBLE

                                Tools.showAlertDialog(_context, getString(R.string.info_ok_valid_code),R.drawable.ic_baseline_info)

                                return@post
                            }else{
                                Tools.showAlertDialog(_context,getString(R.string.info_error_not_valid_code),R.drawable.ic_baseline_error)
                            }
                        }else{
                            Tools.showAlertDialog(_context,_context.getString(R.string.info_error_reset_recovery_proccess),R.drawable.ic_baseline_error)
                            Thread.sleep(3000)
                            startActivity(Intent(_context, LoginActivity::class.java))
                        }
                    }
                }
            }
        }
    }


    /**
     * Manejador de eventos handleBtnRegister
     * Este manajador, responde al evento click sobre el botón registrarse, evalua todos los campos mostrando mensajes
     * de error si el campo evaluado del formulario está vacío o no cumple con las reglas del formatter. Y en el caso de
     * estar correcto, se muestra un alert dialog informando de los campos introducidos.
     */
    private fun handleBtnRegister() {
        // Step 1. Se obtienen los valores introducidos en los inputs del formulario
        val password = _context.findViewById<TextInputLayout>(R.id.txtPassword)
        val password2 = _context.findViewById<TextInputLayout>(R.id.txtPassword2)

        // Step 2. Se limpian los campos de error
        password.error = null
        password2.error = null

        // Step 3. Se vuelven a comprobar los inputs para la contraseña
        if(password.editText?.text.toString().isNullOrEmpty()){
            password.error = getString(R.string.form_error_password)
            return
        }

        if(password2.editText?.text.toString().isNullOrEmpty()){
            password2.error = getString(R.string.form_error_password_confirm)
            return
        }else{
            if(!password.editText?.text.toString().isNullOrEmpty() &&
                !password2.editText?.text.toString().equals(password.editText?.text.toString())){
                password2.error = getString(R.string.form_error_password_no_equals)
                return
            }
        }

        // Step 4. Instancia de la propiedad Usuario & registro en el servidor
        user = User(email.editText?.text.toString(),password.editText?.text.toString())
        register(password.editText?.text.toString())
    }


    /**
     * Manejador de eventos handleBtnLogin
     * Este manejado se encarga de realizar de forma asincrona la operación de LOGIN, una vez que el usuario se ha
     * registrado en DOGFEEDER
     */
    private fun handleBtnLogin(){
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            user.login(_context, false, true)
        }
    }


    /**
     * Método register
     * Este método se encarga de realizar la operación de registro del usuario en la DB.
     *@param password La nueva contraseña de usuario
     */
    private fun register( password : String){

        var statusCode = Session.ServerResponseCodes.ERROR.code
        val handler = Handler(Looper.getMainLooper())
        var msg = _context.getString(R.string.info_error_connection)
        var isConError = false
        var icon = R.drawable.ic_baseline_info
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(password, null)

                // Step 2. Obtenemos la respuesta del servidor
                statusCode = session.readResponseFromServer()
            }catch (se : SocketException){
                session.clientSocket.close()
                isConError = true
                icon = R.drawable.ic_baseline_error
            }finally {
                handler.post {
                    if(!isConError){
                        if(statusCode.equals(Session.ServerResponseCodes.OK.code)){
                            Tools.cleanSharedPreferences(sharedPreferences)
                            btnRegister.isEnabled = false
                            btnLogin.isEnabled = true
                            btnLogin.visibility = View.VISIBLE
                            msg = getString(R.string.info_register_ok)
                        }else{
                            msg =getString(R.string.info_register_error)
                        }
                    }
                    Tools.showAlertDialog(_context,msg,icon)
                }
            }
        }
    }
}