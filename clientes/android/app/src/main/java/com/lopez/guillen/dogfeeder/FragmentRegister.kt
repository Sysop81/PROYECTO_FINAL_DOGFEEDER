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
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.model.User
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

class FragmentRegister() : Fragment() {
    private var session = Session.getInstance()
    private lateinit var _context: Activity
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnLogin : Button
    private lateinit var btnRegister : Button
    private lateinit var email: TextInputLayout
    //private lateinit var password : TextInputLayout
    //private lateinit var password2 : TextInputLayout
    private lateinit var user: User

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
        // Step 0. Carga de preferencias
        sharedPreferences = view.context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        // Step 1. Se obtiene y se deshabilita el botón de login. Además, se le añade su manejador de eventos.
        btnLogin = view.findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener{
            handleBtnLogin()
        }
        btnLogin.isEnabled = false

        // Step 2. Se instancia el boton de registro y se establece su manejador de eventos
        btnRegister = view.findViewById<Button>(R.id.btnRegister)
        btnRegister.setOnClickListener{
            handleBtnRegister()
        }

        // Step 3. Se instancia el input para la entrada del correo electronico y un manejador de eventos para validar
        email = view.findViewById(R.id.txtRegisterEmail)
        email.editText?.doOnTextChanged { text, _, _, _ ->
            if (Tools.isValidEmail(text.toString())) {
                email.error = null
            } else {
                email.error = "Correo electrónico no válido"
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
        val password = _context.findViewById<TextInputLayout>(R.id.txtPassword)
        val password2 = _context.findViewById<TextInputLayout>(R.id.txtPassword2)
        // Step 2. Se limpian los campos de error
        email.error = null
        password.error = null
        password2.error = null


        // Step 3. Se comprueban los campos uno a uno en orden de lista. Si se cumple la condición del if, se realiza
        //         un retorno anticipado del método marcando el campo que ha lanzado el error.
        if(email.editText?.text.toString().isNullOrEmpty()){
            email.error = "El correco electrónico no puede estar vacio"
            return
        }


        if(password.editText?.text.toString().isNullOrEmpty()){
            password.error = "La contraseña no puede estar vacia"
            return
        }

        if(password2.editText?.text.toString().isNullOrEmpty()){
            password2.error = "La contraseña de confirmación no puede estar vacia"
            return
        }else{
            if(!password.editText?.text.toString().isNullOrEmpty() &&
                !password2.editText?.text.toString().equals(password.editText?.text.toString())){
                password2.error = "La contraseña de confirmación no coincide"
                return
            }
        }

        // Instancia de la propiedad Usuario & registro en el servidor
        user = User(email.editText?.text.toString(),password.editText?.text.toString())
        register(user.email,user.password)

    }

    private fun handleBtnLogin(){

        val user = hashMapOf(
            "email" to this.user.email,
            "password" to this.user.password
        )

        // TODO MOVE TO COMMOS CORRUTINE
        val that = this
        val handler = Handler(Looper.getMainLooper())
        GlobalScope.launch(Dispatchers.IO) {
            val stream: OutputStream =  that.session.clientSocket.getOutputStream()
            val streamOut = DataOutputStream(stream)
            streamOut.writeUTF("LOGIN")
            streamOut.writeUTF(user["email"] +"_"+ user["password"])

            val inStream: InputStream = that.session.clientSocket.getInputStream()
            val data = DataInputStream(inStream)
            val statusCode = data.readUTF();

            handler.post{
                if(statusCode.equals("1")){
                    Tools.setUserInSharedPreferences(sharedPreferences,user)
                    startMainActivity()
                }else{
                    Tools.showAlertDialog(_context,"Login incorrecto " + statusCode)
                }
            }
        }
    }


    private fun register(email : String, pass : String){
        val that = this
        val handler = Handler(Looper.getMainLooper())
        GlobalScope.launch(Dispatchers.IO) {
            val stream: OutputStream =  that.session.clientSocket.getOutputStream()
            val streamOut = DataOutputStream(stream)
            streamOut.writeUTF("REGISTER")
            streamOut.writeUTF(email +"_"+pass)

            val inStream: InputStream = that.session.clientSocket.getInputStream()
            val data = DataInputStream(inStream)
            val statusCode = data.readUTF();

            handler.post{
                if(statusCode.equals("1")){
                    Tools.cleanSharedPreferences(sharedPreferences)
                    btnRegister.isEnabled = false
                    btnLogin.isEnabled = true
                }else{
                    Tools.showAlertDialog(_context,"No se ha podido registrar al usuario")
                }
            }
        }
    }

    /**
     * Método startMainActivity
     * Simplemente, se encarga de lanzar el intent con el cambio de activity.
     */
    private fun startMainActivity(){
        startActivity(Intent(requireContext(),MainActivity::class.java))
    }
}