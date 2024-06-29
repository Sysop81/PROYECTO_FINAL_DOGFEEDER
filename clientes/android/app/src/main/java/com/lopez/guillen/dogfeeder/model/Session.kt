package com.lopez.guillen.dogfeeder.model

import android.app.Activity
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

/**
 * Clase Session
 * Clase que representa el Singletone para tener uns instancia global, encargada de manejar el socket cliente, códigos
 * de estado y respuesta ..
 */
class Session private constructor(){
    // Propiedades de clase
    lateinit var clientSocket: Socket
    private var pet : Pet? = null

    // Códigos de estado [END POINTS de la máquina de estados]
    enum class ServerStates {
        CLOSE,LOGIN,REGISTER,RECOVERYPASS,FOOD,AUDITS,LASTAUDIT,GRAPHAUDITS,PETDATA,POSTPET,SETTINGSDATA,LED,REPORT,
        MAXFOODRATION,HOPPERLOW,FEEDEREMPTY,RESET
    }

    // Códigos de respuesta a las peticiones
    enum class ServerResponseCodes(val code : String) {
        ERROR("0"),
        OK("1"),
        EMPTY_DATA("2"),
        WAIT("3"),
        HOPPER_DANGER("4"),
        USER_WORKING("6")
    }

    // Códigos de estado para la medición de la tolva de alimento
    enum class HopperStates{
        DANGER,WARNING,OK
    }


    /**
     * Método getInstance
     * Método estático que devuelve el singletone
     */
    companion object {
        private var instance: Session? = null

        @Synchronized
        fun getInstance(): Session {
            if (instance == null) {
                instance = Session()
            }
            return instance!!
        }
    }


    /**
     * Constructor
     */
    constructor(clientSocket: Socket) : this() {
        this.clientSocket = clientSocket
    }


    /**
     * Setter para la propiedad pet
     * @param pet Instancia de la mascota
     */
    fun setSessionPet(pet : Pet?){
        this.pet = pet
    }


    /**
     * Getter para la propiedad pet
     * @return pet Instancia de pet
     */
    fun getSessionPet() : Pet?{
        return this.pet
    }


    /**
     * Método close
     * Cierra el sockect establecido con el servidor TCP
     */
    fun close(){
        this.clientSocket.close()
        instance = null;
    }


    /**
     * Método checkServerConnection [Suspendido]
     * Método encargado de checkear la conectividad con el socket servidor
     * @param _context Contexto que representa el activity
     * @return Boolean Resultado de la operación de check
     */
     suspend fun checkServerConnection(_context : Activity): Boolean {
        return withContext(Dispatchers.IO) {
            val SERVER_PORT = 2000
            try {
                val SERVER_IP = Tools.getIpServer(_context)
                clientSocket = Socket(SERVER_IP, SERVER_PORT)
                true
            } catch (e: Exception) {
                false
            }
        }
    }


    /**
     * Método sendRequestToServer
     * Método encargado de solicitar una request al servicio TCP DOG-FEEDER
     * @param msg Cadena a enviar al servidor TCP
     * @param op ENDPOINT de la máquina de estados del servidor TCP
     */
    fun sendRequestToServer(msg : String?, op : ServerStates?){
        val stream: OutputStream =  clientSocket.getOutputStream()
        val streamOut = DataOutputStream(stream)
        if(op != null) streamOut.writeUTF(op.toString())
        if(msg != null) streamOut.writeUTF(msg)
    }


    /**
     * Método readResponseFromServer
     * Método encargado de leer la respuesta enviada por el servidor TCP
     * @return String cadena que representa la respuesta del servidor a una Request
     */
    fun readResponseFromServer() : String{
        val inStream: InputStream = clientSocket.getInputStream()
        val data = DataInputStream(inStream)
        val response = data.readUTF();
        return response
    }

    /**
     * Método manageRequest TODO CHECKING
     * Método encargado de devolver de forma directa la respuesta del servidor.
     * @param msg Cadena a enviar al servidor TCP
     * @param op ENDPOINT de la máquina de estados del servidor TCP
     * @return String cadena que representa la respuesta del servidor a una Request
     */
    fun manageRequest(msg : String?, op : ServerStates?) : String{
        // Step 1. Realizamos el envío de datos al servidor
        sendRequestToServer(msg, op)

        // Step 3. Obtenemos la respuesta del servidor
        return readResponseFromServer()
    }
}