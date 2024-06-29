package com.lopez.guillen.dogfeeder.utils

import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Build
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lopez.guillen.dogfeeder.MainActivity
import com.lopez.guillen.dogfeeder.model.AuditFoodItem
import com.lopez.guillen.dogfeeder.model.User
import org.json.JSONObject
import java.time.LocalDateTime

/**
 * Clase Tools 'Estática'
 * Métodos estáticos de apoyo o helpers para diversas tareas de gestión del cliente
 */
class Tools {
    companion object{

        /*************************************** ALERTAS **************************************************************/

        /**
         * Método showAlertDialog
         * Este método se encarga de mostrar información al usuario mediante un AlertDialog. Es un método generico para mostrar
         * cualquier msg de texto que entre como parametro. Solo contiene el boton de cierre y el manejador para dicho botón
         * @param context Contexto del activity
         * @param msg Cadena de caracteres que contiene la información a mostrar
         * @param icon icono a mostrar junto al mensaje
         */
        fun showAlertDialog(context: Context, msg: String, icon: Int? = null){
            // Step 1. Se construye el objeto cosntructor
            val alert = AlertDialog.Builder(context)
            alert.setTitle(com.lopez.guillen.dogfeeder.R.string.dialog_info_title)


            // Step 2. Se establece el botón de cierre y la función lambda encargada de manejar la acción de cierre mediante
            //         el método dismiss.
            alert.setMessage(msg).setPositiveButton(com.lopez.guillen.dogfeeder.R.string.dialog_close){alertDialog, _ ->
                alertDialog.dismiss()
            }

            // Step 3. Se establece el ícono si se ha proporcionado uno
            icon?.let {
                alert.setIcon(it)
            }

            // Step 4. Se crea el objeto mediante el metodo create del constructor y se muestra al usuario mediante el metodo
            //         show.
            val aDialog = alert.create()
            aDialog.show()
        }

        /**
         * Método showQuestionDialog. TODO PROVISIONAL
         * Este método se encarga de mostrar información al usuario con la finalidad de que este tome una decisión sobre la
         * acción que pretende realizar.
         * @param context Contexto del activity
         * @param msg Cadena de caracteres que contiene la información a mostrar
         * @param icon icono a mostrar junto al mensaje
         * @param onAccept lambda encargada de ejecutarse el la llamada ante la respuesta positiva.
         */
        fun showQuestionDialog(context: Context, msg: String, icon: Int? = null,onAccept: () -> Unit){
            // Step 1. Se construye el objeto cosntructor
            val alert = AlertDialog.Builder(context)
            alert.setTitle(com.lopez.guillen.dogfeeder.R.string.dialog_info_title)

            // Step 2. Se establece el botón de cierre y la función lambda encargada de manejar la acción de cierre mediante
            //         el método dismiss.
            alert.setMessage(msg)
                .setNegativeButton(com.lopez.guillen.dogfeeder.R.string.dialog_cancel){alertDialog, _ ->
                    alertDialog.dismiss()
                }
                .setPositiveButton(com.lopez.guillen.dogfeeder.R.string.dialog_ok){alertDialog, _ ->
                    onAccept()
                    alertDialog.dismiss()
                }

            // Step 3. Se establece el ícono si se ha proporcionado uno
            icon?.let {
                alert.setIcon(it)
            }

            // Step 4. Se crea el objeto mediante el metodo create del constructor y se muestra al usuario mediante el metodo
            //         show.
            val aDialog = alert.create()
            aDialog.show()

            // Step 5. Modificamos el botón de cancelación
            aDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.let { button ->
                button.setTextColor(ContextCompat.getColor(context, com.lopez.guillen.dogfeeder.R.color.turquoise))
                button.typeface = Typeface.DEFAULT_BOLD
            }

        }


        /*************************************** VALIDADORES **********************************************************/

        /**
         * Método isValidEmail
         * Este método se encarga de validar el valor recogido del formulario mediante una expresión regular establecida en
         * el patron. Mediante el método matcher del patron construido se evalua la cadena introducida por el usuario. Se retornará
         * verdadero si es correcta en base al patron o falso en caso de que no sea válida.
         * @param email Correo electronico del usuario
         * @return boolean Resultado de la operación de validación
         */
        fun isValidEmail(email: String): Boolean {
            val pattern = Patterns.EMAIL_ADDRESS
            return pattern.matcher(email).matches()
        }


        /**
         * Método isValidString
         * Método encargado de validar una cadena de texto que no contenga caracteres especiales
         * @param input Representa la cadena de texto a validar
         * @return boolean Resultado de la operación de validación
         */
        fun isValidString(input : String) : Boolean{
            val regex = Regex("^[a-zA-ZñÑáéíóúÁÉÍÓÚüÜ\\s]+$")
            return regex.matches(input)
        }

        /******************************** SHAREDPRESERENCES, JSON ****************************************************/

        /**
         * Método loadSharedPreferences
         * Se encarga de determinar si nuestras shared preferences cxuentan con un usuario logueado.
         * @param sharedPrefences Preferencias compartidas del sistema
         * @return true o false
         */
        fun loadSharedPreferences(sharedPrefences: SharedPreferences) : Boolean{
            return sharedPrefences.contains("user")
        }


        /**
         * Método setUserInSharedPreferences [Sobrecargado]
         * Se encaga de guardar el usuario en las sharedPreferences
         * @param sharedPrefences Preferencias compartidas del sistema
         * @param userMap map key-value para la representación del usuario
         */
        fun setUserInSharedPreferences(sharedPrefences: SharedPreferences, userMap: HashMap<String, String>){
            val editor = sharedPrefences.edit()
            val usuarioJson = (JSONObject(userMap as Map<*, *>?)).toString()
            editor.putString("user", usuarioJson)
            editor.apply()
        }


        /**
         * Método getUserInSharedPreferences
         * Se encarga de devolver la instancia de la data clas "User" seteada en las sharedPreferences
         * @param sharedPrefences Preferencias compartidas del sistema
         * @return User Instancia del usuario almacenado en las preferencias del sistema
         */
        fun getUserInSharedPreferences(sharedPrefences: SharedPreferences) : User? {
            val usuarioJson = sharedPrefences.getString("user", null)
            return if (usuarioJson != null) jsonToUser(usuarioJson) else null
        }


        /**
         * Método cheanSharedPreferences
         * Se encarga de borrar el contenido de las sharedPreferences. Utilizado cuando el usuario cierra sesion.
         * @param sharedPrefences Preferencias compartidas del sistema
         */
        fun cleanSharedPreferences(sharedPrefences: SharedPreferences) {
            val editor = sharedPrefences.edit()
            editor.clear()
            editor.apply()
        }


        /**
         * Método checkSharedPreferences
         * Método encargado de verificar si existe un usuario alamcenado en las preferencias del sistema
         * @param sharedPreferences Preferencias compartidas del sistema
         * @return boolean Resultado de la verificación realizada
         */
        fun checkSharedPreferences(sharedPreferences : SharedPreferences) : Boolean{
            if(loadSharedPreferences(sharedPreferences)){
                val user = getUserInSharedPreferences(sharedPreferences)
                return user != null
            }

            return false
        }


        /**
         * Método setUserAndLauncherApp
         * Método encargado de setear al usuario en las preferencias compartidas, siempre que este no exista, posteriormente
         * lanza el activity MAIN.
         * @param email Correo del usuario
         * @param pass Contraseña del usuario
         * @param isInSharedPreferences Determina si el usuario esta en las sharedPreferences
         * @param _context Contexto de la actividad
         */
        fun setUserAndLauncherApp(email : String, pass : String, isInSharedPreferences: Boolean,_context: Activity){
            if(!isInSharedPreferences){
                val user = hashMapOf(
                    "email" to email,
                    "password" to pass
                )

                setUserInSharedPreferences(_context.getSharedPreferences("preferences", Context.MODE_PRIVATE),user)
            }

            _context.startActivity(Intent(_context, MainActivity::class.java))
        }


        /**
         * Método jsonToUser
         * Este método se encarga de convertir un JSON en una instancia de la data class "User" encargada de modelar un usuario
         * de la aplicacion.
         */
        private fun jsonToUser(json: String): User {
            val jsonObject = JSONObject(json)
            return User(jsonObject.optString("email"),
                        jsonObject.optString("password"))
        }


        /*************************************** FORMULARIOS **********************************************************/


        /**
         * Método formatDate
         * Método encargado de realizar la conversión de la fecha con diferentes delimitadores
         * @param date Fecha en formato cadena de caracteres
         * @param delimiter Delimitador para el retorno de la fecha
         * @return String Cadena de texto con la fecha formateada
         */
        fun formatDate(date : String, delimiter : String) : String{
            var aDate = date.split("-");
            if (aDate.isEmpty() || aDate.size < 3) return date
            return aDate[2] + delimiter + aDate[1] + delimiter + aDate[0]
        }


        /**************************************** Elementos gráficos **************************************************/

        /**
         * Método generateWebViewLastSupplyFoodContent
         * Método encargado de realizar la carga del control webView destinado a mostrar la información del último
         * registro de auditoría de alimento registrada.
         * @param aFood Instancia de auditoria para el registro de suministro de alimento
         * @param title Título para mostrar en el webView
         * @return String Cadena de texto con el contenido HTML
         */
        fun generateWebViewLastSupplyFoodContent(aFood : AuditFoodItem, title : String) : String{
            return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title></title>
                </head>
                    <body style=" background : rgb(254, 247, 255);">
                        <p style="padding: 2px 2px 1px 10px;
                                  border-radius: 5px 5px 0px 0px;
                                  margin-bottom: 0;
                                  background : rgb(117, 103, 87);
                                  color : rgb(254, 247, 255);
                                  border: 2px solid rgb(117, 103, 87);">
                            <b>${title}</b>
                            
                        </p>    
                        <div style="padding: 5px;
                                    background : white;
                                    border-radius: 0px 0px 5px 5px; 
                                    border: 2px solid rgb(117, 103, 87);">    
                            <ul>
                                <li><b>Usuario </b>${aFood.user}</li>
                                <li><b>Fecha&nbsp;&nbsp;&nbsp;</b>${aFood.date}</li>
                                <li><b>Hora&nbsp;&nbsp;&nbsp;&nbsp;</b>${aFood.time}</li>
                                <li><b>Dispensado </b>${roundToDecimals(aFood.weight,2)} gramos
                            </ul>
                        </div>
                    </body>
                </html>
            """.trimIndent()

        }


        /**
         * Método generateWebViewHopperState
         * Método encargado de realizar la carga del webView destinado a mostrar el estado de la tolva.
         * @param hopperStatus Estado de la tolva
         * @param title Título a mostrar en el webView
         * @return String Cadena de texto con el contenido HTML
         */
        @RequiresApi(Build.VERSION_CODES.O)
        fun generateWebViewHopperState(hopperStatus : String, title : String) : String{
            val today = LocalDateTime.now()
            val time ="" + today.hour + ":" + today.minute
            val date ="" + today.dayOfMonth + "-" + today.month + "-" + today.year

            return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title></title>
                </head>
                    <body style=" background : rgb(254, 247, 255);">
                        <p style="padding: 2px 2px 1px 10px; 
                                  border-radius: 5px 5px 0px 0px; 
                                  margin-bottom: 0; 
                                  background : rgb(117, 103, 87);
                                  color : rgb(254, 247, 255);
                                  border: 2px solid rgb(117, 103, 87);">
                                  <b>${title}</b>
                        </p>    
                        <div style="padding: 5px; 
                                    background : white;
                                    border-radius: 0px 0px 5px 5px;
                                    border: 2px solid rgb(117, 103, 87);">    
                            <ul>
                                <li><b>Estado </b>${formatHopperState(hopperStatus)}</b></li> 
                                <li><b>Fecha&nbsp;&nbsp;&nbsp;</b>${date}&nbsp;${time}</li>
                            </ul>
                        </div>
                    </body>
                </html>
            """.trimIndent()
        }


        /**
         * Método formatHopperState
         * Método de apoyo destinado a montar el span con los estilos en función del estado de la tolva
         * @param state Estado de la tolva "DANGER, WARNING, OK"
         * @return String Cadena de texto con el contenido HTML
         */
        private fun formatHopperState(state : String) : String{
            var span = """<spam style="padding:2px 10px 2px 10px; 
                                       border-radius:2px; 
                                       color:rgb(255, 255, 255); 
                                       font-weight:bold; background:""".trimMargin()
            return when(state){
                "DANGER"  -> span + """ rgb(220, 53, 69)"> ${state}</span>"""
                "WARNING" -> span + """ rgb(255, 193, 7)"> ${state}</span>"""
                else -> span + """ rgb(25, 135, 84)"> ${state}</span>"""
            }
        }


        /********************************************** Utilidades ****************************************************/

        /**
         * Método getIpServer
         * Este método se encarga de obtener la IP del servidor TCP.
         * @param _context Contexto de la actividad
         * @return String Cadena que representa la dirección IP del servicio DOGFEEDER
         */
        fun getIpServer(_context: Activity) : String?{
            // Step 1. Se obtiene la instancia del servicio de conectividad y las propiedades de la conexión activa
            //         Es decir, donde estemos conectado WIFI o Red movil
            val connectivityManager = _context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val linkAddresses = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)?.linkAddresses

            // Step 2. Filtramos para obtener la primera dirección que cuente con un "." -> Indica que es un IP v4
            var ipV4Address = linkAddresses?.firstOrNull { linkAddress ->
                linkAddress.address.hostAddress?.contains('.') ?: false }?.address?.hostAddress

            // Step 3. Troceamos la dirección utilizando el delimitador del punto y obtenemos un array con los cuatro números
            //         de la IP v4
            val aIp = ipV4Address?.split(".")

            // Step 4. Si el array no esta vacío operamos dentro del if y creamos la IP del servidor, cambiando el último
            //         número por 240 ya que el servicio corre de forma estática en cualquier red de tipo C, siendo la maquina
            //         240. No contempla subredes u otras redes que no sean de clase C
            if(aIp?.isNotEmpty() == true){
                ipV4Address = aIp[0] + "." + aIp[1] + "." + aIp[2] + ".240"
            }

            // Step 5. Si no empieza 192.168 -> lanzamos una excepción para que la capture la llamada al método y resulva
            //         lo antes posible la falta de conectividad, ya que no nos encontraremos en la red que opera el servicio
            //         DOG-FEEDER
            if (ipV4Address == null || !ipV4Address.startsWith("192.168")) {
                throw IllegalArgumentException("Invalid IP address: $ipV4Address")
            }
            // Step 6. Finalmente se retorna la dirección IP
            return ipV4Address
        }


