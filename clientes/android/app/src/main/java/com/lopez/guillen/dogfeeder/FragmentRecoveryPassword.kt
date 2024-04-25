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
import androidx.fragment.app.Fragment
import androidx.transition.Visibility
import com.google.android.material.textfield.TextInputLayout
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

class FragmentRecoveryPassword() : Fragment() {
    private var session = Session.getInstance()
    private lateinit var _context: Activity
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnRecoveryPass : Button
    private lateinit var btnValidateCode : Button
    private lateinit var btnChangePassword: Button

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

        btnRecoveryPass = view.findViewById<Button>(R.id.btnRecoveryPass)
        btnRecoveryPass.setOnClickListener{ handleBtnRecovery() }
        btnValidateCode = view.findViewById<Button>(R.id.btnVerifyCode)
        btnValidateCode.setOnClickListener{ handleBtnVerifyCode() }
        btnChangePassword = view.findViewById<Button>(R.id.btnChangePassword)
        btnChangePassword.setOnClickListener{ handleBtnChangePassword() }

    }

    private fun handleBtnChangePassword(){
        val password = _context.findViewById<TextInputLayout>(R.id.txtPassword)
        val password2 = _context.findViewById<TextInputLayout>(R.id.txtPassword2)

        // Step 2. Se limpian los campos de error
        password.error = null
        password2.error = null

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


        setRequestWhitNewPassWord(password.editText?.text.toString())


    }

    private fun setRequestWhitNewPassWord(password : String){
        // TODO MOVE TO COMMOS CORRUTINE
        val that = this
        val handler = Handler(Looper.getMainLooper())
        GlobalScope.launch(Dispatchers.IO) {
            val stream: OutputStream =  that.session.clientSocket.getOutputStream()
            val streamOut = DataOutputStream(stream)
            streamOut.writeUTF(password)

            val inStream: InputStream = that.session.clientSocket.getInputStream()
            val data = DataInputStream(inStream)
            val statusCode = data.readUTF();

            handler.post{
                if(statusCode.equals("1")){
                    that.btnChangePassword.visibility = View.INVISIBLE
                    Tools.showAlertDialog(_context, "Contraseña fuardada con éxito")
                    Thread.sleep(3000)
                    startLoginActivity()

                }else{
                    Tools.showAlertDialog(_context,"El código no es válido")
                }
            }
        }
    }

    private fun handleBtnVerifyCode(){
        val code = _context.findViewById<TextInputLayout>(R.id.txtVerifyCode)

        code.error = ""

        // Step 3. Comprobamos si alguno de los campos está vacio y de ser así lanzamos error y realizamos un retorno
        //         anticipado.
        if(code?.editText?.text.toString().isNullOrEmpty()){
            code?.error = "El código no puede estar vacío"
            return
        }

        setRequestWithCode(code.editText?.text.toString())
    }

    private fun handleBtnRecovery(){
        val email = _context.findViewById<TextInputLayout>(R.id.txtEmail)
        this.btnRecoveryPass.isEnabled = false
        sendRequestChangePassword(email.editText?.text.toString())
    }


    private fun setRequestWithCode(code: String){
        // TODO MOVE TO COMMONS CORRUTINE
        val that = this
        val handler = Handler(Looper.getMainLooper())
        GlobalScope.launch(Dispatchers.IO) {
            val stream: OutputStream =  that.session.clientSocket.getOutputStream()
            val streamOut = DataOutputStream(stream)
            streamOut.writeUTF(code)

            val inStream: InputStream = that.session.clientSocket.getInputStream()
            val data = DataInputStream(inStream)
            val statusCode = data.readUTF();

            handler.post{
                if(statusCode.equals("1")){
                    //Tools.setUserInSharedPreferences(sharedPreferences,user)
                    //startMainActivity()
                    //that.btnValidateCode.visibility = View.INVISIBLE
                    _context.findViewById<Button>(R.id.btnVerifyCode).visibility = View.INVISIBLE
                    _context.findViewById<TextView>(R.id.textVerifyCode).visibility = View.INVISIBLE
                    _context.findViewById<TextInputLayout>(R.id.txtVerifyCode).visibility = View.INVISIBLE

                    // Elementos visibles
                    _context.findViewById<TextView>(R.id.textPassword).visibility = View.VISIBLE
                    _context.findViewById<TextInputLayout>(R.id.txtPassword).visibility = View.VISIBLE
                    _context.findViewById<TextView>(R.id.textPassword2).visibility = View.VISIBLE
                    _context.findViewById<TextInputLayout>(R.id.txtPassword2).visibility = View.VISIBLE
                    _context.findViewById<Button>(R.id.btnChangePassword).visibility = View.VISIBLE
                    //that.btnValidateCode.visibility = View.VISIBLE
                }else{
                    Tools.showAlertDialog(_context,"El código no es válido")
                }
            }
        }
    }

    private fun sendRequestChangePassword(email : String){
        // TODO MOVE TO COMMOMS CORRUTINE
        val that = this
        val handler = Handler(Looper.getMainLooper())
        GlobalScope.launch(Dispatchers.IO) {
            val stream: OutputStream =  that.session.clientSocket.getOutputStream()
            val streamOut = DataOutputStream(stream)
            streamOut.writeUTF("RECOVERYPASS")
            streamOut.writeUTF(email)

            val inStream: InputStream = that.session.clientSocket.getInputStream()
            val data = DataInputStream(inStream)
            val statusCode = data.readUTF();

            handler.post{
                if(statusCode.equals("1")){
                    //Tools.setUserInSharedPreferences(sharedPreferences,user)
                    //startMainActivity()
                    that.btnRecoveryPass.visibility = View.INVISIBLE
                    _context.findViewById<TextView>(R.id.textEmail).visibility = View.INVISIBLE
                    _context.findViewById<TextInputLayout>(R.id.txtEmail).visibility = View.INVISIBLE

                    // Elementos visibles
                    _context.findViewById<TextView>(R.id.textVerifyCode).visibility = View.VISIBLE
                    _context.findViewById<TextInputLayout>(R.id.txtVerifyCode).visibility = View.VISIBLE
                    that.btnValidateCode.visibility = View.VISIBLE
                }else{
                    Tools.showAlertDialog(_context,"Email no registrado en DOGFEEDER")
                }
            }
        }
    }

    private fun startLoginActivity(){
        startActivity(Intent(requireContext(),LoginActivity::class.java))
    }
}