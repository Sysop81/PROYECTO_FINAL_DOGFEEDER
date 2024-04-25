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
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.lopez.guillen.dogfeeder.LoginActivity
import com.lopez.guillen.dogfeeder.R
import com.lopez.guillen.dogfeeder.model.FragmentInteractionListener
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.*
import java.net.Socket

class FragmentLogin : Fragment(){

    //private lateinit var clientSocket: Socket
    private var session = Session.getInstance();
    private lateinit var _context: Activity
    private lateinit var sharedPreferences: SharedPreferences

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
        sharedPreferences = view.context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        GlobalScope.launch(Dispatchers.IO) {
            // establecemos el socket con el servidor TCP //  TODO -> Set in sharedPreferences - or set a fun to search search a server IP
            //val serverIp = "10.0.2.2"        // localhost
            //val serverIp = "192.168.18.240" // Local 1
            val serverIp = "192.168.1.240"     // Local 2
            val serverPort = 2000
            session.clientSocket = Socket(serverIp, serverPort)

            if(Tools.loadSharedPreferences(sharedPreferences)){

                val user = Tools.getUserInSharedPreferences(sharedPreferences)
                if (user != null) {
                    login(user.email,user.password,true)
                };

            }
        }

        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val btnRecovery = view.findViewById<Button>(R.id.btnRecoveryPassword)
        btnLogin.setOnClickListener{ handleLogin() }
        btnRegister.setOnClickListener{ handleRegister() }
        btnRecovery.setOnClickListener{handleBtnRecoveryPass()}
    }


    fun handleLogin() {
        // Step 1. Instanciamos los controles TextInputLayout del formulario
        val txtUser = view?.findViewById<TextInputLayout>(R.id.txtUser)
        val txtPass = view?.findViewById<TextInputLayout>(R.id.txtPassword)

        // Step 2. Limpiamos los campos de error
        txtUser?.error = ""
        txtPass?.error = ""

        // Step 3. Comprobamos si alguno de los campos está vacio y de ser así lanzamos error y realizamos un retorno
        //         anticipado.
        if(txtUser?.editText?.text.toString().isNullOrEmpty()){
            txtUser?.error = "El nombre de usuario no puede estar vacío"
            return
        }

        if(txtPass?.editText?.text.toString().isNullOrEmpty()){
            txtPass?.error = "El nombre de usuario no puede estar vacío"
            return
        }

        // Step 4. Recuperamos los valores introducidos por el usuario y preguntamos en la base de datos. Si las credenciales
        //         se validan correctamente, se lanza el ReciclerViewActivity para mostrar el listado de entrenamientos
        val user = txtUser?.editText?.text.toString()
        val pass = txtPass?.editText?.text.toString()

        login(user,pass,false);

    }


    private fun login(email : String, pass : String, isInSharedPreferences: Boolean){
        val that = this
        val handler = Handler(Looper.getMainLooper())
        GlobalScope.launch(Dispatchers.IO) {
            val stream: OutputStream =  that.session.clientSocket.getOutputStream()
            val streamOut = DataOutputStream(stream)
            streamOut.writeUTF("LOGIN")
            streamOut.writeUTF(email +"_"+pass)

            val inStream: InputStream = that.session.clientSocket.getInputStream() 
            val data = DataInputStream(inStream)
            val statusCode = data.readUTF();

            handler.post{
                if(statusCode.equals("1")){
                    setUserAndLauncherApp(email,pass,isInSharedPreferences)
                }else{
                    Tools.showAlertDialog(_context,"Login incorrecto " + statusCode)
                }
            }
        }
    }

    private fun setUserAndLauncherApp(email : String, pass : String, isInSharedPreferences: Boolean){
        if(!isInSharedPreferences){
            val user = hashMapOf(
                "email" to email,
                "password" to pass
            )
            Tools.setUserInSharedPreferences(sharedPreferences,user)
        }

        startMainActivity()
    }


    /**
     * Método startMainActivity
     * Simplemente, se encarga de lanzar el intent con el cambio de activity.
     */
    private fun startMainActivity(){
        startActivity(Intent(requireContext(),MainActivity::class.java))
    }


    /**
     * Manejador de eventos handleBtnRegister
     * Se encarga de manejar el evento click sobre el botón de registro. Se lanza una nueva activity para mostrar el
     * formulario de registro para un nuevo usuario de la aplicación
     */
    fun handleRegister() {
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
    fun handleBtnRecoveryPass(){
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