        /**
         * Método roundToDecimals
         * Método encargado de realizar el redondeo de decimales
         * @param number Número decimal a redondear
         * @param numberOfDecimals Número de decimales a redondear
         * @return Double Número decimal redodeado
         */
        fun roundToDecimals(number: Double, numberOfDecimals: Int): Double {
            val factor = Math.pow(10.0, numberOfDecimals.toDouble())
            return Math.round(number * factor) / factor
        }


        /**
         * Método loadSpinner
         * Método encargado de realizar la carga de los diversos spinners que se encuentran en los formularios de las vistas
         * @param spinner Control sobre el que se realiza la carga de datos
         * @param items Items o elementos a cargar en el interior del spinner
         * @param _context Contexto de la actividad
         */
        fun loadSpinner(spinner : Spinner, items : List<String>, _context : Activity){

            // Crea un adaptador para el Spinner
            val adapter = ArrayAdapter(_context, R.layout.simple_spinner_item, items)

            // Especifica el estilo del menú desplegable
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Asigna el adaptador al Spinner
            spinner.adapter = adapter
        }


        /**
         * Método activateMainSwipeRefresh
         * Este método se encarga de activar / desctivar la función de refresco del activity Main.
         * @param _context Referencia al contexto del Activity
         * @param isEnabled Valor para activar o desactivar el refresco
         */
        fun activateMainSwipeRefresh(_context: Activity, isEnabled : Boolean){
            val mainSwipe = _context.findViewById<SwipeRefreshLayout>(com.lopez.guillen.dogfeeder.R.id.swipeMain)
            mainSwipe.isEnabled = isEnabled
        }
    }
}