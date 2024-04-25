package com.lopez.guillen.dogfeeder

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lopez.guillen.dogfeeder.model.FragmentInteractionListener

class LoginActivity : AppCompatActivity(), FragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Step 1. Se establece la pantalla de carga y espera 3 segundos antes de establecer el layout del activity
        installSplashScreen()
        Thread.sleep(3000)
        setContentView(R.layout.activity_login)

        // Step. 2. Se lealiza la carga del layout del login en el frame layout
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayoutLogin, FragmentLogin())
        fragmentTransaction.commit()
    }


    /**
     * Manejador de eventos onHandleRegisterUser
     * Se encarga de manejar el click sobre el boton de registro de un nuevo usuario. Simplemente, carga en el frameLayout
     * el layout correspondiente a la vista de registro de un nuevo usuario de la aplicación.
     */
    override fun onHandleRegisterUser() {
        // Step 1. Instanciamos el objeto encargado de interactuar con los fragments
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        // Step 2. Se realiza el reemplazo del fragment por el deseado y se crea una nueva instancia FragmentRegister asociado
        //         al contexto de este activity.
        fragmentTransaction.replace(R.id.frameLayoutLogin, FragmentRegister())
        // Step 3. Se maneja la pila de retroceso permitiendo volver al usuario al fragment anterior que siempre será loginFragment
        fragmentTransaction.addToBackStack(null)
        // Step 4. Se confirma la transacción y se aplican los cambios
        fragmentTransaction.commit()
    }

    override fun onHandleRecoveryPassword() {
        // Step 1. Instanciamos el objeto encargado de interactuar con los fragments
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        // Step 2. Se realiza el reemplazo del fragment por el deseado y se crea una nueva instancia FragmentRecoveryPassword asociado
        //         al contexto de este activity.
        fragmentTransaction.replace(R.id.frameLayoutLogin, FragmentRecoveryPassword())
        // Step 3. Se maneja la pila de retroceso permitiendo volver al usuario al fragment anterior que siempre será loginFragment
        fragmentTransaction.addToBackStack(null)
        // Step 4. Se confirma la transacción y se aplican los cambios
        fragmentTransaction.commit()
    }


    /**
     * Método getLoginContext
     * Getter para el contexto del activity
     */
    fun getLoginContext(): Activity {
        return this
    }
}