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
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketException

/**
 * Clase FragmentRecoveryPassword
 * Clase encargada de manejar la lógica de negocio para la vista encargada de recuperar la contraseña olvidada de un
 * usuario previamente registrado en la aplicación.
 */
class FragmentRecoveryPassword() : Fragment(), RefreshInteraction {
    // Declaración de propiedades de clase
    private var session = Session.getInstance()
    private lateinit var _context: Activity
    private lateinit var swipeRefresh : SwipeRefreshLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnRecoveryPass : Button
    private lateinit var btnValidateCode : Button
    private lateinit var btnChangePassword: Button
    private lateinit var email : TextInputLayout
    private lateinit var progressBar: ProgressBar

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
        return inflater.inflate(R.layout.fragment_recovery_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Step 0. Carga de preferencias
        sharedPreferences = view.context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        // Step 1. Instacias de controles del formulario
        btnRecoveryPass = view.findViewById(R.id.btnRecoveryPass)
        btnRecoveryPass.setOnClickListener{ handleBtnRecovery() }
        btnValidateCode = view.findViewById(R.id.btnVerifyCode)
        btnValidateCode.setOnClickListener{ handleBtnVerifyCode() }
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnChangePassword.setOnClickListener{ handleBtnChangePassword() }

        // Step 2. Instancia y asiganción del manejador de eventos para el refresco de la vista
        swipeRefresh = view.findViewById(R.id.swipeRecoveryFragment)
        swipeRefresh.setOnRefreshListener { handleRefreshLayout() }

        // Step 3. Se instancia el input para la entrada del correo electronico y un manejador de eventos para validar
        email = view.findViewById(R.id.txtEmail)
        email.editText?.doOnTextChanged { text, _, _, _ ->
            if (Tools.isValidEmail(text.toString())) {
                email.error = null
            } else {
                email.error = getString(R.string.form_error_email)
            }
        }

        // Step 4. Instancia y ocultación de la progressbar
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE

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
     * Manejador de eventos handleBtnChangePassword
     * Obtiene los valores introducidos en los campos del formulario y maneja sus errores, con la finalidad de lanzar el
     * proceso de creación de una nueva contraseña para el usuario.
     */
    private fun handleBtnChangePassword(){
        val password = _context.findViewById<TextInputLayout>(R.id.txtPassword)
        val password2 = _context.findViewById<TextInputLayout>(R.id.txtPassword2)

        // Step 2. Se limpian los campos de error y se verifican nuevamente los inputs
        password.error = null
        password2.error = null

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

        // Step 3. Se envía el nuevo password al servidor TCP para que este lo almacene en la BD
        setRequestWithNewPassWord(password.editText?.text.toString())
    }


    /**
     * Método setRequestWhitNewPassWord
     * Este método se encarga de realizar una operación asincrona para el envío del nuevo password de usuario
     * @param password Nueva contraseña para el usuario
     */
    private fun setRequestWithNewPassWord(password : String){
        progressBar.visibility = View.VISIBLE
        var statusCode = Session.ServerResponseCodes.ERROR.code
        val handler = Handler(Looper.getMainLooper())
        var msg = _context.getString(R.string.info_error_connection)
        var isConError = false
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
            // Step 1.  Realizamos el envío de datos al servidor
            session.sendRequestToServer(password,null)

            // Step 2. Obtenemos la respuesta del servidor
            statusCode = session.readResponseFromServer()
            }catch (se : SocketException){
                session.clientSocket.close()
                isConError = true
            }finally {
                handler.post {
                    progressBar.visibility = View.GONE
                    if(!isConError) {
                        if (statusCode.equals(Session.ServerResponseCodes.OK.code)) {
                            btnChangePassword.visibility = View.INVISIBLE
                            Tools.showAlertDialog(_context, getString(R.string.info_change_pass_ok),R.drawable.ic_baseline_info)
                            Thread.sleep(3000)
                            startActivity(Intent(requireContext(), LoginActivity::class.java))
                            return@post
                        } else {
                            Tools.showAlertDialog(_context, getString(R.string.info_change_pass_error),R.drawable.ic_baseline_error)
                        }
                    }
                    Tools.showAlertDialog(_context,msg,R.drawable.ic_baseline_error)
                }
            }
        }
    }


    /**
     * Manejador de eventos handleBtnVerifyCode
     * Este manejador de eventos, se encarga de checkear que el usuario introduce un código
     */
    private fun handleBtnVerifyCode(){
        // Step 1. Se obtiene el código introducido por el usuario y se resetea el campo de error del formulario
        val code = _context.findViewById<TextInputLayout>(R.id.txtVerifyCode)
        code.error = ""

        // Step 2. Comprobamos si alguno de los campos está vacio y de ser así lanzamos error y realizamos un retorno
        //         anticipado.
        if(code?.editText?.text.toString().isNullOrEmpty()){
            code?.error = getString(R.string.form_error_not_empty_code)
            return
        }
        // Step 3. Se realiza el envío del código que llega al correo electronico, para que este sea comprobado por el servidor
        setRequestWithCode(code.editText?.text.toString())
    }


    /**
     * Manejador de eventos handleBtnRecovery
     * Este manejador se encarga del boton para el envío del código al correo que el usuario ha introducido para su
     * recuperación
     */
    private fun handleBtnRecovery(){
        // Step 1. Se limpian los campos de error
        email.error = null

        // Step 2. Se comprueba nuevamente
        if(email.editText?.text.toString().isNullOrEmpty()){
            email.error = getString(R.string.form_error_user)
            return
        }

        // Step 3. Se desahabilita el botón para la recuperación de contraseña y se envía la petición al servidor
        this.btnRecoveryPass.isEnabled = false
        sendRequestChangePassword(email.editText?.text.toString())
    }


    /**
     * Método setRequestWithCode
     * Este método se encarga de realizar el envío del código previamente enviado al correo del usuario para ver si ambos
     * coinciden.
     * @param code Cadena que represnta el código de comprobación enviado al cliente
     */
    private fun setRequestWithCode(code: String){
        progressBar.visibility = View.VISIBLE
        var isConError = false
        var statusCode = Session.ServerResponseCodes.ERROR.code
        val handler = Handler(Looper.getMainLooper())
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
                            _context.findViewById<Button>(R.id.btnVerifyCode).visibility = View.INVISIBLE
                            _context.findViewById<TextView>(R.id.textVerifyCode).isEnabled = false
                            _context.findViewById<TextInputLayout>(R.id.txtVerifyCode).isEnabled = false

                            // Elementos visibles
                            _context.findViewById<TextView>(R.id.textPassword).visibility = View.VISIBLE
                            _context.findViewById<TextInputLayout>(R.id.txtPassword).visibility = View.VISIBLE
                            _context.findViewById<TextView>(R.id.textPassword2).visibility = View.VISIBLE
                            _context.findViewById<TextInputLayout>(R.id.txtPassword2).visibility = View.VISIBLE
                            _context.findViewById<Button>(R.id.btnChangePassword).visibility = View.VISIBLE

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


    /**
     * Método sendRequestChangePassword
     * Este método se encarga de inicializar el proceso para establecer un nuevo password de usuario.
     * @param email Correo electrónico del usuario que pretende modificar su contraseña
     */
    private fun sendRequestChangePassword(email : String){
        progressBar.visibility = View.VISIBLE
        var isConError = false
        var msg = _context.getString(R.string.info_error_connection)
        var statusCode = Session.ServerResponseCodes.ERROR.code
        val handler = Handler(Looper.getMainLooper())
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try{
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(email,Session.ServerStates.RECOVERYPASS)

                // Step 2. Obtenemos la respuesta del servidor
                statusCode = session.readResponseFromServer()
            }catch (se : SocketException){
                session.clientSocket.close()
                isConError = true
            }catch(e: UninitializedPropertyAccessException){
                isConError = true
            }finally {
                handler.post {
                    progressBar.visibility = View.GONE
                    if(!isConError) {
                        if (statusCode.equals(Session.ServerResponseCodes.OK.code)) {
                            btnRecoveryPass.visibility = View.INVISIBLE
                            _context.findViewById<TextView>(R.id.textEmail).visibility = View.INVISIBLE
                            _context.findViewById<TextInputLayout>(R.id.txtEmail).visibility = View.INVISIBLE

                            // Elementos visibles
                            _context.findViewById<TextView>(R.id.textVerifyCode).visibility = View.VISIBLE
                            _context.findViewById<TextInputLayout>(R.id.txtVerifyCode).visibility = View.VISIBLE
                            btnValidateCode.visibility = View.VISIBLE
                            return@post
                        } else {
                            msg = getString(R.string.info_error_email_not_register)
                            btnRecoveryPass.isEnabled = true
                        }
                    }else{
                        btnRecoveryPass.isEnabled = true
                    }

                    Tools.showAlertDialog(_context,msg,R.drawable.ic_baseline_error)
                }
            }
        }
    }
}