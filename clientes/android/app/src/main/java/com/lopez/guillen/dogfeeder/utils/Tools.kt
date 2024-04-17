package com.lopez.guillen.dogfeeder.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import com.lopez.guillen.dogfeeder.model.User
import org.json.JSONObject

class Tools {
    companion object{
        /**
         * Método showAlertDialog
         * Este método se encarga de mostrar información al usuario mediante un AlertDialog. Es un método generico para mostrar
         * cualquier msg de texto que entre como parametro. Solo contiene el boton de cierre y el manejador para dicho botón
         */
        fun showAlertDialog(context: Context, msg: String){
            // Step 1. Se construye el objeto cosntructor
            val alert = AlertDialog.Builder(context)

            // Step 2. Se establece el botón de cierre y la función lambda encargada de manejar la acción de cierre mediante
            //         el método dismiss.
            alert.setMessage(msg).setPositiveButton("Cerrar"){alertDialog, _ ->
                alertDialog.dismiss()
            }
            // Step 3. Se crea el objeto mediante el metodo create del constructor y se muestra al usuario mediante el metodo
            //         show.
            val aDialog = alert.create()
            aDialog.show()
        }

        /**
         * Método loadSharedPreferences
         * Se encarga de determinar si nuestras shared preferences cxuentan con un usuario logueado.
         * @return true o false
         */
        fun loadSharedPreferences(sharedPrefences: SharedPreferences) : Boolean{
            return sharedPrefences.contains("user")
        }

        /**
         * Método setUserInSharedPreferences [Sobrecargado]
         * Se encaga de guardar el usuario en las sharedPreferences
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
         */
        fun getUserInSharedPreferences(sharedPrefences: SharedPreferences) : User? {
            val usuarioJson = sharedPrefences.getString("user", null)
            return if (usuarioJson != null) jsonToUser(usuarioJson) else null
        }

        /**
         * Método cheanSharedPreferences
         * Se encarga de borrar el contenido de las sharedPreferences. Utilizado cuando el usuario cierra sesion.
         */
        fun cheanSharedPreferences(sharedPrefences: SharedPreferences) {
            val editor = sharedPrefences.edit()
            editor.clear()
            editor.apply()
        }

        /**
         * Método jsonToUser
         * Este método se encarga de convertir un JSON en una instancia de la data class "User" encargada de modelar un usuario
         * de la aplicacion.
         */
        private fun jsonToUser(json: String): User {
            val jsonObject = JSONObject(json)
            return User(/*jsonObject.optString("name"),
                jsonObject.optString("password"),
                jsonObject.optString("born"),
                jsonObject.optString("gender"),*/
                jsonObject.optString("email"),
                jsonObject.optString("password"))
        }
    }
}