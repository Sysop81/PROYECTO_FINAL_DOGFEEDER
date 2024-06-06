package com.lopez.guillen.dogfeeder

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textfield.TextInputLayout
import com.lopez.guillen.dogfeeder.model.FragmentInteractionListener
import com.lopez.guillen.dogfeeder.model.RefreshInteraction
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.model.User
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.*

/**
 * Clase FragmentLogin
 * Fragmento encargado de la lógica de negocio para la vista de LOGIN
 */
class FragmentLogin : Fragment(), RefreshInteraction{
    // Propiedades de clase
    private var session = Session.getInstance();
    private lateinit var _context: Activity
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var inputUserEmail : TextInputLayout
    private lateinit var inputUserPassword: TextInputLayout
    private lateinit var btnLogin : Button
    private lateinit var btnRegister : Button
    private lateinit var btnRecovery : Button
    private lateinit var swipeRefresh : SwipeRefreshLayout

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
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override  fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Step 1. Instanciado de controles de la vista y demas objetos
        sharedPreferences = view.context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        swipeRefresh = view.findViewById(R.id.swipeLoginFragment)
        inputUserEmail = view.findViewById(R.id.txtUser)
        inputUserPassword = view.findViewById(R.id.txtPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnRegister = view.findViewById(R.id.btnRegister)
        btnRecovery = view.findViewById(R.id.btnRecoveryPassword)


        // Step 2. Definición de manejadores de eventos
        swipeRefresh.setOnRefreshListener { handleRefreshLayout() }
        btnLogin.setOnClickListener{ handleLogin() }
        btnRegister.setOnClickListener{ handleRegister() }
        btnRecovery.setOnClickListener{handleBtnRecoveryPass()}
        inputUserEmail.editText?.doOnTextChanged { text, _, _, _ ->
            if (Tools.isValidEmail(text.toString())) {
                inputUserEmail.error = null
            } else {
                inputUserEmail.error = getString(R.string.form_error_email)
            }
        }

        // Step 3. Llamada por defecto al método encargado de seguir el flijo de carga del frangment.
        handleRefreshLayout()
    }


    /**
     * Manejador de eventos handleRefreshLayout
     * Método encargado del refresco de la vista [función de desplazamiento o deslizamiento hacia abjo de la vista],
     * en terminos generales para la aplicación, este método se encarga de reestablecer la sesión o conectividad con
     * DOG-FEEDER en caso de haberla perdido.
     */
    override fun handleRefreshLayout() {
        val handler = Handler(Looper.getMainLooper())
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            var isConnected = false
            try {
                isConnected = session.checkServerConnection(_context)
            }catch(e : Exception){

            }finally {
                handler.post{
                    var defaultControlsState = false
                    if(isConnected){
                        if(Tools.checkSharedPreferences(sharedPreferences)){
                            val user = Tools.getUserInSharedPreferences(sharedPreferences)
                            if(user != null)
                                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
                                    user.login(_context,true,true)
                                }
                        }
                        defaultControlsState = true
                    }else{
                        Tools.showAlertDialog(_context,_context.getString(R.string.info_error_connection),R.drawable.ic_baseline_error)
                    }
                    setStatusOfControls(defaultControlsState)
                    swipeRefresh.isRefreshing = false
                }
            }
        }
    }


    /**
     * Método setStatusOfControls
     * Método encargado de activar / desactivar los distintos controles del formulario en función de la conectividad de
     * la aplicación cliente y el servicio TCP
     * @param _state Estado activado / desactivado para los controles en función del estado de la conectividad con el
     * servicio TCP
     */
    private fun setStatusOfControls(_state : Boolean){
        btnLogin.isEnabled = _state
        btnRegister.isEnabled = _state
        btnRecovery.isEnabled = _state
        inputUserEmail.isEnabled = _state
        inputUserPassword.isEnabled = _state
    }


    /**
     * Método handleLogin
     * Manejador de eventos para el botón de LOGIN. Evalua las entradas proporcionadas por el usuario marcando error en
     * caso de no evitar los condicionales y en caso contrario realizando la llamada al método encargado de realizar el
     * login.
     */
    private fun handleLogin() {
        // Step 1. Instanciamos los controles TextInputLayout del formulario
        val txtUser = view?.findViewById<TextInputLayout>(R.id.txtUser)
        val txtPass = view?.findViewById<TextInputLayout>(R.id.txtPassword)

        // Step 2. Limpiamos los campos de error
        txtUser?.error = ""
        txtPass?.error = ""

        // Step 3. Comprobamos si alguno de los campos está vacio y de ser así lanzamos error y realizamos un retorno
        //         anticipado.
        if(txtUser?.editText?.text.toString().isNullOrEmpty()){
            txtUser?.error = getString(R.string.form_error_user)
            return
        }

        if(txtPass?.editText?.text.toString().isNullOrEmpty()){
            txtPass?.error = getString(R.string.form_error_password)
            return
        }

        // Step 4. Recuperamos los valores introducidos por el usuario
        val user = txtUser?.editText?.text.toString()
        val pass = txtPass?.editText?.text.toString()

        // Step 5. Realizamos la llamada al metodo encargado de realizar el LOGIN en la aplicación.
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            User(user, pass).login(_context, false, true)
        }
    }


    /**
     * Manejador de eventos handleBtnRegister
     * Se encarga de manejar el evento click sobre el botón de registro. Se lanza una nueva activity para mostrar el
     * formulario de registro para un nuevo usuario de la aplicación
     */
    private fun handleRegister() {
        // Step 1. se declara la variable listener encargada de acceder al metodo implementado en el LoginActivity al
        //         implmentar la interfaz FragmentInteractionListener
        var listener: FragmentInteractionListener? = null

        // Step 2. Preguntamos si nuestra activity es del tipo (Interface) FragmentInteractionListener
        if (_context is FragmentInteractionListener) {
            // De esta forma casteamos el Activity a FragmentInteractionListener
            listener = _context as FragmentInteractionListener
            // Finalmente podemos acceder al metodo implmentado en el activity para realizar el cambio de fragment en
            // nuestro contenedor framelayout situado en la vista del activity login.
            listener.onHandleRegisterUser()
        }
    }


    /**
     * Manejador de eventos handleBtnRecoveryPass
     * Se encarga de manejar el evento click sobre el botón de recuperacion de contraseña. Se lanza un metodoto que se
     * encarga de manejar la acción en el activity login para cambiar en el frame layout al fragment que contiene el
     * cambio de contraseña.
     */
    private fun handleBtnRecoveryPass(){
        // Step 1. se declara la variable listener encargada de acceder al metodo implementado en el LoginActivity al
        //         implmentar la interfaz FragmentInteractionListener
        var listener: FragmentInteractionListener? = null
        // Step 2. Preguntamos si nuestra activity es del tipo (Interface) FragmentInteractionListener
        if (_context is FragmentInteractionListener) {
            // De esta forma casteamos el Activity a FragmentInteractionListener
            listener = _context as FragmentInteractionListener
            // Finalmente podemos acceder al metodo implmentado en el activity para realizar el cambio de fragment en
            // nuestro contenedor framelayout situado en la vista del activity login.
            listener.onHandleRecoveryPassword()
        }
    }
}