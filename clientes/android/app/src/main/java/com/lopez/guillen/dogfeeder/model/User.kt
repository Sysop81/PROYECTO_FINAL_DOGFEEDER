package com.lopez.guillen.dogfeeder.model

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.lopez.guillen.dogfeeder.R
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.SocketException

/**
 * Clase User
 * Esta data class se encarga de la gestion de instancias de tipo usuario
 * email -> Correo electrónico del usuario
 * password -> Contraseña del usuario
 * validEmail -> Determina si el correo es válido para notificaciones o no
 */
data class User(val email:String, val password:String) {

    private var validEmail: Boolean? = null

    /**
     * Constructor secundario
     */
    constructor(email: String, password: String, validEmail: Boolean) : this(email, password) {
        this.validEmail = validEmail
    }

    /**
     * Método suspendido login
     * Método encargado de manejar el login del usuario tanto para la acción inicial como para la recuperación de la
     * sesión desde cualquier fragment
     * @param _context Contexto del activity donde se realiza la llamada
     * @param isInSharedPreferences Determina si el usuario se encuentra alamcenado o no en las sharedPreferences
     * @param isLaunchMainActivity Determina si debe lanzar un alertdialog o el mainActivity
     */
    suspend fun login(_context: Activity,isInSharedPreferences: Boolean,isLaunchMainActivity: Boolean){
        withContext(Dispatchers.IO) {
            val handler = Handler(Looper.getMainLooper())
            val session = Session.getInstance()
            var statusCode = Session.ServerResponseCodes.ERROR.code
            var msg = _context.getString(R.string.info_login_error)
            try {
                // Step 0. Creamos el JSON que enviaremos al servidor
                // Generamos un map con las propiedades
                val user = hashMapOf(
                    "email" to email,
                    "password" to password
                )
                // Obtenemos la cadena JSON a enviar al servidor
                val usuarioJson = (JSONObject(user as Map<*, *>?)).toString()

                // Step 1. Realizamos el envío de datos al servidor
                session.sendRequestToServer(usuarioJson, Session.ServerStates.LOGIN)

                // Step 2. Obtenemos la respuesta del servidor
                statusCode = session.readResponseFromServer()
            }catch (se : SocketException){
                session.clientSocket.close()
                msg =  _context.getString(R.string.info_error_connection)
            } finally {

                handler.post {
                    if (statusCode.equals(Session.ServerResponseCodes.OK.code)) {
                        if(isLaunchMainActivity){
                            Tools.setUserAndLauncherApp(email, password, isInSharedPreferences,_context)
                        }else{
                            Tools.showAlertDialog(_context,_context.getString(R.string.info_ok_connection),R.drawable.ic_baseline_info)
                        }

                    } else {
                        Tools.showAlertDialog(_context, msg,R.drawable.ic_baseline_error)
                    }
                }
            }
        }
    }
}